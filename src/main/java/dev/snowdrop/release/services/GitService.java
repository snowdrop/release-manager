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
import org.eclipse.jgit.transport.CredentialsProvider;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@ApplicationScoped
public class GitService {
    static final Logger LOG = Logger.getLogger(GitService.class);
    private final static Pattern sha1Pattern = Pattern.compile("\\b[0-9a-f]{5,40}\\b");
    private CompletableFuture<Git> git;
    private String token;
    private File repository;
    
    
    static InputStream getStreamFrom(String gitRef, String relativePath) throws IOException {
        URI uri = URI.create("https://raw.githubusercontent.com/" + gitRef + "/" + relativePath);
        return uri.toURL().openStream();
    }
    
    public File getRepositoryDirectory() {
        return repository;
    }
    
    public void initRepository(String gitRef, String token) throws IOException {
        this.token = token;
        // first parse git ref
        final var split = gitRef.split("/");
        if (split.length != 3) {
            throw new IllegalArgumentException("Invalid git reference: " + gitRef + ". Must follow organization/repository/branch format.");
        }
        final var branch = "refs/heads/" + split[2];
        
        repository = Files.createTempDirectory("snowdrop-bom").toFile();
        repository.deleteOnExit();
        
        git = CompletableFuture.supplyAsync(() -> {
            try {
                final var uri = "https://github.com/" + split[0] + "/" + split[1] + ".git";
                final var git = Git.cloneRepository()
                    .setURI(uri)
                    .setBranchesToClone(Collections.singleton(branch))
                    .setBranch(branch)
                    .setDirectory(repository)
                    .setCredentialsProvider(getCredentialsProvider(token))
                    .call();
                LOG.infof("Cloned %s in %s", uri, repository.getPath());
                return git;
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private CredentialsProvider getCredentialsProvider(String token) {
        return new UsernamePasswordCredentialsProvider(token, "");
    }
    
    public void commitAndPush(String commitMessage, File... changed) throws IOException {
        try {
            git.thenAcceptAsync(g -> {
                try {
                    final var status = g.status().call();
                    final var uncommittedChanges = status.getUncommittedChanges();
                    var hasChanges = false;
                    if (!uncommittedChanges.isEmpty()) {
                        final var addCommand = g.add().setUpdate(true);
                        for (File file : changed) {
                            final var path = file.getName();
                            if (uncommittedChanges.contains(path)) {
                                // only add file to be committed if it's part of the modified set
                                LOG.infof("Added %s", path);
                                addCommand.addFilepattern(path);
                                hasChanges = true;
                            }
                        }
                        if (hasChanges) {
                            addCommand.call();
                            final var commit = g.commit().setMessage(commitMessage).call();
                            LOG.infof("Committed: %s", commit.getFullMessage());
                            g.push().setCredentialsProvider(getCredentialsProvider(token)).call();
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

    public String getCveIssueTitle(final String version) {
        return "CVE " + (version != null ? "for " + version : "for triage");
    }

    public void createGithubIssue(final String mdText, final String issueTitle, final String label,
        final String token, final String repoName) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository gitHubRepo = github.getRepository(repoName);
        GHIssueBuilder githubIssueBuilder = gitHubRepo.createIssue(issueTitle);
        githubIssueBuilder.body(mdText);
        githubIssueBuilder.label(label);
        githubIssueBuilder.create();
    }

    /**
     * Close issues
     * @param label
     * @param token
     * @param repoName
     * @param version
     * @throws IOException
     */
    public void closeOldCveIssues(final String label, final String token, final String repoName, final String version) throws IOException {
        final String issueTitle = this.getCveIssueTitle(version);
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository gitHubRepo = github.getRepository(repoName);
        List<GHIssue> ghIssues = gitHubRepo.getIssues(GHIssueState.OPEN);
        ghIssues.stream().filter(issue -> {
            try {
                LOG.warnf("Issue title: %s; calculated title: %s;", issue.getTitle(),issueTitle);
               return (issue.getLabels().stream().anyMatch(labels -> {
                   return labels.getName().equalsIgnoreCase(label);
               }) && (
                issue.getTitle().equalsIgnoreCase(issueTitle)
               )
               );
            } catch (IOException e) {
                LOG.warnf("Error filtering issue %s. Error: %s", issue.getNumber(), e.getLocalizedMessage());
                return false;
            }
        }).forEach(issue -> {
            try {
                issue.close();
            } catch (IOException e) {
                LOG.warnf("Error closing issue {}. Error: {}", issue.getNumber(), e.getLocalizedMessage());
            }
        });
    }
}
