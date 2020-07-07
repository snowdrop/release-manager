package dev.snowdrop.jira.atlassian.model;

import java.util.List;

public class Release {

    private String projectKey;
    private String version;
    private String fixVersion;
    private String date;
    private String EOL;
    private String title;
    private String template;
    private String dueDate;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    private List<Component> components;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEOL() {
        return EOL;
    }

    public void setEOL(String EOL) {
        this.EOL = EOL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }
}
