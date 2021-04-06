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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.snowdrop.release.model.buildconfig.BuildConfig;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Singleton
public class BuildConfigFactory {
    public static final String BUILD_CONFIG_FILE_NAME = "build-config.yaml";
    private static final Logger LOG = Logger.getLogger(BuildConfigFactory.class);
    private static final YAMLMapper MAPPER = new YAMLMapper();

    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
        final var factory = MAPPER.getFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Inject
    GitlabService gitlab;

    private String gitRef;
    private String gitBranch;

    private String releaseVersionMajMin;

    static InputStream getStreamFromGitRef(final String gitRepoRef, final String gitBranchRef, final String relativePath) throws IOException {
        return GitlabService.getStreamFrom(gitRepoRef, gitBranchRef, relativePath);
    }

    /**
     * @param gitRef         a GitHub reference in the form org/project/reference e.g.
     *                       springboot/build-configurations
     * @param releaseVersion <p>Release version, e.g. 2.4.3</p>
     * @return
     * @throws Exception
     */
    public BuildConfig createFromGitRef(final String gitRef, final String releaseVersion)
        throws Throwable {
        LOG.infof("#createFromGitRef(%s,%s,%s)...", gitRef, gitBranch, releaseVersion);
        this.gitRef = gitRef;
        this.gitBranch = gitlab.getBranchName(releaseVersion);
        final String[] releaseMMF = releaseVersion.split("\\.");
        this.releaseVersionMajMin = releaseMMF[0] + "." + releaseMMF[1];
        final String relativePath = getRelativePath();
        LOG.infof("relativePath: %s", relativePath);
        try (InputStream buildConfigIS = getStreamFromGitRef(gitRef, gitBranch, relativePath)) {
            final var buildConfig = createFrom(buildConfigIS);
            System.out.println("Loaded release " + buildConfig.getVersion() + "." + buildConfig.getMilestone());
            return buildConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRelativePath() {
        return "spring-boot/" + releaseVersionMajMin + "/" + BUILD_CONFIG_FILE_NAME;
    }

    public void pushChanges(BuildConfig buildConfig) throws IOException {
//        if (!release.isTestMode()) {
        if (Utility.isStringNullOrBlank(gitRef)) {
            throw new IllegalArgumentException("Cannot push changes to BuildConfig not associated with a git ref");
        }
        final var buildConfigFile = new File(gitlab.getRepositoryDirectory(), getRelativePath());
        saveTo(buildConfig, buildConfigFile);
        gitlab.commitAndPush("chore: update release issues' key [issues-manager]", buildConfigFile);
//        }
    }

    public BuildConfig createFrom(
        InputStream releaseIS) throws Throwable {
        try {
            final var buildConfig = CompletableFuture.supplyAsync(() -> {
                try {
                    return MAPPER.readValue(releaseIS, BuildConfig.class);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            });
            return buildConfig.get();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    void saveTo(BuildConfig buildConfig, File to) throws IOException {
        final var writer = MAPPER.writerFor(BuildConfig.class);
        writer.writeValue(to, buildConfig);
    }
}
