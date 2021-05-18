package dev.snowdrop.release.model.cpaas;

import java.util.List;

public class ReleaseMustache {
    private boolean createAdvisory;
    private boolean securityAdvisory;
    private List<String> cve;
    private String release;
    private String previousRelease;
    private String securityImpact;

    public ReleaseMustache() {
    }

    public ReleaseMustache(boolean createAdvisory, boolean securityAdvisory, List<String> cveList, String securityImpact, String release, String previousRelease) {
        this.createAdvisory = createAdvisory;
        this.securityAdvisory = securityAdvisory;
        this.cve = cveList;
        this.securityImpact = securityImpact;
        this.release = release;
        this.previousRelease = previousRelease;
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

    public String getSecurityImpact() {
        return securityImpact;
    }

    public void setSecurityImpact(String securityImpact) {
        this.securityImpact = securityImpact;
    }

    public boolean isSecurityAdvisory() {
        return securityAdvisory;
    }

    public void setSecurityAdvisory(boolean securityAdvisory) {
        this.securityAdvisory = securityAdvisory;
    }
}
