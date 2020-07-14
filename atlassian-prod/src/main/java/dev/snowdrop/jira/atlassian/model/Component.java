package dev.snowdrop.jira.atlassian.model;

public class Component {
    private String jiraProject;
    private String name;
    private String version;
    private String isStarter;
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

    public String getIsStarter() {
        return isStarter;
    }

    public void setIsStarter(String isStarter) {
        this.isStarter = isStarter;
    }
}
