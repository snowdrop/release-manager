package dev.snowdrop.release.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.release.services.Utility;

import static dev.snowdrop.release.services.Utility.isStringNullOrBlank;

public class Release extends Blockable {
    public static final String RELEASE_SUFFIX = ".RELEASE";
    @JsonProperty
    private String version;
    @JsonProperty
    private Issue issue;
    @JsonProperty
    private Schedule schedule;
    @JsonProperty
    private List<Component> components;
    @JsonIgnore
    private String gitRef;
    @JsonIgnore
    private POM pom;
    
    public String getProjectKey() {
        return issue.getProject();
    }
    
    public String getLongVersionName() {
        return "[Spring Boot " + getVersion() + "] Release steps CR [" + schedule.getFormattedReleaseDate() + "]";
    }
    
    public String getJiraKey() {
        return issue.getKey();
    }
    
    /**
     * Associates this Release to the ticket identified by the specified key. Note that if the JIRA key is already set
     * for this Release, this method won't do anything because the source of truth is assumed to be the YAML file.
     *
     * @param key the ticket identifier to which this Release should be associated
     */
    public void setJiraKey(String key) {
        issue.setKey(key);
    }
    
    public String getVersion() {
        return version;
    }
    
    public List<Component> getComponents() {
        if (components != null) {
            // make sure that the parent is properly set, could probably be optimized if needed
            components.forEach(c -> c.setParent(this));
            return components;
        }
        return Collections.emptyList();
    }
    
    public Schedule getSchedule() {
        return schedule;
    }
    
    public String getGitRef() {
        return gitRef;
    }
    
    public void setGitRef(String gitRef) {
        this.gitRef = gitRef;
    }
    
    public POM getPOM() {
        return pom;
    }
    
    public void setPom(POM pom) {
        this.pom = pom;
    }
    
    /**
     * Changes the release definition to use the {@link Issue#TEST_JIRA_PROJECT} project for all requests instead of the
     * specified ones so that we can check a test release without spamming projects.
     *
     * @param test whether or not the release should be set to test mode
     */
    public void setTest(boolean test) {
        if (test) {
            issue.setKey(Issue.TEST_ISSUE_KEY); // to avoid the cloning process
            issue.useTestMode();
            components.forEach(c -> {
                var issue = c.getJira();
                if (issue != null) {
                    issue.useTestMode();
                }
                issue = c.getProductIssue();
                if (issue != null) {
                    issue.useTestMode();
                }
            });
        }
    }
    
    public List<String> validate(boolean skipProductRequests) {
        final List<String> errors = new LinkedList<>();
        
        // validate version
        if (isStringNullOrBlank(version)) {
            errors.add("missing version");
        } else {
            if (pom == null) {
                errors.add("no associated POM");
            } else {
                final int suffix = version.indexOf(RELEASE_SUFFIX);
                final int len = suffix > 0 ? version.length() - suffix : version.length();
                final var expectedVersion = pom.getVersion();
                if (!this.version.regionMatches(0, expectedVersion, 0, len)) {
                    errors.add(String.format("'%s' release version doesn't match '%s' version in associated POM", this.version,
                        expectedVersion));
                }
            }
        }
        
        // validate schedule
        if (schedule == null) {
            errors.add("missing schedule");
        } else {
            if (isStringNullOrBlank(schedule.getReleaseDate())) {
                errors.add("missing release date");
            }
            try {
                schedule.getFormattedReleaseDate();
            } catch (Exception e) {
                errors.add("invalid release ISO8601 date: " + e.getMessage());
            }
            
            if (isStringNullOrBlank(schedule.getEOLDate())) {
                errors.add("missing EOL date");
            }
            try {
                schedule.getFormattedEOLDate();
            } catch (Exception e) {
                errors.add("invalid EOL ISO8601 date: " + e.getMessage());
            }
        }
    
        // validate components
        if (!skipProductRequests) {
            final var components = getComponents();
            components.parallelStream().forEach(c -> errors.addAll(c.validate(getRestClient())));
        }
    
        return errors;
    }
    
    public void computeStatus() {
        final var key = getJiraKey();
        if (!Utility.isStringNullOrBlank(key)) {
            final var issue = getRestClient().getIssueClient().getIssue(key).claim();
            processLabels(issue);
            processLinks(issue);
        }
    }
}
