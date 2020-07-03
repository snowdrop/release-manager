package dev.snowdrop.jira.atlassian;

import org.codehaus.jackson.annotate.JsonProperty;

public class Release {
    @JsonProperty
    private String version;

    @JsonProperty
    private String date;

    @JsonProperty
    private String EOL;

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
}
