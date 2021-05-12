package dev.snowdrop.release.model.cpaas;

import dev.snowdrop.release.model.CVE;
import dev.snowdrop.release.services.CVEService;

import javax.inject.Inject;
import java.util.List;

public class ReleaseMustache {
    private Boolean isErCr;
    private Boolean isSecurityAdvisory;
    private List<String> cve;
    private String release;
    private String previousRelease;

    @Inject
    CVEService cveService;

    public ReleaseMustache() {
    }

    public ReleaseMustache(Boolean isErCr, Boolean isSecurityAdvisory, List<String> cve, String release, String previousRelease) {
        this.isErCr = isErCr;
        this.isSecurityAdvisory = isSecurityAdvisory;
        this.cve = cve;
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

    public List<String> getCve() {
        return cve;
    }

    public void setCve(List<String> cve) {
        this.cve = cve;
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

    public String getCveList() {
        StringBuffer cveSb = new StringBuffer();
        cve.forEach(cveItem -> {
            cveSb.append("  * ").append(cveItem).append("\n");
        });
        return cveSb.toString();
    }

}
