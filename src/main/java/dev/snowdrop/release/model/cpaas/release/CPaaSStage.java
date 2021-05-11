package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSStage {
    @JsonProperty
    private String name;
    @JsonProperty
    private Boolean enabled;

    public CPaaSStage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSStage{");
        sb.append("name='").append(name).append('\'');
        sb.append(", enabled=").append(enabled);
        sb.append('}');
        return sb.toString();
    }
}
