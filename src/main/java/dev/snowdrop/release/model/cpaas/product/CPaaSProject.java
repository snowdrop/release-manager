package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.json.JsonObject;
import java.util.List;

public class CPaaSProject {
    @JsonProperty
    private String name;

    @JsonProperty
    private CPaaSMidstream midstream;

    @JsonProperty
    private List<String> owners;

    @JsonProperty
    private List<String> advisories;

    @JsonProperty()
    private List<CPaaSComponent> components;

    public CPaaSProject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CPaaSMidstream getMidstream() {
        return midstream;
    }

    public void setMidstream(CPaaSMidstream midstream) {
        this.midstream = midstream;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<String> getAdvisories() {
        return advisories;
    }

    public void setAdvisories(List<String> advisories) {
        this.advisories = advisories;
    }

    public List<CPaaSComponent> getComponents() {
        return components;
    }

    public void setComponents(List<CPaaSComponent> components) {
        this.components = components;
    }
}
