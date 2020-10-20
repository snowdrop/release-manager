/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.snowdrop.release.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.snowdrop.release.services.Utility;
import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;
import org.joda.time.DateTime;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public abstract class Blockable {
    public static final String LINK_TYPE = "Blocks";
    @JsonIgnore
    private final List<Blocker> blockedBy = new LinkedList<>();
    @JsonIgnore
    private JiraRestClient restClient;
    
    protected abstract boolean useExtendedStatus();
    
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
    
    public void processLabels(Issue issue) {
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
    
    public RatioStatus processLinks(Issue issue) {
        final var links = issue.getIssueLinks();
        return process(links, new IssueIdentifier<IssueLink>() {
            @Override
            public String getKey(IssueLink item) {
                return item.getTargetIssueKey();
            }
            
            @Override
            public boolean match(IssueLink item) {
                final var type = item.getIssueLinkType();
                return type.getDirection() == IssueLinkType.Direction.INBOUND && type.getName().equals(LINK_TYPE);
            }
        });
    }
    
    public RatioStatus processTasks(Issue issue) {
        final var tasks = issue.getSubtasks();
        return process(tasks, new IssueIdentifier<Subtask>() {
            @Override
            public String getKey(Subtask item) {
                return item.getIssueKey();
            }
            
            @Override
            public boolean match(Subtask item) {
                return true;
            }
        });
    }
    
    private interface IssueIdentifier<T> {
        String getKey(T item);
        
        boolean match(T item);
    }
    
    private RatioStatus process(Iterable<?> links, IssueIdentifier identifier) {
        final var result = new RatioStatus();
        if (links != null) {
            final var promises = new LinkedList<Promise<? extends Issue>>();
            links.forEach(l -> {
                if (identifier.match(l)) {
                    promises.add(restClient.getIssueClient().getIssue(identifier.getKey(l)));
                }
            });
            
            result.setConsidered(promises.size());
            
            Promises.when(promises).claim().forEach(blocker -> {
                    final var status = blocker.getStatus();
                    // only add blocker if linked issue is not done
                    if (!status.getStatusCategory().getKey().equals("done")) {
                        addBlocker(newBlockerIssue(blocker));
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
    
    public Blocker newBlockerIssue(Issue blocker) {
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
    
    public Blocker newBlockerAssignee(Issue issue, String assigneeName, String since) {
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
    
}
