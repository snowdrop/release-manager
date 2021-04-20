package dev.snowdrop.release.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GitService {

    static final Logger LOG = Logger.getLogger(GitService.class);
    private final Map<GitConfig, CompletableFuture<Git>> repositories = new ConcurrentHashMap<>(7);

    public static InputStream getStreamFrom(GitConfig config, String relativePath) throws IOException {
        return URI.create(config.getURI(relativePath)).toURL().openStream();
    }

    public void initRepository(GitConfig config) throws IOException {
        final var repository = Files.createTempDirectory(config.directoryPrefix()).toFile();
        repository.deleteOnExit();

        repositories.put(config, config.cloneFrom().thenApplyAsync(branch -> {
            try {
                LOG.infof("Cloned %s in %s", config.remoteURI(), repository.getPath());
                return Git.cloneRepository()
                    .setURI(config.remoteURI())
                    .setBranchesToClone(Collections.singleton(branch))
                    .setBranch(branch)
                    .setDirectory(repository)
                    .setCredentialsProvider(config.getCredentialProvider())
                    .call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        }).thenApplyAsync(config::checkout));
    }

    public void commitAndPush(String commitMessage, GitConfig config, FileModifier... changed) throws IOException {
        CompletableFuture<Git> git = repositories.get(config);
        if (git == null) {
            throw new IllegalStateException("must call initRepository first");
        }
        try {
            git.thenAcceptAsync(g -> {
                final File repository = g.getRepository().getWorkTree();
                try {
                    // process the potential changes
                    var files = Arrays.stream(changed).flatMap(fm -> fm.modify(repository)).collect(Collectors.toList());
                    final var status = g.status().call();
                    final var uncommittedChanges = status.getUncommittedChanges();
                    final var untracked = status.getUntracked();
                    final var statusChanged = status.getChanged();
                    final var removed = status.getRemoved();
                    final var missing = status.getMissing();
                    final var hasAdd = new boolean[]{false};
                    final var hasRm = new boolean[]{false};
                    LOG.debugf("getAdded %s", status.getAdded());
                    LOG.debugf("getChanged %s", status.getChanged());
                    LOG.debugf("getRemoved %s", removed);
                    LOG.debugf("getModified %s", status.getModified());
                    LOG.debugf("getMissing %s", status.getMissing());
                    LOG.debugf("getUntracked %s", untracked);
                    LOG.debugf("getUncommittedChanges %s", uncommittedChanges);
                    if (!uncommittedChanges.isEmpty() || !untracked.isEmpty() || !removed.isEmpty()) {
                        final var addCommand = g.add();
                        final var rmCommand = g.rm();
                        files.forEach(file -> {
                            final var path = file.getAbsolutePath().replace(repository.getAbsolutePath() + "/", "");
                            if (untracked.contains(path) || statusChanged.contains(path)) {
                                // only add file to be committed if it's part of the modified set or untracked
                                LOG.infof("Added %s", path);
                                addCommand.addFilepattern(path);
                                hasAdd[0] = true;
                            }
                            if (removed.contains(path) || missing.contains(path)) {
                                LOG.infof("Removed %s", path);
                                rmCommand.addFilepattern(path);
                                hasRm[0] = true;
                            }
                        });
                        if (hasAdd[0] || hasRm[0]) {
                            if (hasAdd[0]) {
                                addCommand.call();
                            }
                            if (hasRm[0]) {
                                rmCommand.call();
                            }
                            final var commit = g.commit().setMessage(commitMessage).call();
                            LOG.infof("Committed: %s", commit.getFullMessage());
                            final String branch = config.getBranch();
                            g.push().setRefSpecs(new RefSpec(branch + ":" + branch))
                                .setCredentialsProvider(config.getCredentialProvider()).call();
                            LOG.infof("Pushed");
                            return;
                        }
                    }
                    LOG.infof("No changes detected");
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @FunctionalInterface
    public interface FileModifier {

        Stream<File> modify(File repo);
    }

    public static class GitHubConfig extends GitConfig {

        private final String token;

        GitHubConfig(String org, String repo, String branch, String token, String previousBranch) {
            super(org, repo, branch, previousBranch);
            this.token = token;
        }

        CredentialsProvider getCredentialProvider() {
            return new UsernamePasswordCredentialsProvider(token, "");
        }

        @Override
        String directoryPrefix() {
            return "snowdrop-bom";
        }

        @Override
        public String remoteURI() {
            return "https://github.com/" + org + "/" + repo + ".git";
        }

        @Override
        public String getURI(String relativePath) {
            return "https://raw.githubusercontent.com/" + org + "/" + repo + "/" +  branch + "/" + relativePath;
        }
    }

    private static class GitLabConfig extends GitConfig {

        private final String user;
        private final String token;

        private GitLabConfig(String org, String repo, String branch, String user, String token, String previousBranch) {
            super(org, repo, branch, previousBranch);
            this.user = user;
            this.token = token;
        }

        @Override
        CredentialsProvider getCredentialProvider() {
            return new UsernamePasswordCredentialsProvider(user, token);
        }

        @Override
        String directoryPrefix() {
            return "issues-manager-" + org + "-" + repo;
        }

        @Override
        public String remoteURI() {
            return "https://gitlab.cee.redhat.com/" + org + "/" + repo + ".git";
        }

        @Override
        public String getURI(String relativePath) {
            return "https://gitlab.cee.redhat.com/" + org + "/" + repo + "/-/raw/" + branch + "/" + relativePath;
        }
    }

    public abstract static class GitConfig {

        public static final String DEFAULT_CLONE_FROM_BRANCH = "master";
        protected final String org;
        protected final String repo;
        protected final String branch;
        protected String cloneFromBranch = DEFAULT_CLONE_FROM_BRANCH;
        private final CompletableFuture<Boolean> branchMissing;


        private GitConfig(String org, String repo, String branch, String cloneFromBranch) {
            if (cloneFromBranch != null) {
                this.cloneFromBranch = cloneFromBranch;
            }
            this.org = org;
            this.repo = repo;
            this.branch = branch;
            branchMissing = CompletableFuture.supplyAsync(() -> {
                try {
                    Collection<Ref> refs = Git.lsRemoteRepository().setHeads(true)
                        .setCredentialsProvider(getCredentialProvider())
                        .setRemote(remoteURI()).call();
                    return refs;
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                }
            }).thenApplyAsync(branches -> branches.stream().noneMatch(ref -> ref.getName().contains(branch)));
        }

        /**
         * Returns a {@link GitConfig} adapted to connect to a GitHub repository identified by the specified {@code gitRef},
         * with the specified token and optionally cloning from the specified branch if the specified {@code gitRef} points to a
         * non-existing branch.
         *
         * @param gitRef          a String in the {@code organization/repository/branch} format where {@code branch} can point
         *                        to a non-existing branch
         * @param token           the GitHub token to use for the operations
         * @param cloneFromBranch the name of the branch to clone from if the branch specified by {@code gitRef} doesn't exist.
         *                        If {@code null} is specified, then {@link #DEFAULT_CLONE_FROM_BRANCH} will be used if the
         *                        desired branch needs to be created
         * @return the {@link GitConfig} needed to operate on the repository
         */
        public static GitConfig githubConfig(String gitRef, String token, String cloneFromBranch) {
            final var split = gitRef.split("/");
            if (split.length != 3) {
                throw new IllegalArgumentException("Invalid git reference: " + gitRef
                    + ". Must follow organization/repository/branch format.");
            }
            return new GitHubConfig(split[0], split[1], split[2], token, cloneFromBranch);
        }

        /**
         * Creates a {@link GitConfig} adapted to operate on the GitLab repository identified by the specified parameters,
         * creating the associated branch if needed.
         *
         * @param release         the Spring Boot release
         * @param username        the GitLab user name
         * @param token           the GitLab token
         * @param gitRef          the git reference identifying the repository in the {@code organization/repository} format
         * @param cloneFromGitRef the name of the branch to clone from if the branch specified by {@code gitRef} doesn't exist.
         *                        If {@code null} is specified, then {@link #DEFAULT_CLONE_FROM_BRANCH} will be used if the
         *                        desired branch needs to be created
         * @return the {@link GitConfig} needed to operate on the repository
         */
        public static GitConfig gitlabConfig(String release, String username, String token, String gitRef, String cloneFromGitRef) {
            final var split = gitRef.split("/");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid git reference: " + gitRef
                    + ". Must follow organization/repository format.");
            }
            final var branch = "snowdrop-issues-manager-" + release;
            return new GitLabConfig(split[0], split[1], branch, username, token, cloneFromGitRef);
        }

        /**
         * Retrieves the default branch name associated with the specified Spring Boot release.
         *
         * @param release the Spring Boot release to derive a branch name from, in the {@code Major.Minor.Micro} format
         * @return the default branch name in the {@code sb-Major.Minor.x} format
         */
        public static String getDefaultBranchName(final String release) {
            final String[] releaseMajorMinorFix = release.split("\\.");
            return String.format("sb-%s.%s.x", releaseMajorMinorFix[0], releaseMajorMinorFix[1]);
        }

        abstract CredentialsProvider getCredentialProvider();

        abstract String directoryPrefix();

        public abstract String remoteURI();

        public abstract String getURI(String relativePath);

        public String getBranch() {
            return branch;
        }

        CompletableFuture<String> cloneFrom() {
            return branchMissing.thenApplyAsync(missing -> "refs/heads/" + (missing ? cloneFromBranch() : branch));
        }

        protected String cloneFromBranch() {
            return cloneFromBranch;
        }

        public Git checkout(Git git) {
            branchMissing.thenAccept(missing -> {
                try {
                    if (missing) {
                        git.branchCreate().setName(branch).call();
                    }
                    git.checkout().setName(branch).call();
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                }
            });
            return git;
        }
    }
}
