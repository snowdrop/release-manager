package dev.snowdrop.release.model.cpaas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseMustache {
    private boolean createAdvisory;
    private boolean securityAdvisory;
    private List<String> cveList = new ArrayList<>(0);
    private String release;
    private String previousRelease;
    private String securityImpact;

    public ReleaseMustache(boolean createAdvisory, boolean securityAdvisory, List<String> cveList, String securityImpact, String release, String previousRelease) {
        this.createAdvisory = createAdvisory;
        this.securityAdvisory = securityAdvisory;
        this.cveList = cveList;
        this.securityImpact = securityImpact;
        this.release = release;
        this.previousRelease = previousRelease;
    }

    public List<String> getCveList() {
        return cveList;
    }

    public void setCve(List<String> cveList) {
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

    public String getCves() {
        return cveList.stream().collect(Collectors.joining("\n\n","  * ",""));
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

    public boolean isCreateAdvisory() {
        return createAdvisory;
    }

    public void setCreateAdvisory(boolean createAdvisory) {
        this.createAdvisory = createAdvisory;
    }
}
