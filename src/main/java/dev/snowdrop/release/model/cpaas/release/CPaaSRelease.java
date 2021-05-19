package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CPaaSRelease {
    @JsonProperty
    private String name;
    @JsonProperty
    private List<CPaaSPipelines> pipelines;
    @JsonProperty
    private List<CPaaSTool> tools;

    public CPaaSRelease() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CPaaSPipelines> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<CPaaSPipelines> pipelines) {
        this.pipelines = pipelines;
    }

    public List<CPaaSTool> getTools() {
        return tools;
    }

    public void setTools(List<CPaaSTool> tools) {
        this.tools = tools;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSRelease{");
        sb.append("name='").append(name).append('\'');
        sb.append(", pipelines=").append(pipelines);
        sb.append(", tools=").append(tools);
        sb.append('}');
        return sb.toString();
    }
}
