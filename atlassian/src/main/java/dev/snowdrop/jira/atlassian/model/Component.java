package dev.snowdrop.jira.atlassian.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class Component {

    @JsonProperty("jira-project")
    private String jiraProject;
    private String name;
    private String version;

    public String getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(String jiraProject) {
        this.jiraProject = jiraProject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
