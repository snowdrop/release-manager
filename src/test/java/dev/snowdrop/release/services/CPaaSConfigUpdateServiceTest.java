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
import dev.snowdrop.release.model.POM;
import dev.snowdrop.release.model.Release;
import dev.snowdrop.release.model.cpaas.product.CPaaSProduct;
import dev.snowdrop.release.model.cpaas.product.CPaaSProductFile;
import dev.snowdrop.release.model.cpaas.release.CPaaSAdvisory;
import dev.snowdrop.release.model.cpaas.release.CPaaSPipelines;
import dev.snowdrop.release.model.cpaas.release.CPaaSRelease;
import dev.snowdrop.release.model.cpaas.release.CPaaSReleaseFile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.IntegrationTests.class)
public class CPaaSConfigUpdateServiceTest {

    private static final String RELEASE = "2.4.0";
    private static final String PREVIOUS_RELEASE = "2.3.6";
    private static final String PRODUCT_FILE_NAME = "product.yml";
    private static final String RELEASE_FILE_NAME = "release.yml";
    private static final YAMLMapper MAPPER = new YAMLMapper();

    @Inject
    CPaaSConfigUpdateService service;

    @Inject
    CPaaSReleaseFactory factory;

    @Inject
    GitService git;

    @Test
    public void testProduct() {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        GitService.GitConfig config = service.buildGitConfig(RELEASE, user, token, Optional.of(PREVIOUS_RELEASE), Optional.of(ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot")));
        try {
            git.initRepository(config);
            Stream<File> files = service.updateCPaaSFiles(RELEASE, git.getConfig(config).getRepository().getWorkTree(), PREVIOUS_RELEASE, false, true);
            Map<String, File> fileMap = files.collect(Collectors.toMap(file -> file.getName(), file -> file));
            assertTrue(fileMap.containsKey(PRODUCT_FILE_NAME), fileMap.toString());
            testProductFile(factory.createCPaaSProductFromStream(new FileInputStream(fileMap.get(PRODUCT_FILE_NAME))));
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            fail(e);
        }
    }

    private void testProductFile(CPaaSProductFile productFile) {
        CPaaSProduct product = productFile.getProduct();
        assertEquals(RELEASE, product.getRelease().getVersion());
        assertEquals("spring-boot/2.4", product.getProjects().get(0).getComponents().get(0).getBuilds().get(0).getPigSource().getRoot());
    }

    @Test
    public void testReleaseCommon() {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        GitService.GitConfig config = service.buildGitConfig(RELEASE, user, token, Optional.of(PREVIOUS_RELEASE), Optional.of(ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot")));
        try {
            git.initRepository(config);
            Stream<File> files = service.updateCPaaSFiles(RELEASE, git.getConfig(config).getRepository().getWorkTree(), PREVIOUS_RELEASE, false, true);
            Map<String, File> fileMap = files.collect(Collectors.toMap(file -> file.getName(), file -> file));
            assertTrue(fileMap.containsKey(RELEASE_FILE_NAME), fileMap.toString());
            testReleaseFile(factory.createCPaaSReleaseFromStream(new FileInputStream(fileMap.get(RELEASE_FILE_NAME))));
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            fail(e);
        }
    }

    private void testReleaseFile(CPaaSReleaseFile releaseFile) {
        CPaaSRelease release = releaseFile.getRelease();
        List<CPaaSPipelines> pipelines = release.getPipelines();
        assertTrue("build".equalsIgnoreCase(pipelines.get(0).getName()));
        assertTrue("release".equalsIgnoreCase(pipelines.get(1).getName()));
        CPaaSAdvisory cpaasAdvisoryRelease = release.getTools().get(0).getAdvisories().get(0);
        assertTrue(cpaasAdvisoryRelease.getSynopsis().contains(RELEASE), releaseFile.toString());
        assertFalse(cpaasAdvisoryRelease.getSynopsis().contains(PREVIOUS_RELEASE), releaseFile.toString());
    }

    @Test
    public void testReleaseDr() {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        GitService.GitConfig config = service.buildGitConfig(RELEASE, user, token, Optional.of(PREVIOUS_RELEASE), Optional.of(ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot")));
        try {
            git.initRepository(config);
            Stream<File> files = service.updateCPaaSFiles(RELEASE, git.getConfig(config).getRepository().getWorkTree(), PREVIOUS_RELEASE, false, true);
            Map<String, File> fileMap = files.collect(Collectors.toMap(file -> file.getName(), file -> file));
            CPaaSReleaseFile releaseFile = factory.createCPaaSReleaseFromStream(new FileInputStream(fileMap.get(RELEASE_FILE_NAME)));
            CPaaSRelease release = releaseFile.getRelease();
            List<CPaaSPipelines> pipelines = release.getPipelines();
            pipelines.get(1).getStages().forEach(stage -> {
                if ("create-errata-tool-advisories".equalsIgnoreCase(stage.getName())) {
                    assertFalse(stage.getEnabled());
                }
            });
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    public void testReleaseErCr() {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        GitService.GitConfig config = service.buildGitConfig(RELEASE, user, token, Optional.of(PREVIOUS_RELEASE), Optional.of(ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot")));
        try {
            git.initRepository(config);
            Stream<File> files = service.updateCPaaSFiles(RELEASE, git.getConfig(config).getRepository().getWorkTree(), PREVIOUS_RELEASE, true, true);
            Map<String, File> fileMap = files.collect(Collectors.toMap(file -> file.getName(), file -> file));
            CPaaSReleaseFile releaseFile = factory.createCPaaSReleaseFromStream(new FileInputStream(fileMap.get(RELEASE_FILE_NAME)));
            CPaaSRelease release = releaseFile.getRelease();
            List<CPaaSPipelines> pipelines = release.getPipelines();
            pipelines.get(1).getStages().forEach(stage -> {
                if ("create-errata-tool-advisories".equalsIgnoreCase(stage.getName())) {
                    assertFalse(stage.getEnabled());
                }
            });
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    public void createNewBranch() throws Throwable {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        InputStream releaseIS = HelperFunctions.getResourceAsStream("release_template_int_tests.yml");
        Release releaseObj = MAPPER.readValue(releaseIS, Release.class);
        releaseObj.setPom(POM.createFrom(HelperFunctions.getResourceAsStream("pom.xml")));

        final String[] prevReleaseMajorMinorFix = PREVIOUS_RELEASE.split("\\.");
        String repoName = ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot");
        GitService.GitConfig config = service.buildGitConfig(RELEASE, user, token, Optional.of(PREVIOUS_RELEASE), Optional.of(repoName));
        try {
            git.initRepository(config);
            service.newRelease(config, RELEASE, PREVIOUS_RELEASE, false, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex);
        } finally {
            try {
                git.deleteRemoteBranch(config, config.branch, "master");
            } catch (ExecutionException | InterruptedException | GitAPIException e) {
                e.printStackTrace();
                fail(e);
            }
        }
    }

}