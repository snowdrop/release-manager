package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSMidstream {
    @JsonProperty
    private String project;
    @JsonProperty
    private String type;
    @JsonProperty
    private String branch;

    public CPaaSMidstream() {
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSMidstream{");
        sb.append("project='").append(project).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", branch='").append(branch).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
