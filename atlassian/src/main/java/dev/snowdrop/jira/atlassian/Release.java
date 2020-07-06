package dev.snowdrop.jira.atlassian;

import org.codehaus.jackson.annotate.JsonProperty;

public class Release {
    @JsonProperty
    private String version;

    @JsonProperty
    private String date;

    @JsonProperty
    private String EOL;

    @JsonProperty
    private String title;

    @JsonProperty
    private String template;

    @JsonProperty
    private String dueDate;

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
}
