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
package dev.snowdrop.jira.atlassian.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@QuarkusTest
public class ReleaseTest {
	
	@Inject
	ReleaseFactory factory;
	
	private static InputStream getResourceAsStream(String s) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(s);
	}
	
	@Test
	public void missingVersionShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("missing-version.yml"), getResourceAsStream("pom.xml"), true);
			fail("should have failed on missing version");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("missing version"));
		} catch (IOException e) {
			fail(e);
		}
	}

	@Test
	public void mismatchedVersionShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("mismatched-version.yml"), getResourceAsStream("pom.xml"), true);
			fail("should have failed on mismatched version");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("release version doesn't match"));
		} catch (IOException e) {
			fail(e);
		}
	}

	@Test
	public void missingScheduleShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("missing-schedule.yml"), getResourceAsStream("pom.xml"), true);
			fail("should have failed on missing schedule");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("missing schedule"));
		} catch (IOException e) {
			fail(e);
		}
	}

	@Test
	public void wrongScheduleShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("invalid-schedule.yml"), getResourceAsStream("pom.xml"), true);
			fail("should have failed on invalid schedule");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("invalid release"));
			assertTrue(e.getMessage().contains("missing EOL"));
		} catch (IOException e) {
			fail(e);
		}
	}

	@Test
	public void mismatchedPOMVersionsShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("release.yml"), getResourceAsStream("mismatched-pom.xml"), true);
			fail("should have failed on mismatched POM versions");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("parent version doesn't match"));
			assertTrue(e.getMessage().contains("spring-boot.version"));
		} catch (IOException e) {
			fail(e);
		}
	}

	@Test
	public void invalidComponentProjectShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("invalid-component-project.yml"), getResourceAsStream("pom.xml"), false);
			fail("should have failed on invalid component project");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("invalid jira project 'FOO'"));
			assertTrue(e.getMessage().contains("invalid product project 'BAR'"));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	@Test
	public void invalidComponentProjectShouldNotFailIfProductsAreSkipped() throws IOException {
		factory.createFrom(getResourceAsStream("invalid-component-project.yml"), getResourceAsStream("pom.xml"), true);
	}
	
	@Test
	public void invalidComponentIssueTypeShouldFail() {
		try {
			factory.createFrom(getResourceAsStream("invalid-component-issuetypeid.yml"), getResourceAsStream("pom.xml"), false);
			fail("should have failed on invalid component issue type");
		} catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().contains("invalid issue type id '1234'"));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	@Test
	public void invalidComponentIssueTypeShouldNotFailIfProductsAreSkipped() throws IOException {
		factory.createFrom(getResourceAsStream("invalid-component-issuetypeid.yml"), getResourceAsStream("pom.xml"), true);
	}
	
	@Test
	public void validReleaseShouldWork() throws IOException {
		final Release release = factory.createFrom(getResourceAsStream("release.yml"), getResourceAsStream("pom.xml"));
		validate(release);
	}
	
	private void validate(Release release) {
		assertNotNull(release);
		final var expectedSBVersion = "2.3.2";
		assertEquals(expectedSBVersion, release.getVersion());
		
		final List<Component> components = release.getComponents();
		assertEquals(11, components.size());
		
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
		for (Artifact artifact : artifacts) {
			assertTrue(description.contains(artifact.getName()));
			assertTrue(description.contains(artifact.getVersion()));
		}
		
		component = components.get(7);
		assertEquals("Narayana starter", component.getName());
		final var product = component.getProduct();
		assertNotNull(product);
		final var jbtmAssignee = product.getJira().getAssignee().get();
		assertEquals("mmusgrov", jbtmAssignee);
		assertEquals(jbtmAssignee, component.getProductIssue().getAssignee().get());
		final var jira = component.getJira();
		assertEquals("gytis", jira.getAssignee().get());
		final var endOfSupportDate = product.getEndOfSupportDate();
		assertEquals(component.getParent().getSchedule().getFormattedEOLDate(), endOfSupportDate);
		description = product.getDescription();
		assertTrue(description.contains(endOfSupportDate));
		assertTrue(description.contains(expectedSBVersion));
		assertFalse(description.contains("**")); // this would happen if some substitutions didn't happen
		
		final List<Issue> cves = release.getCves();
		assertEquals(4, cves.size());
		
		Issue cve = cves.get(0);
		assertEquals(cve.getProject(), "ENTSBT");
		assertEquals(cve.getKey(), "360");
		
		cve = cves.get(1);
		assertEquals(cve.getProject(), "ENTSBT");
		assertEquals(cve.getKey(), "316");
	}
	
	@Test
	public void creatingFromNonExistentGitRefShouldFail() {
		assertThrows(IOException.class, () -> ReleaseFactory.getStreamFromGitRef("foo/bar", "release.yml"));
	}
	
	@Test
	public void creatingFromGitBranchShouldWork() throws Exception {
		final String gitRef = "snowdrop/spring-boot-bom/sb-2.3.x";
		ReleaseFactory.getStreamFromGitRef(gitRef, "release.yml");
	}
	
	@Test
	public void creatingFromGitCommitShouldWork() throws Exception {
		final String gitRef = "snowdrop/spring-boot-bom/1c45351";
		ReleaseFactory.getStreamFromGitRef(gitRef, "release.yml");
	}

	private void checkArtifact(List<Artifact> artifacts, int index, String expectedName, String expectedVersion) {
		final Artifact artifact = artifacts.get(index);
		assertEquals(expectedName, artifact.getName());
		assertEquals(expectedVersion, artifact.getVersion());
	}
}
