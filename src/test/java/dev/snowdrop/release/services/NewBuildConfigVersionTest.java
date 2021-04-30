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

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.IntegrationTests.class)
public class NewBuildConfigVersionTest {

    private static final String PREVIOUS_SB_VERSION = "2.3.2.RELEASE";
    private static final String NEW_SB_VERSION = "9.9.9";
    private static final String GIT_REPO = "snowdrop/spring-boot-bom";
    private static final String GIT_BRANCH = "integration-test-2.4.x";

    private static final YAMLMapper MAPPER = new YAMLMapper();

    @Inject
    GitService git;

    @Inject
    SpringBootBomUpdateService springBootBomUpdateService;

    @Inject
    BuildConfigUpdateService buildConfigUpdateService;

    @Test
    public void newBOMMajMin() {
        final String token = ConfigProvider.getConfig().getValue("github.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("github.user", String.class);
        final String[] releaseMajorMinorFix = NEW_SB_VERSION.split("\\.");
        final String[] prevReleaseMajorMinorFix = PREVIOUS_SB_VERSION.split("\\.");
        final GitService.GitConfig bomGitConfig = GitService.GitConfig.githubConfig(String.format("%s/%s",GIT_REPO,GIT_BRANCH), user, token, Optional.of(String.format("sb-%s.%s.x", prevReleaseMajorMinorFix[0], prevReleaseMajorMinorFix[1])));
        try {
            git.initRepository(bomGitConfig);
            springBootBomUpdateService.newMajorMinor(bomGitConfig);
            File repository = git.getConfig(bomGitConfig).getRepository().getWorkTree();
            FileFilter fileFilter = new WildcardFileFilter("release-*.yml");
            File[] files = repository.listFiles(fileFilter);
            assertEquals(0,files.length );
        } catch (IOException | ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            fail(ex);
        } finally {
            try {
                git.deleteRemoteBranch(bomGitConfig, GIT_BRANCH, String.format("sb-%s.%s.x", prevReleaseMajorMinorFix[0], prevReleaseMajorMinorFix[1]));
            } catch (ExecutionException | InterruptedException | GitAPIException e) {
                e.printStackTrace();
                fail(e);
            }
        }
    }

    @Test
    public void newBuildConfigMajMin() {
        final String gluser = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        final String gltoken = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String[] releaseMajorMinorFix = NEW_SB_VERSION.split("\\.");
        final String[] prevReleaseMajorMinorFix = PREVIOUS_SB_VERSION.split("\\.");
        final GitService.GitConfig buildConfigGitlabConfig = GitService.GitConfig.gitlabConfig(NEW_SB_VERSION, gluser, gltoken, "snowdrop/build-configurations", Optional.of("master"), Optional.of(String.format("%s",GIT_BRANCH)));
        try {
            git.initRepository(buildConfigGitlabConfig);
            buildConfigUpdateService.newMajorMinor(buildConfigGitlabConfig, releaseMajorMinorFix[0], releaseMajorMinorFix[1], prevReleaseMajorMinorFix[0], prevReleaseMajorMinorFix[1]);
            File repository = git.getConfig(buildConfigGitlabConfig).getRepository().getWorkTree();
            File sbFolder = new File(repository.getPath()+"/"+String.format("spring-boot/%s.%s", releaseMajorMinorFix[0], releaseMajorMinorFix[1]));
            FileFilter fileFilter = new NameFileFilter("build-config.yaml");
            File[] files = sbFolder.listFiles(fileFilter);
            assertEquals(1, files.length );
        } catch (IOException | ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            fail(ex);
        } finally {
            try {
                git.deleteRemoteBranch(buildConfigGitlabConfig, GIT_BRANCH, "master");
            } catch (ExecutionException | InterruptedException | GitAPIException e) {
                e.printStackTrace();
                fail(e);
            }
        }
    }

}
