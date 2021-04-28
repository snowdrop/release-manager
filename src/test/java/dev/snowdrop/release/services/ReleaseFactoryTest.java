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

import dev.snowdrop.release.model.Artifact;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.Release;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.CoreTags.class)
public class ReleaseFactoryTest {

    @Inject
    ReleaseFactory factory;

    @Test
    public void missingVersionShouldNotFail() throws Throwable {
        factory.createFrom(HelperFunctions.getResourceAsStream("missing_version_template.yml"), HelperFunctions.getResourceAsStream("pom.xml"), true,
                false);
    }

    @Test
    public void missingScheduleShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("missing_schedule_template.yml"), HelperFunctions.getResourceAsStream("pom.xml"),
                    true, false);
            fail("should have failed on missing schedule");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("missing schedule"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void wrongScheduleShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("invalid_schedule_template.yml"), HelperFunctions.getResourceAsStream("pom.xml"),
                    true, false);
            fail("should have failed on invalid schedule");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("invalid release"));
            assertTrue(e.getMessage().contains("missing EOL"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void mismatchedPOMVersionsShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("release_template.yml"), HelperFunctions.getResourceAsStream("mismatched-pom.xml"),
                    true, false);
            fail("should have failed on mismatched POM versions");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("parent version doesn't match"));
            assertTrue(e.getMessage().contains("spring-boot.version"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void invalidComponentProjectShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_project_template.yml"), HelperFunctions.getResourceAsStream(
                    "pom.xml"), false, false);
            fail("should have failed on invalid component project");
        } catch (IllegalArgumentException e) {
            // expected
            final var message = e.getMessage();
            assertTrue(message.contains("Invalid product"));
            assertTrue(message.contains("Invalid jira"));
            assertTrue(message.contains("invalid project 'FOO'"));
            assertTrue(message.contains("invalid project 'BAR'"));
            assertTrue(message.contains("invalid issue key: INVALID"));
            assertTrue(message.contains("invalid issue key: " + MockIssueRestClient.ISSUE_KEY
                    + " doesn't match project FOO"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void emptyComponentProjectShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_empty_project_template.yml"), HelperFunctions.getResourceAsStream(
                    "pom.xml"), false, false);
            fail("should have failed on empty component project");
        } catch (IllegalArgumentException e) {
            // expected
            final var message = e.getMessage();
            assertFalse(message.contains("Invalid product"));
            assertTrue(message.contains("Invalid jira"));
            assertTrue(message.contains("project must be specified"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void invalidComponentProjectShouldNotFailIfProductsAreSkipped() throws Throwable {
        factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_project_template.yml"), HelperFunctions.getResourceAsStream(
                "pom.xml"), true, false);
    }

    @Test
    public void invalidComponentIssueTypeShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_issuetypeid_template.yml"), HelperFunctions.getResourceAsStream(
                    "pom.xml"), false, false);
            fail("should have failed on invalid component issue type");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("invalid issue type id '1234'"));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void invalidComponentIssueTypeShouldNotFailIfProductsAreSkipped() throws Throwable {
        factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_issuetypeid_template.yml"), HelperFunctions.getResourceAsStream(
                "pom.xml"), true, false);
    }

    @Test
    public void invalidComponentAssigneeShouldFail() {
        try {
            factory.createFrom(HelperFunctions.getResourceAsStream("invalid_component_assignee_template.yml"), HelperFunctions.getResourceAsStream(
                    "pom.xml"), false, false);
            fail("should have failed on invalid component assignee");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("invalid assignee for project 'ENTSBT': "
                    + MockUserRestClient.NON_EXISTING_USER));
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void validReleaseShouldWork() throws Throwable {
        final Release release = factory.createFrom(HelperFunctions.getResourceAsStream("release_template.yml"), HelperFunctions.getResourceAsStream(
                "pom.xml"));
        validate(release);
    }

    private void validate(Release release) {
        assertNotNull(release);
        final var expectedSBVersion = "2.3.2.RELEASE";
        assertEquals(expectedSBVersion, release.getVersion());

        final List<Component> components = release.getComponents();
        assertEquals(10, components.size());

        var component = components.get(0);
        assertNotNull(component.getParent());
        assertEquals("Hibernate / Hibernate Validator / Undertow / RESTEasy", component.getName());
        assertEquals("ivassile", component.getJira().getAssignee().get());

        final List<Artifact> artifacts = component.getArtifacts();
        assertEquals(7, artifacts.size());

        final String hibernateVersion = "5.4.18.Final";
        final String undertowVersion = "2.1.3.Final";
        checkArtifact(artifacts, 0, "org.hibernate:hibernate-core", hibernateVersion);
        checkArtifact(artifacts, 1, "org.hibernate:hibernate-entitymanager", hibernateVersion);
        checkArtifact(artifacts, 2, "org.hibernate.validator:hibernate-validator", "6.1.5.Final");
        checkArtifact(artifacts, 3, "io.undertow:undertow-core", undertowVersion);
        checkArtifact(artifacts, 4, "io.undertow:undertow-servlet", undertowVersion);
        checkArtifact(artifacts, 5, "io.undertow:undertow-websockets-jsr", undertowVersion);

        var description = component.getDescription();
        assertTrue(description.contains(component.getParent().getVersion()));
        assertFalse(description.contains("**")); // this would happen if some substitutions didn't happen
        assertTrue(description.contains("It is important that you also communicate the EOL of your product so that we can plan our future releases accordingly."));
        assertFalse(description.contains("tested and supported"));
        assertTrue(description.contains("- product name:"));
        for (Artifact artifact : artifacts) {
            assertTrue(description.contains(artifact.getName()));
            assertTrue(description.contains(artifact.getVersion()));
        }

        component = components.get(2);
        assertEquals("AMQP", component.getName());
        assertTrue(component.getDescription().contains("It is important that you also communicate the EOL of your product so that we can plan our future releases accordingly."));
        assertTrue(component.getDescription().contains("tested and supported"),component.toString() + component.getDescription());
        assertFalse(component.getDescription().contains("- product name:"),component.toString() + component.getDescription());
        component = components.get(6);
        assertEquals("Narayana starter", component.getName());
        assertTrue(component.getDescription().contains("tested and supported"));
        assertFalse(component.getDescription().contains("It is important that you also communicate the EOL of your product so that we can plan our future releases accordingly."));
        assertFalse(component.getDescription().contains("- product name:"));
        component = components.get(7);
        assertEquals("Vert.x starter", component.getName());
        final var product = component.getProduct();
        assertNotNull(product);
        assertEquals("Vert.x", product.getName());
        assertEquals("Currently supported Vert.x version information", product.getTitle());
        final var jbtmAssignee = product.getJira().getAssignee().get();
        assertEquals("rruss", jbtmAssignee);
        assertEquals(jbtmAssignee, component.getProductIssue().getAssignee().get());
        final var jira = component.getJira();
        assertEquals("gytis", jira.getAssignee().get());
        final var endOfSupportDate = product.getEndOfSupportDate();
        assertEquals(component.getParent().getSchedule().getFormattedEOLDate(), endOfSupportDate);
        description = product.getDescription();
        assertTrue(component.getDescription().contains(endOfSupportDate));
        assertTrue(component.getDescription().contains(expectedSBVersion));
        assertFalse(component.getDescription().contains("**")); // this would happen if some substitutions didn't happen
    }

    @Test
    public void creatingFromNonExistentGitRefShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> ReleaseFactory.getStreamFromGitRef("foo/bar", "release_template.yml"));
    }

    @Test
    public void creatingFromGitBranchShouldWork() throws Exception {
        final String gitRef = "snowdrop/spring-boot-bom/sb-2.4.x";
        ReleaseFactory.getStreamFromGitRef(gitRef, "release_template.yml");
    }

    @Test
    public void creatingFromGitCommitShouldWork() throws Exception {
        final String gitRef = "snowdrop/spring-boot-bom/74cea74";
        ReleaseFactory.getStreamFromGitRef(gitRef, "release_template.yml");
    }

    private void checkArtifact(List<Artifact> artifacts, int index, String expectedName, String expectedVersion) {
        final Artifact artifact = artifacts.get(index);
        assertEquals(expectedName, artifact.getName());
        assertEquals(expectedVersion, artifact.getVersion());
    }
}