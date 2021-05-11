package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSRelease {
    @JsonProperty
    private String type;
    @JsonProperty
    private String version;

    public CPaaSRelease() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSRelease{");
        sb.append("type='").append(type).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
