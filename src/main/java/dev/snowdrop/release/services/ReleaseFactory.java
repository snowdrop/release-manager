/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package dev.snowdrop.release.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.snowdrop.release.model.POM;
import dev.snowdrop.release.model.Release;
import org.jboss.logging.Logger;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Singleton
public class ReleaseFactory {
    private static final Logger LOG = Logger.getLogger(ReleaseFactory.class);

    @Inject
    JiraRestClient restClient;

    @Inject
    GitService git;

    private static final YAMLMapper MAPPER = new YAMLMapper();

    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
        final var factory = MAPPER.getFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * @param gitRef a GitHub reference in the form org/project/reference e.g.
     *               metacosm/spring-boot-bom/release-integration
     * @return
     * @throws Exception
     */
    public Release createFromGitRef(String gitRef) throws Throwable {
        return createFromGitRef(gitRef, false, false);
    }

    public Release createFromGitRef(String gitRef, boolean skipProductRequests, boolean skipScheduleValidation)
            throws Throwable {
        try (InputStream releaseIS = getStreamFromGitRef(gitRef, "release_template.yml");
                InputStream pomIS = getStreamFromGitRef(gitRef, "pom.xml")) {

            final var release = createFrom(releaseIS, pomIS, skipProductRequests, skipScheduleValidation);
            release.setGitRef(gitRef);
            System.out.println("Loaded release " + release.getVersion() + " from " + release.getGitRef());
            return release;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Release createFromGitRef(
        String gitRef,
        boolean skipProductRequests,
        boolean skipScheduleValidation,
        String version) throws Throwable {
        try (InputStream releaseIS = getStreamFromGitRef(gitRef, "release" + "-" + version + ".yml");
             InputStream pomIS = getStreamFromGitRef(gitRef, "pom.xml")) {

            final var release = createFrom(releaseIS, pomIS, skipProductRequests, skipScheduleValidation);
            release.setGitRef(gitRef);
            System.out.println("Loaded release " + release.getVersion() + " from " + release.getGitRef());
            return release;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void pushChanges(Release release) throws IOException {
        if (!release.isTestMode()) {
            final var gitRef = release.getGitRef();
            if (Utility.isStringNullOrBlank(gitRef)) {
                throw new IllegalArgumentException("Cannot push changes to Release not associated with a git ref");
            }
            String releaseFileName = "release" + "-" + release.getVersion() + ".yml";
            final var releaseFile = new File(git.getRepositoryDirectory(), releaseFileName);
            saveTo(release, releaseFile);
            git.commitAndPush("chore: update release issues' key [issues-manager]", releaseFile);
        }
    }

    Release createFrom(InputStream releaseIS, InputStream pomIS) throws Throwable {
        return createFrom(releaseIS, pomIS, false, false);
    }

    public Release createFrom(
            InputStream releaseIS,
            InputStream pomIS,
            boolean skipProductRequests,
            boolean skipScheduleValidation) throws Throwable {
        try {
            final var release = CompletableFuture.supplyAsync(() -> {
                try {
                    return MAPPER.readValue(releaseIS, Release.class);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            });
            final var pom = CompletableFuture.supplyAsync(() -> {
                try {
                    return POM.createFrom(pomIS);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });
            return pom.thenCombineAsync(release, (p, r) -> {
                r.setVersion(p.getVersion());
                r.setPom(p);
                r.setJiraClient(restClient);
                final var errors = r.validate(skipProductRequests, skipScheduleValidation);
                if (!errors.isEmpty()) {
                    throw new IllegalArgumentException(errors.stream().reduce("Invalid release:\n", Utility
                            .errorsFormatter(0)));
                }
                return r;
            }).join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    void saveTo(Release release, File to) throws IOException {
        final var writer = MAPPER.writerFor(Release.class);
        writer.writeValue(to, release);
    }

    static InputStream getStreamFromGitRef(String gitRef, String relativePath) throws IOException {
        return GitService.getStreamFrom(gitRef, relativePath);
    }
}
