/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.snowdrop.release.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@ApplicationScoped
public class GitlabService {
    static final Logger LOG = Logger.getLogger(GitlabService.class);
    private final static Pattern sha1Pattern = Pattern.compile("\\b[0-9a-f]{5,40}\\b");
    private CompletableFuture<Git> git;
    private String username;
    private String token;
    private File repository;

    static InputStream getStreamFrom(final String gitRepoRef, final String gitBranchRef, final String relativePath) throws IOException {
//        https://gitlab.cee.redhat.com/middleware/build-configurations/-/raw/amq-clients/spring-boot-2.1/build-config.yaml
        URI uri = URI.create("https://gitlab.cee.redhat.com/" + gitRepoRef + "/-/raw/" + gitBranchRef + "/" + relativePath);
        return uri.toURL().openStream();
    }

    public File getRepositoryDirectory() {
        return repository;
    }

    public void initRepository(String gitRef, final String releaseVersion, String username, String token) throws IOException, GitAPIException {
        LOG.infof("#initRepository(%s,***;%s)...", gitRef, releaseVersion);
        this.token = token;
        this.username = username;
        final String branchName = getBranchName(releaseVersion);
        // first parse git ref
        final var split = gitRef.split("/");
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid git reference: " + gitRef
                + ". Must follow organization/repository format.");
        }
        final var branch = "refs/heads/" + branchName;

        repository = Files.createTempDirectory("issues-manager").toFile();
        repository.deleteOnExit();
        final var uri = "https://gitlab.cee.redhat.com/" + gitRef + ".git";
        Collection<Ref> gitBranches = Git.lsRemoteRepository().setHeads(true).setRemote(uri).call();
        if (!gitBranches.stream().anyMatch(ref -> ref.getName().equalsIgnoreCase(branch))) {
            LOG.warnf("Branch %s doesn't exist. Creating new branch...", branch);
            Git gitlab = Git.cloneRepository().setURI(uri)
                .setBranchesToClone(Collections.singleton("refs/heads/master"))
                .setBranch("refs/heads/master").setDirectory(repository).setCredentialsProvider(getCredentialsProvider(username,
                    token)).call();
            Ref ref1 = gitlab.branchCreate().setName(branchName).call();
            gitlab.push().setCredentialsProvider(getCredentialsProvider(username,token)).setRefSpecs(new RefSpec(ref1.getName() + ":" + ref1.getName())).call();
        }
        git = CompletableFuture.supplyAsync(() -> {
            try {
                Git gitlab = Git.cloneRepository().setURI(uri).setBranchesToClone(Collections.singleton(branch))
                    .setBranch(branch).setDirectory(repository).setCredentialsProvider(getCredentialsProvider(username,
                        token)).call();
                return gitlab;
            } catch (GitAPIException e) {
                LOG.error(e, e);
                throw new RuntimeException(e);
            }
        });
        LOG.infof("#initRepository(%s,***;%s) <- %s!", gitRef, releaseVersion, git);
    }

    public String getBranchName(String releaseMMF) {
        final String branchName = "snowdrop-issues-manager-" + releaseMMF;
        return branchName;
    }

    private CredentialsProvider getCredentialsProvider(final String username, final String token) {
        return new UsernamePasswordCredentialsProvider(username, token);
    }

    public void commitAndPush(String commitMessage, File... changed) throws IOException {
        try {
            git.thenAcceptAsync(g -> {
                try {
                    final var status = g.status().call();
                    final var uncommittedChanges = status.getUncommittedChanges();
                    final var untracked = status.getUntracked();
                    var hasChanges = false;
                    if (!uncommittedChanges.isEmpty() || !untracked.isEmpty()) {
                        final var addCommand = g.add();
                        for (File file : changed) {
                            final var path = file.getName();
                            if (uncommittedChanges.contains(path) || untracked.contains(path)) {
                                // only add file to be committed if it's part of the modified set or untracked
                                LOG.infof("Added %s", path);
                                addCommand.addFilepattern(path);
                                hasChanges = true;
                            }
                        }
                        if (hasChanges) {
                            addCommand.call();
                            final var commit = g.commit().setMessage(commitMessage).call();
                            LOG.infof("Committed: %s", commit.getFullMessage());
                            g.push().setCredentialsProvider(getCredentialsProvider(username,token)).call();
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

}
