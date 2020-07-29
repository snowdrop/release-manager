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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class ReleaseTest {
	@Test
	public void creatingFromGitRefShouldWork() throws Exception {
		final String gitRef = "metacosm/spring-boot-bom/release-integration";
		final Release release = Release.createFromGitRef(gitRef);
		assertNotNull(release);
		assertEquals(gitRef, release.getGitRef());
		assertEquals("2.3.1.RELEASE", release.getVersion());

		final Component component = release.getComponents().get(0);

		assertNotNull(component.getParent());
		/*
		org.hibernate:hibernate-core:5.4.14.Final
		org.hibernate:hibernate-entitymanager:5.4.14.Final
		 org.hibernate.validator:hibernate-validator:6.0.18.Final
		 */

		final List<Artifact> artifacts = component.getArtifacts();
		assertEquals(3, artifacts.size());

		final String hibernateVersion = "5.4.14.Final";
		checkArtifact(artifacts, 0, "org.hibernate:hibernate-core", hibernateVersion);
		checkArtifact(artifacts, 1, "org.hibernate:hibernate-entitymanager", hibernateVersion);
		checkArtifact(artifacts, 2, "org.hibernate.validator:hibernate-validator", "6.0.18.Final");

		final List<Issue> cves = release.getCves();
		assertEquals(4, cves.size());

		Issue cve = cves.get(0);
		assertEquals(cve.getProject(), "ENTSBT");
		assertEquals(cve.getKey(), "360");

		cve = cves.get(1);
		assertEquals(cve.getProject(), "ENTSBT");
		assertEquals(cve.getKey(), "316");
	}

	private void checkArtifact(List<Artifact> artifacts, int index, String expectedName, String expectedVersion) {
		final Artifact artifact = artifacts.get(index);
		assertEquals(expectedName, artifact.getName());
		assertEquals(expectedVersion, artifact.getVersion());
	}
}
