package dev.snowdrop.jira.atlassian.model;

import java.util.List;

public class Release {
    private String jiraProject;
    private String jiraKey;
    private String version;
    private String fixVersion;
    private String date;
    private String EOL;
    private String title;
    private String dueDate;
    private List<Component> components;
    private List<Cve> cves;

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
