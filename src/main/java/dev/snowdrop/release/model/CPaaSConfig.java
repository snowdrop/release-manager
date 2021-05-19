package dev.snowdrop.release.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSConfig {
    @JsonProperty
    private String productFile;
    @JsonProperty
    private String releaseFile;
    @JsonProperty
    private String advisoryFile;

    public String getProductFile() {
        return productFile;
    }

    public void setProductFile(String productFile) {
        this.productFile = productFile;
    }

    public String getReleaseFile() {
        return releaseFile;
    }

    public void setReleaseFile(String releaseFile) {
        this.releaseFile = releaseFile;
    }

    public String getAdvisoryFile() {
        return advisoryFile;
    }

    public void setAdvisoryFile(String advisoryFile) {
        this.advisoryFile = advisoryFile;
    }
}
