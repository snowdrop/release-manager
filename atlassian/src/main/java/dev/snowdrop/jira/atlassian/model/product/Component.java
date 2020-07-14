package dev.snowdrop.jira.atlassian.model.product;

public class Component {

    private String projectKey;
    private String name;
    private String version;
    private String isStarter;

    public String getIsStarter() {
        return isStarter;
    }

    public void setIsStarter(String isStarter) {
        this.isStarter = isStarter;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
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
