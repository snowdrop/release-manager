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

import dev.snowdrop.release.model.cpaas.product.*;
import dev.snowdrop.release.model.cpaas.release.CPaaSReleaseFile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.CoreTags.class)
public class CPaaSReleaseFactoryTest {
    @Inject
    CPaaSReleaseFactory factory;

    @Test
    public void checkProductParsing() throws Throwable {
        CPaaSProductFile productFile = factory.createCPaaSProductFromStream(HelperFunctions.getResourceAsStream("cpaas/product.yml"));
        CPaaSProduct product = productFile.getProduct();
        assertEquals("2.3.6", product.getRelease().getVersion());
        List<CPaaSProject> projects = product.getProjects();
        assertEquals(1, projects.size());
        CPaaSProject project = projects.get(0);
        assertEquals("spring-boot-parent", project.getName());
        assertEquals(1, project.getAdvisories().size());
        assertEquals("rhoar", project.getAdvisories().get(0));
        List<CPaaSComponent> components = project.getComponents();
        assertEquals(1, components.size());
        CPaaSComponent component = components.get(0);
        assertEquals("spring-boot", component.getName());
        List<CPaaSBuild> builds = component.getBuilds();
        assertEquals(1, builds.size());
        CPaaSBuild build = builds.get(0);
        assertEquals("pig", build.getType());
        assertEquals("latest", build.getPigVersion());
        List<CPaaSShip> ships = build.getShip();
        assertEquals(1, ships.size());
        CPaaSShip ship = ships.get(0);
        assertEquals("dev.snowdrop", ship.getArtifact().getGroup());
    }

    @Test
    public void checkReleaseParsing() throws Throwable {
        CPaaSReleaseFile release = factory.createCPaaSReleaseFromStream(HelperFunctions.getResourceAsStream("cpaas/release.yml"));
        assertEquals(2, release.getRelease().getPipelines().size());
    }
}
