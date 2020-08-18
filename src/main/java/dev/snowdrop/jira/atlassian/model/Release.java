package dev.snowdrop.jira.atlassian.model;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.jira.atlassian.Utility;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.MAPPER;
import static dev.snowdrop.jira.atlassian.Utility.isStringNullOrBlank;

@ApplicationScoped
public class Release {
	public static final String RELEASE_SUFFIX = ".RELEASE";
	@JsonProperty
	private String version;
	@JsonProperty
	private Issue issue;
	@JsonProperty
	private Schedule schedule;
	@JsonProperty
	private List<Component> components;
	@JsonProperty
	private List<Issue> cves;
	@JsonIgnore
	private String gitRef;
	@JsonIgnore
	private POM pom;

	@Inject
	JiraRestClient restClient;

	/**
	 * @param gitRef a GitHub reference in the form org/project/reference e.g. metacosm/spring-boot-bom/release-integration
	 * @return
	 * @throws Exception
	 */
	public static Release createFromGitRef(String gitRef) {
		try (InputStream releaseIS = getStreamFromGitRef(gitRef, "release.yml");
			  InputStream pomIS = getStreamFromGitRef(gitRef, "pom.xml")) {

			return createFrom(releaseIS, pomIS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static Release createFrom(InputStream releaseIS, InputStream pomIS) throws IOException {
		final Release release = MAPPER.readValue(releaseIS, Release.class);

		// retrieve associated POM
		release.pom = POM.createFrom(pomIS);

		// validate release
		final String pomVersion = release.pom.getVersion();
		release.validate(pomVersion);

		return release;
	}

	public void validate(String expectedVersion) throws IllegalArgumentException {
		final List<String> errors = new LinkedList<>();

		// validate version
		if (Utility.isStringNullOrBlank(version)) {
			errors.add("missing version");
		} else {
			final int suffix = version.indexOf(RELEASE_SUFFIX);
			final int len = suffix > 0 ? version.length() - suffix : version.length();
			if (!version.regionMatches(0, expectedVersion, 0, len)) {
				errors.add(String.format("'%s' release version doesn't match '%s' version in associated POM", version,
						expectedVersion));
			}
		}

		// validate schedule
		if (schedule == null) {
			errors.add("missing schedule");
		} else {
			if (isStringNullOrBlank(schedule.getReleaseDate())) {
				errors.add("missing release date");
			}
			try {
				schedule.getFormattedReleaseDate();
			} catch (Exception e) {
				errors.add("invalid release ISO8601 date: " + e.getMessage());
			}

			if (isStringNullOrBlank(schedule.getEOLDate())) {
				errors.add("missing EOL date");
			}
			try {
				schedule.getFormattedEOLDate();
			} catch (Exception e) {
				errors.add("invalid EOL ISO8601 date: " + e.getMessage());
			}
		}

		// validate components
		if (components != null) {
			components.parallelStream().forEach(component -> {
				validateIssue(errors, component, false);
				validateIssue(errors, component, true);
			});
		}

		if (!errors.isEmpty()) {
			throw new IllegalArgumentException(
					errors.stream().reduce("Invalid release:\n", (s, s2) -> s + "\t- " + s2 + "\n"));
		}
	}

	private void validateIssue(List<String> errors, Component component, boolean isProduct) {
		var issue = isProduct ? component.getProductIssue() : component.getJira();
		if (issue != null) {
			var componentEntryName = isProduct ? "product" : "jira";
			var project = issue.getProject();
			var name = component.getName();
			var issueTypeId = issue.getIssueTypeId();
			try {
				var p = restClient.getProjectClient().getProject(project).claim();
				for (IssueType issueType : p.getIssueTypes()) {
					if (issueType.getId().equals(issueTypeId)) {
						return;
					}
				}
				errors.add(String.format("invalid issue type id '%d' for component '%s'", issueTypeId, name));
			} catch (Exception e) {
				errors.add(String.format("invalid %s project '%s' for component '%s'", componentEntryName, project, name));
			}
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

	/**
	 * Associates this Release to the ticket identified by the specified key. Note that if the JIRA key is already set
	 * for this Release, this method won't do anything because the source of truth is assumed to be the YAML file.
	 *
	 * @param key the ticket identifier to which this Release should be associated
	 */
	public void setJiraKey(String key) {
		if (isStringNullOrBlank(getJiraKey())) {
			issue.setKey(key);
		}
	}

	public String getVersion() {
		return version;
	}

	public List<Component> getComponents() {
		// make sure that the parent is properly set, could probably be optimized if needed
		components.forEach(c -> c.setParent(this));
		return components;
	}

	public List<Issue> getCves() {
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
		return pom;
	}
}
