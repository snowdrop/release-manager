package dev.snowdrop.release.model.cpaas.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CPaaSProductFile {
    @JsonProperty
    private CPaaSProduct product;

    public CPaaSProductFile() {
    }

    public CPaaSProduct getProduct() {
        return product;
    }

    public void setProduct(CPaaSProduct product) {
        this.product = product;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSProductFile{");
        sb.append("product=").append(product);
        sb.append('}');
        return sb.toString();
    }

}
