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
package dev.snowdrop.jira.atlassian.model;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.jira.atlassian.Utility;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class Issue {
    private static final String DEFAULT_JIRA_PROJECT = "ENTSBT";
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
