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
package dev.snowdrop.release.services;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BulkOperationResult;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Page;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.Votes;
import com.atlassian.jira.rest.client.api.domain.Watchers;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import io.atlassian.util.concurrent.Promise;

import static dev.snowdrop.release.services.MockProjectRestClient.PROJECT;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class MockIssueRestClient implements IssueRestClient {
    static final String ISSUE_KEY = "ENTSBT-1234";
    
    @Override
    public Promise<BasicIssue> createIssue(IssueInput issue) {
        return null;
    }
    
    @Override
    public Promise<Void> updateIssue(String issueKey, IssueInput issue) {
        return null;
    }
    
    @Override
    public Promise<Iterable<CimProject>> getCreateIssueMetadata(@Nullable GetCreateIssueMetadataOptions options) {
        return null;
    }
    
    @Override
    public Promise<BulkOperationResult<BasicIssue>> createIssues(Collection<IssueInput> issues) {
        return null;
    }
    
    @Override
    public Promise<Page<IssueType>> getCreateIssueMetaProjectIssueTypes(@Nonnull String projectIdOrKey, @Nullable Long startAt, @Nullable Integer maxResults) {
        return null;
    }
    
    @Override
    public Promise<Page<CimFieldInfo>> getCreateIssueMetaFields(@Nonnull String projectIdOrKey, @Nonnull String issueTypeId, @Nullable Long startAt, @Nullable Integer maxResults) {
        return null;
    }
    
    @Override
    public Promise<Issue> getIssue(String issueKey) {
        if (issueKey.equals(ISSUE_KEY)) {
            return new MockPromise<>() {
                @Override
                public Issue claim() {
                    return new Issue("Fake issue", null, ISSUE_KEY, 1234L, PROJECT, null,
                        null, "Fake issue", null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null);
                }
            };
        }
        return null;
    }
    
    @Override
    public Promise<Issue> getIssue(String issueKey, Iterable<Expandos> expand) {
        return null;
    }
    
    @Override
    public Promise<Void> deleteIssue(String issueKey, boolean deleteSubtasks) {
        return null;
    }
    
    @Override
    public Promise<Watchers> getWatchers(URI watchersUri) {
        return null;
    }
    
    @Override
    public Promise<Votes> getVotes(URI votesUri) {
        return null;
    }
    
    @Override
    public Promise<Iterable<Transition>> getTransitions(URI transitionsUri) {
        return null;
    }
    
    @Override
    public Promise<Iterable<Transition>> getTransitions(Issue issue) {
        return null;
    }
    
    @Override
    public Promise<Void> transition(URI transitionsUri, TransitionInput transitionInput) {
        return null;
    }
    
    @Override
    public Promise<Void> transition(Issue issue, TransitionInput transitionInput) {
        return null;
    }
    
    @Override
    public Promise<Void> vote(URI votesUri) {
        return null;
    }
    
    @Override
    public Promise<Void> unvote(URI votesUri) {
        return null;
    }
    
    @Override
    public Promise<Void> watch(URI watchersUri) {
        return null;
    }
    
    @Override
    public Promise<Void> unwatch(URI watchersUri) {
        return null;
    }
    
    @Override
    public Promise<Void> addWatcher(URI watchersUri, String username) {
        return null;
    }
    
    @Override
    public Promise<Void> removeWatcher(URI watchersUri, String username) {
        return null;
    }
    
    @Override
    public Promise<Void> linkIssue(LinkIssuesInput linkIssuesInput) {
        return null;
    }
    
    @Override
    public Promise<Void> addAttachment(URI attachmentsUri, InputStream in, String filename) {
        return null;
    }
    
    @Override
    public Promise<Void> addAttachments(URI attachmentsUri, AttachmentInput... attachments) {
        return null;
    }
    
    @Override
    public Promise<Void> addAttachments(URI attachmentsUri, File... files) {
        return null;
    }
    
    @Override
    public Promise<Void> addComment(URI commentsUri, Comment comment) {
        return null;
    }
    
    @Override
    public Promise<InputStream> getAttachment(URI attachmentUri) {
        return null;
    }
    
    @Override
    public Promise<Void> addWorklog(URI worklogUri, WorklogInput worklogInput) {
        return null;
    }
}
