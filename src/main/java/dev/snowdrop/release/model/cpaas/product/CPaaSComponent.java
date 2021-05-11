package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CPaaSComponent {
    @JsonProperty
    private String type;
    @JsonProperty
    private String name;
    @JsonProperty("display-name")
    private String displayName;
    @JsonProperty
    private String description;
    @JsonProperty
    private String summary;
    @JsonProperty
    private Integer priority;
    @JsonProperty()
    private List<CPaaSBuild> builds;

    public CPaaSComponent() {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<CPaaSBuild> getBuilds() {
        return builds;
    }

    public void setBuilds(List<CPaaSBuild> builds) {
        this.builds = builds;
    }
}
