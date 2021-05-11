package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSReleaseFile {
    @JsonProperty
    private CPaaSRelease release;

    public CPaaSReleaseFile() {
    }

    public CPaaSRelease getRelease() {
        return release;
    }

    public void setRelease(CPaaSRelease release) {
        this.release = release;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSReleaseFile{");
        sb.append("release=").append(release);
        sb.append('}');
        return sb.toString();
    }

}
