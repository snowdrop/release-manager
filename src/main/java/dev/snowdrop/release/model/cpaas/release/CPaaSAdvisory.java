package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSAdvisory {
    @JsonProperty
    private String name;
    @JsonProperty("release-name")
    private String releaseName;
    @JsonProperty("package-maintainer")
    private String packageMaintainer;
    @JsonProperty("manager-contact")
    private String managerContact;
    @JsonProperty("qa-owner")
    private String qaOwner;
    @JsonProperty("text-only")
    private Boolean textOnly;
    @JsonProperty("advisory-type")
    private String advisoryType;
    @JsonProperty("security-impact")
    private String securityImpact;
    @JsonProperty
    private String synopsis;
    @JsonProperty
    private String description;
    @JsonProperty
    private String topic;
    @JsonProperty
    private String solution;

    public CPaaSAdvisory() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getPackageMaintainer() {
        return packageMaintainer;
    }

    public void setPackageMaintainer(String packageMaintainer) {
        this.packageMaintainer = packageMaintainer;
    }

    public String getManagerContact() {
        return managerContact;
    }

    public void setManagerContact(String managerContact) {
        this.managerContact = managerContact;
    }

    public String getQaOwner() {
        return qaOwner;
    }

    public void setQaOwner(String qaOwner) {
        this.qaOwner = qaOwner;
    }

    public Boolean getTextOnly() {
        return textOnly;
    }

    public void setTextOnly(Boolean textOnly) {
        this.textOnly = textOnly;
    }

    public String getAdvisoryType() {
        return advisoryType;
    }

    public void setAdvisoryType(String advisoryType) {
        this.advisoryType = advisoryType;
    }

    public String getSecurityImpact() {
        return securityImpact;
    }

    public void setSecurityImpact(String securityImpact) {
        this.securityImpact = securityImpact;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSAdvisory{");
        sb.append("name='").append(name).append('\'');
        sb.append(", releaseName='").append(releaseName).append('\'');
        sb.append(", packageMaintainer='").append(packageMaintainer).append('\'');
        sb.append(", managerContact='").append(managerContact).append('\'');
        sb.append(", qaOwner='").append(qaOwner).append('\'');
        sb.append(", textOnly=").append(textOnly);
        sb.append(", advisoryType='").append(advisoryType).append('\'');
        sb.append(", securityImpact='").append(securityImpact).append('\'');
        sb.append(", synopsis='").append(synopsis).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", solution='").append(solution).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
