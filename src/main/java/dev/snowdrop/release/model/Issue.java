/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package dev.snowdrop.release.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.release.services.Utility;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class Issue {
    public static final String DEFAULT_JIRA_PROJECT = "ENTSBT";
    static final String TEST_JIRA_PROJECT = "SB";
    static final String TEST_ASSIGNEE = "snowdrop-test-user";
    static final String TEST_ISSUE_KEY = "SB-1611";
    private static final Long DEFAULT_ISSUE_TYPE_ID = 3L;
    @JsonProperty
    private String project;
    @JsonProperty
    private String key;
    @JsonProperty
    private Long issueTypeId;
    @JsonProperty
    private String assignee;
    
    public String getKey() {
        return key;
    }
    
    void setKey(String key) {
        this.key = key;
    }
    
    public Long getIssueTypeId() {
        return Objects.requireNonNullElse(issueTypeId, DEFAULT_ISSUE_TYPE_ID);
    }
    
    public String getProject() {
        return Utility.isStringNullOrBlank(project) ? DEFAULT_JIRA_PROJECT : project;
    }
    
    void useTestMode() {
        this.project = Issue.TEST_JIRA_PROJECT;
        this.issueTypeId = DEFAULT_ISSUE_TYPE_ID;
        this.assignee = TEST_ASSIGNEE;
    }
    
    public Optional<String> getAssignee() {
        return Utility.isStringNullOrBlank(assignee) ? Optional.empty() : Optional.of(assignee);
    }
    
    public List<String> validate(JiraRestClient restClient) {
        final var errors = new LinkedList<String>();
        if (Utility.isStringNullOrBlank(project)) {
            errors.add("project must be specified");
        }
        if (!Utility.isStringNullOrBlank(key)) {
            try {
                final var issue = restClient.getIssueClient().getIssue(key).claim();
                if (!issue.getProject().getKey().equals(project)) {
                    errors.add(String.format("invalid issue key: %s doesn't match project %s", key, project));
                }
            } catch (Exception e) {
                errors.add(String.format("invalid issue key: %s", key));
            }
        }
        getAssignee().ifPresent(assignee ->
            {
                try {
                    restClient.getUserClient().getUser(assignee).claim();
                } catch (Exception e) {
                    errors.add(String.format("invalid assignee for project '%s': %s", project, assignee));
                }
            }
        );
        try {
            final var issueTypeId = getIssueTypeId();
            var p = restClient.getProjectClient().getProject(project).claim();
            for (IssueType issueType : p.getIssueTypes()) {
                if (issueType.getId().equals(issueTypeId)) {
                    return errors;
                }
            }
            errors.add(String.format("invalid issue type id '%d'", issueTypeId));
        } catch (Exception e) {
            errors.add(String.format("invalid project '%s': %s", project, e.getLocalizedMessage()));
        }
        return errors;
    }
    
    @Override
    public String toString() {
        return "{" +
            "project='" + project + '\'' +
            ", key='" + key + '\'' +
            ", issueTypeId=" + issueTypeId +
            ", assignee='" + assignee + '\'' +
            '}';
    }
}
