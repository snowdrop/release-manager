package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSShip {
    @JsonProperty
    private CPaaSArtifact artifact;
    @JsonProperty
    private String as;

    public CPaaSShip() {
    }

    public CPaaSArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(CPaaSArtifact artifact) {
        this.artifact = artifact;
    }

    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSShip{");
        sb.append("artifact=").append(artifact);
        sb.append(", as=").append(as);
        sb.append('}');
        return sb.toString();
    }
}
