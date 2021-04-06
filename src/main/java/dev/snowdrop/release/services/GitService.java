/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package dev.snowdrop.release.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.logging.Logger;

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
            throw new IllegalArgumentException("Invalid git reference: " + gitRef
                    + ". Must follow organization/repository/branch format.");
        }
        final var branch = "refs/heads/" + split[2];

        repository = Files.createTempDirectory("snowdrop-bom").toFile();
        repository.deleteOnExit();

        git = CompletableFuture.supplyAsync(() -> {
            try {
                final var uri = "https://github.com/" + split[0] + "/" + split[1] + ".git";
                final var git = Git.cloneRepository().setURI(uri).setBranchesToClone(Collections.singleton(branch))
                        .setBranch(branch).setDirectory(repository).setCredentialsProvider(getCredentialsProvider(
                                token)).call();
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
}
