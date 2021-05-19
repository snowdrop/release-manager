package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CPaaSNotification {
    @JsonProperty("release-team")
    private CPaaSReleaseTeam releaseTeam;
    @JsonProperty("component-owner")
    private Map<String,String> componentOwner;

    public CPaaSNotification() {
    }

    public CPaaSReleaseTeam getReleaseTeam() {
        return releaseTeam;
    }

    public void setReleaseTeam(CPaaSReleaseTeam releaseTeam) {
        this.releaseTeam = releaseTeam;
    }

    public Map<String, String> getComponentOwner() {
        return componentOwner;
    }

    public void setComponentOwner(Map<String, String> componentOwner) {
        this.componentOwner = componentOwner;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSNotification{");
        sb.append("releaseTeam=").append(releaseTeam);
        sb.append(", componentOwner=").append(componentOwner);
        sb.append('}');
        return sb.toString();
    }
}
