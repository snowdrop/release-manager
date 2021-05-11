package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CPaaSTool {
    @JsonProperty
    private String type;
    @JsonProperty
    private List<CPaaSAdvisory> advisories;

    public CPaaSTool() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CPaaSAdvisory> getAdvisories() {
        return advisories;
    }

    public void setAdvisories(List<CPaaSAdvisory> advisories) {
        this.advisories = advisories;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSTool{");
        sb.append("type='").append(type).append('\'');
        sb.append(", advisories=").append(advisories);
        sb.append('}');
        return sb.toString();
    }
}
