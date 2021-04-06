package dev.snowdrop.release.model.buildconfig;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildConfig {

    @JsonProperty
    private Product product;
    @JsonProperty
    private String version;
    @JsonProperty
    private String milestone;
    @JsonProperty
    private String group;
    @JsonIgnore
    private String defaultBuildParameters;
    @JsonProperty
    private List<Build> builds;

    @JsonIgnore
    private List<String> outputPrefixes;
    @JsonIgnore
    private List<String> flow;
    @JsonIgnore
    private List<String> addons;

    @JsonIgnore
    private Map<String,Object> additionalProperties = new HashMap<>();

    @JsonProperty("vertxSpringBootVersion")
    private String xxx;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BuildConfig{");
        sb.append("product=").append(product);
        sb.append(", version='").append(version).append('\'');
        sb.append(", milestone='").append(milestone).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", defaultBuildParameters='").append(defaultBuildParameters).append('\'');
        sb.append(", builds=").append(builds);
        sb.append(", outputPrefixes=").append(outputPrefixes);
        sb.append(", flow=").append(flow);
        sb.append(", addons=").append(addons);
        sb.append(", additionalProperties=").append(additionalProperties);
        sb.append(", xxx=").append(xxx);
        sb.append('}');
        return sb.toString();
    }

    @JsonAnySetter
    public void setUnmapped(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getUnmapped() {
        return additionalProperties;
    }

    public List<String> getOutputPrefixes() {
        return outputPrefixes;
    }

    public void setOutputPrefixes(List<String> outputPrefixes) {
        this.outputPrefixes = outputPrefixes;
    }

    public List<String> getFlow() {
        return flow;
    }

    public void setFlow(List<String> flow) {
        this.flow = flow;
    }

    public List<String> getAddons() {
        return addons;
    }

    public void setAddons(List<String> addons) {
        this.addons = addons;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDefaultBuildParameters() {
        return defaultBuildParameters;
    }

    public void setDefaultBuildParameters(String defaultBuildParameters) {
        this.defaultBuildParameters = defaultBuildParameters;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }
}
