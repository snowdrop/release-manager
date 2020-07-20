package dev.snowdrop.jira.atlassian.model;

public class Cve {
	private String jiraProject;
	private String issue;

	public String getJiraProject() {
		return jiraProject;
	}

	public void setJiraProject(String jiraProject) {
		this.jiraProject = jiraProject;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}
}
