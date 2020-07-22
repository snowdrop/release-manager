package dev.snowdrop.jira.atlassian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
	private String version;

	public String getLongVersionName() {
		return "[Spring Boot " + version + "] Release steps CR [" + schedule.getFormattedReleaseDate() + "]";
	}

	public String getJiraKey() {
		return issue.getKey();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
}
