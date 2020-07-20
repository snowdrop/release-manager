package dev.snowdrop.jira.atlassian.model;

public class Component {
    private String jiraProject;
    private String jiraTitle;
    private String name;
    private String version;
    private Boolean isStarter = false;
    private Boolean skipCreation = false;

    public Boolean getSkipCreation() {
        return skipCreation;
    }

    public void setSkipCreation(Boolean skipCreation) {
        if (skipCreation != null) {
            this.skipCreation = skipCreation;
        }
    }

    public String getJiraTitle() {
        return jiraTitle;
    }

    public void setJiraTitle(String jiraTitle) {
        this.jiraTitle = jiraTitle;
    }

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

    public Boolean getIsStarter() {
        return isStarter;
    }

    public void setIsStarter(Boolean isStarter) {
        if (isStarter != null) {
            this.isStarter = isStarter;
        }
    }
}
