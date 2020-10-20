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
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.release.services.Utility;
import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;
import org.joda.time.DateTime;

import static dev.snowdrop.release.services.Utility.isStringNullOrBlank;

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
    @JsonIgnore
    private String summary;
    @JsonIgnore
    private List<String> fixVersions = new LinkedList<>();
    @JsonIgnore
    private String status;
    
    public Issue() {
    }
    
    public Issue(String key, String summary, Iterable<String> fixVersions, String status) {
        this.key = key;
        this.summary = summary;
        fixVersions.forEach(s -> this.fixVersions.add(s));
        this.status = status;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public List<String> getFixVersions() {
        return fixVersions;
    }
    
    public void setKey(String key) {
        if (isStringNullOrBlank(this.key)) {
            this.key = key;
        } else {
            if (!this.key.equals(key)) {
                throw new IllegalStateException("Issue already has a key: " + this.key);
            }
        }
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
    
    public static final String LINK_TYPE = "Blocks";
    @JsonIgnore
    private final List<Blocker> blockedBy = new LinkedList<>();
    @JsonIgnore
    private JiraRestClient restClient;
    
    public void setJiraClient(JiraRestClient jiraClient) {
        this.restClient = jiraClient;
    }
    
    public JiraRestClient getRestClient() {
        return restClient;
    }
    
    public Optional<String> getRevisit() {
        final var revisit = blockedBy.stream()
            .map(Blocker::getRevisit)
            .filter(Optional::isPresent)
            .map(o -> "- " + o.get())
            .collect(Collectors.joining("\n"));
        return revisit.isBlank() ? Optional.empty() : Optional.of(revisit);
    }
    
    public List<Blocker> getBlockedBy() {
        return blockedBy;
    }
    
    public void addBlocker(Blocker blocker) {
        blockedBy.add(blocker);
    }
    
    public Status computeStatus(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        return computeStatus(issue, item -> addBlocker(newBlockerIssue(item)));
    }
    
    public Status computeStatus(com.atlassian.jira.rest.client.api.domain.Issue issue, IssueVisitor visitor) {
        final var status = new Status();
        processLabels(issue);
        status.setBlockedLinksRatio(processLinks(issue, visitor));
        status.setBlockedTasksRatio(processTasks(issue, visitor));
        return status;
    }
    
    
    void processLabels(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        final var labels = issue.getLabels();
        labels.stream()
            .filter(l -> l.startsWith("im:"))
            .forEach(l -> {
                final var split = l.split(":");
                switch (split[1]) {
                    case "wait_release":
                        // format: im:wait_release:<product name with spaces escaped by _>[:<date in dd_MMM_YYYY format>]?
                        final var date = split.length == 4 ? unescape(split[3]) : null;
                        addBlocker(newBlockerRelease(unescape(split[2]), Optional.ofNullable(date)));
                        break;
                    case "wait_assignee":
                        // format: im:wait_assignee:<assignee>:<date in dd_MMM_YYYY format>
                        // we need to specify the assignee in the label in case the ticket gets re-assigned
                        addBlocker(newBlockerAssignee(issue, unescape(split[2]), unescape(split[3])));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown label: '" + l + "'");
                }
            });
    }
    
    RatioStatus processLinks(com.atlassian.jira.rest.client.api.domain.Issue issue, IssueVisitor visitor) {
        final var links = issue.getIssueLinks();
        return process(links, new IssueFilter<IssueLink>() {
            @Override
            public String getKey(IssueLink item) {
                return item.getTargetIssueKey();
            }
            
            @Override
            public boolean accept(IssueLink item) {
                final var type = item.getIssueLinkType();
                return type.getDirection() == IssueLinkType.Direction.INBOUND && type.getName().equals(LINK_TYPE);
            }
        }, visitor);
    }
    
    RatioStatus processTasks(com.atlassian.jira.rest.client.api.domain.Issue issue, IssueVisitor visitor) {
        final var tasks = issue.getSubtasks();
        return process(tasks, new IssueFilter<Subtask>() {
            @Override
            public String getKey(Subtask item) {
                return item.getIssueKey();
            }
            
            @Override
            public boolean accept(Subtask item) {
                return true;
            }
        }, visitor);
    }
    
    private interface IssueFilter<T> {
        String getKey(T item);
        
        boolean accept(T item);
    }
    
    @FunctionalInterface
    public interface IssueVisitor {
        void visit(com.atlassian.jira.rest.client.api.domain.Issue item);
    }
    
    protected RatioStatus process(Iterable<?> links, IssueFilter filter, IssueVisitor visitor) {
        final var result = new RatioStatus();
        if (links != null) {
            final var promises = new LinkedList<Promise<? extends com.atlassian.jira.rest.client.api.domain.Issue>>();
            links.forEach(l -> {
                if (filter.accept(l)) {
                    promises.add(restClient.getIssueClient().getIssue(filter.getKey(l)));
                }
            });
            
            result.setConsidered(promises.size());
            
            Promises.when(promises).claim().forEach(blocker -> {
                    final var status = blocker.getStatus();
                    // only add blocker if linked issue is not done
                    if (!status.getStatusCategory().getKey().equals("done")) {
                        visitor.visit(blocker);
                        result.incrementMatching();
                    }
                }
            );
        }
        return result;
    }
    
    private String unescape(String s) {
        return s.replaceAll("_", " ");
    }
    
    public Blocker newBlockerIssue(com.atlassian.jira.rest.client.api.domain.Issue blocker) {
        final var key = blocker.getKey();
        final String finalMsg;
        if (useExtendedStatus()) {
            finalMsg = "by " + key + " " + blocker.getIssueType().getName() + " [" + blocker.getStatus().getName() + "]" + ": "
                + blocker.getSummary();
        } else {
            finalMsg = "by " + key + " [" + blocker.getStatus().getName() + "]";
        }
        
        final var block = new Blocker(() -> finalMsg);
        final var updateDate = blocker.getUpdateDate();
        // if the blocker issue has been updated within the last week, mark it as needing revisit
        if (updateDate.isAfter(DateTime.now().minusDays(7))) {
            block.setRevisit(key + " updated last week");
        }
        return block;
    }
    
    public Blocker newBlockerRelease(String product, Optional<String> expectedDate) {
        var msg = "by " + product;
        String revisit = null;
        if (expectedDate.isPresent()) {
            final var dateAsString = expectedDate.get();
            msg += " expected on " + dateAsString;
            // if the product has been released, we need to revisit
            if (Utility.fromReadableDate(dateAsString).isBeforeNow()) {
                revisit = product + " should be released";
            }
        }
        String finalMsg = msg;
        final var blocker = new Blocker(() -> finalMsg);
        blocker.setRevisit(revisit);
        return blocker;
    }
    
    public Blocker newBlockerAssignee(com.atlassian.jira.rest.client.api.domain.Issue issue, String assigneeName, String since) {
        // check comments to see if assignee has commented since it was assigned to them
        final var assignedDate = Utility.fromReadableDate(since);
        String revisit = null;
        for (Comment comment : issue.getComments()) {
            final var author = comment.getAuthor();
            if (author != null && author.getName().equals(assigneeName) && comment.getCreationDate().isAfter(assignedDate)) {
                revisit = "assignee has commented";
            }
        }
        final var blocker = new Blocker(() -> "by " + assigneeName + " since " + since);
        blocker.setRevisit(revisit);
        return blocker;
    }
    
    
    protected boolean useExtendedStatus() {
        return false;
    }
}
