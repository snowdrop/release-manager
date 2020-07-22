package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Artifact;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {

	private static Release release;

	@BeforeAll
	public static void init() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		release = mapper.readValue(getFileFromResources("release.yaml"), Release.class);
	}

	@Test
	public void checkDataOfAComponentTest() {
		assertNotNull(release);
		final Component component = release.getComponents().get(0);
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
	}

	private void checkArtifact(List<Artifact> artifacts, int index, String expectedName, String expectedVersion) {
		final Artifact artifact = artifacts.get(index);
		assertEquals(expectedName, artifact.getName());
		assertEquals(expectedVersion, artifact.getVersion());
	}


	@Test
	public void checkCVEsTest() throws JsonProcessingException {
		assertNotNull(release);
		assertEquals(release.getCves().get(0).getJiraProject(), "ENTSBT");
		assertEquals(release.getCves().get(0).getIssue(), "1010");

		assertEquals(release.getCves().get(1).getJiraProject(), "ENTSBT");
		assertEquals(release.getCves().get(1).getIssue(), "1020");
	}

	// Get file from classpath, resources folder
	private static File getFileFromResources(String fileName) {
		ClassLoader classLoader = ValidateYAMLtoPojoTest.class.getClassLoader();

		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return new File(resource.getFile());
		}
	}
}
