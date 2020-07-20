package dev.snowdrop.jira.atlassian.model;

import java.util.List;

public class Release {
	private String jiraProject;
	private String jiraKey;
	private String version;
	private String longVersionName;
	private String fixVersion;
	private String EOL;
	private String releaseDate;
	private String dueDate;
	private String dueDateFormatted;
	private List<Component> components;
	private List<Cve> cves;

	public String getLongVersionName() {
		return longVersionName;
	}

	public void setLongVersionName(String longVersionName) {
		this.longVersionName = longVersionName;
	}

	public String getDueDateFormatted() {
		return dueDateFormatted;
	}

	public void setDueDateFormatted(String dueDateFormatted) {
		this.dueDateFormatted = dueDateFormatted;
	}

	public String getJiraKey() {
		return jiraKey;
	}

	public void setJiraKey(String jiraKey) {
		this.jiraKey = jiraKey;
	}

	public String getJiraProject() {
		return jiraProject;
	}

	public void setJiraProject(String jiraProject) {
		this.jiraProject = jiraProject;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFixVersion() {
		return fixVersion;
	}

	public void setFixVersion(String fixVersion) {
		this.fixVersion = fixVersion;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getEOL() {
		return EOL;
	}

	public void setEOL(String EOL) {
		this.EOL = EOL;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public List<Cve> getCves() {
		return cves;
	}

	public void setCves(List<Cve> cves) {
		this.cves = cves;
	}
}
