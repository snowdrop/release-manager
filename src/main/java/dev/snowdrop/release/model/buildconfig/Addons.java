package dev.snowdrop.release.model.buildconfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Addons {
    @JsonProperty("notYetAlignedFromDependencyTree")
    private String notYetAlignedFromDependencyTree;

    public String getNotYetAlignedFromDependencyTree() {
        return notYetAlignedFromDependencyTree;
    }

    public void setNotYetAlignedFromDependencyTree(String notYetAlignedFromDependencyTree) {
        this.notYetAlignedFromDependencyTree = notYetAlignedFromDependencyTree;
    }
}
