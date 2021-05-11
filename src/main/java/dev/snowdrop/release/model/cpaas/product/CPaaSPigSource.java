package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSPigSource {
    @JsonProperty
    private String repo;
    @JsonProperty
    private String ref;
    @JsonProperty
    private String root;

    public CPaaSPigSource() {
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
