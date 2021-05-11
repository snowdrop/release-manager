package dev.snowdrop.release.model.cpaas;

import java.util.List;

public class ReleaseMustache {
    private Boolean isErCr;
    private Boolean isSecurityAdvisory;
    private List<String> cveList;
    private String release;
    private String previousRelease;

    public ReleaseMustache() {
    }

    public ReleaseMustache(Boolean isErCr, Boolean isSecurityAdvisory, List<String> cveList, String release, String previousRelease) {
        this.isErCr = isErCr;
        this.isSecurityAdvisory = isSecurityAdvisory;
        this.cveList = cveList;
        this.release = release;
        this.previousRelease = previousRelease;
    }

    public Boolean getErCr() {
        return isErCr;
    }

    public void setErCr(Boolean erCr) {
        isErCr = erCr;
    }

    public Boolean getSecurityAdvisory() {
        return isSecurityAdvisory;
    }

    public void setSecurityAdvisory(Boolean securityAdvisory) {
        isSecurityAdvisory = securityAdvisory;
    }

    public List<String> getCveList() {
        return cveList;
    }

    public void setCveList(List<String> cveList) {
        this.cveList = cveList;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getPreviousRelease() {
        return previousRelease;
    }

    public void setPreviousRelease(String previousRelease) {
        this.previousRelease = previousRelease;
    }
}
