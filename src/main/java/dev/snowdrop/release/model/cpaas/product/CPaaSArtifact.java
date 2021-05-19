package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSArtifact {
    @JsonProperty
    private String group;
    @JsonProperty
    private String artifact;
    @JsonProperty
    private String type;

    public CPaaSArtifact() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSArtifact{");
        sb.append("group='").append(group).append('\'');
        sb.append(", artifact='").append(artifact).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
