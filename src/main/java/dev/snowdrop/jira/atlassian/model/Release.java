package dev.snowdrop.jira.atlassian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.MAPPER;

public class Release {
	@JsonProperty
	private Issue issue;
	@JsonProperty
	private Schedule schedule;
	@JsonProperty
	private List<Component> components;
	@JsonProperty
	private List<Cve> cves;
	@JsonIgnore
	private String gitRef;
	@JsonIgnore
	private POM pom;

	/**
	 * @param gitRef a GitHub reference in the form org/project/reference e.g. metacosm/spring-boot-bom/release-integration
	 * @return
	 * @throws Exception
	 */
	public static Release createFromGitRef(String gitRef) throws Exception {
		try (InputStream inputStream = getStreamFromGitRef(gitRef, "release.yml")) {
			final Release release = MAPPER.readValue(inputStream, Release.class);
			release.setGitRef(gitRef);
			return release;
		}
	}

	private static InputStream getStreamFromGitRef(String gitRef, String relativePath) throws IOException {
		URI uri = URI.create("https://raw.githubusercontent.com/" + gitRef + "/" + relativePath);
		return uri.toURL().openStream();
	}

	public String getLongVersionName() {
		return "[Spring Boot " + getVersion() + "] Release steps CR [" + schedule.getFormattedReleaseDate() + "]";
	}

	public String getJiraKey() {
		return issue.getKey();
	}

	public String getVersion() {
		return getPOM().getVersion();
	}

	public List<Component> getComponents() {
		// make sure that the parent is properly set, could probably be optimized if needed
		components.forEach(c -> c.setParent(this));
		return components;
	}

	public List<Cve> getCves() {
		return cves;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public String getGitRef() {
		return gitRef;
	}

	private void setGitRef(String gitRef) {
		this.gitRef = gitRef;
	}

	public POM getPOM() {
		if (pom == null) {
			try (InputStream is = getStreamFromGitRef(gitRef, "pom.xml")) {
				this.pom = POM.createFrom(is);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return pom;
	}
}
