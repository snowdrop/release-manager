package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CPaaSProduct {
    @JsonProperty
    private String name;

    @JsonProperty("short-name")
    private String shortName;

    @JsonProperty("product-page")
    private String productPage;

    @JsonProperty
    private CPaaSRelease release;

    @JsonProperty("fail-on-any")
    private Boolean failOnAny;

    @JsonProperty()
    private List<CPaaSProject> projects;

    public CPaaSProduct() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getProductPage() {
        return productPage;
    }

    public void setProductPage(String productPage) {
        this.productPage = productPage;
    }

    public CPaaSRelease getRelease() {
        return release;
    }

    public void setRelease(CPaaSRelease release) {
        this.release = release;
    }

    public Boolean getFailOnAny() {
        return failOnAny;
    }

    public void setFailOnAny(Boolean failOnAny) {
        this.failOnAny = failOnAny;
    }

    public List<CPaaSProject> getProjects() {
        return projects;
    }

    public void setProjects(List<CPaaSProject> projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSProduct{");
        sb.append("name='").append(name).append('\'');
        sb.append(", shortName='").append(shortName).append('\'');
        sb.append(", productPage='").append(productPage).append('\'');
        sb.append(", release=").append(release);
        sb.append(", failOnAny=").append(failOnAny);
        sb.append(", projects=").append(projects);
        sb.append('}');
        return sb.toString();
    }
}
