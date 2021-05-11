package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CPaaSBuild {
    @JsonProperty
    private String type;
    @JsonProperty
    private String name;
    @JsonProperty
    private Integer priority;
    @JsonProperty("pig-flags")
    private String pigFlags;
    @JsonProperty("pig-version")
    private String pigVersion;
    @JsonProperty("pig-source")
    private CPaaSPigSource pigSource;
    @JsonProperty("staging-dir")
    private String stagingDir;
    @JsonProperty("skip-staging")
    private Boolean skipStaging;
    @JsonProperty("brew-push")
    private Boolean brewPush;
    @JsonProperty
    private List<CPaaSShip> ship;

    public CPaaSBuild() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPigFlags() {
        return pigFlags;
    }

    public void setPigFlags(String pigFlags) {
        this.pigFlags = pigFlags;
    }

    public String getPigVersion() {
        return pigVersion;
    }

    public void setPigVersion(String pigVersion) {
        this.pigVersion = pigVersion;
    }

    public CPaaSPigSource getPigSource() {
        return pigSource;
    }

    public void setPigSource(CPaaSPigSource pigSource) {
        this.pigSource = pigSource;
    }

    public String getStagingDir() {
        return stagingDir;
    }

    public void setStagingDir(String stagingDir) {
        this.stagingDir = stagingDir;
    }

    public Boolean getSkipStaging() {
        return skipStaging;
    }

    public void setSkipStaging(Boolean skipStaging) {
        this.skipStaging = skipStaging;
    }

    public Boolean getBrewPush() {
        return brewPush;
    }

    public void setBrewPush(Boolean brewPush) {
        this.brewPush = brewPush;
    }

    public List<CPaaSShip> getShip() {
        return ship;
    }

    public void setShip(List<CPaaSShip> ship) {
        this.ship = ship;
    }
}
