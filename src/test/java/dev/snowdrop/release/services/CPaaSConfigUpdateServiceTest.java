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
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.IntegrationTests.class)
public class CPaaSConfigUpdateServiceTest {

    private static final String RELEASE = "2.3.2";
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
    public void parseProductConfig() throws Throwable {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        InputStream releaseIS = HelperFunctions.getResourceAsStream("release_template.yml");
        Release releaseObj = MAPPER.readValue(releaseIS, Release.class);
        releaseObj.setPom(POM.createFrom(HelperFunctions.getResourceAsStream("pom.xml")));
        GitService.GitConfig config = service.buildGitConfig(releaseObj, user, token, Optional.of(ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot")));
        git.initRepository(config);
        Stream<File> files = service.updateCPaaSFiles(releaseObj, git.getConfig(config).getRepository().getWorkTree(), false);
        Map<String, File> fileMap = files.collect(Collectors.toMap(file -> file.getName(), file -> file));
        assertTrue(fileMap.containsKey(PRODUCT_FILE_NAME), fileMap.toString());
        CPaaSProductFile productFile = factory.createCPaaSProductFromStream(new FileInputStream(fileMap.get(PRODUCT_FILE_NAME)));
        CPaaSProduct product = productFile.getProduct();
        assertEquals(RELEASE, product.getRelease().getVersion());
        assertEquals("spring-boot/2.3", product.getProjects().get(0).getComponents().get(0).getBuilds().get(0).getPigSource().getRoot());
    }

    @Test
    public void shouldCreateNewBranchAndRemoveItAtTheEnd() throws Throwable {
        final String token = ConfigProvider.getConfig().getValue("gitlab.token", String.class);
        final String user = ConfigProvider.getConfig().getValue("gitlab.user", String.class);
        InputStream releaseIS = HelperFunctions.getResourceAsStream("release_template.yml");
        Release releaseObj = MAPPER.readValue(releaseIS, Release.class);
        releaseObj.setPom(POM.createFrom(HelperFunctions.getResourceAsStream("pom.xml")));
        String repoName = ConfigProvider.getConfig().getOptionalValue("gitlab.repository", String.class).orElse(user + "/springboot");
        GitService.GitConfig config = service.buildGitConfig(releaseObj, user, token, Optional.of(repoName));
        try {
            git.initRepository(config);
            service.newRelease(config, releaseObj, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex);
        } finally {
            git.deleteRemoteBranch(config, config.branch, "master");
        }
    }

}