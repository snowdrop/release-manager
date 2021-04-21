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
package dev.snowdrop.release.services;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.snowdrop.release.exception.JiraGavDescriptionNotParsableException;
import dev.snowdrop.release.model.Issue;
import dev.snowdrop.release.model.Release;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
public class IssueServiceTest {

    private static final YAMLMapper MAPPER = new YAMLMapper();

    @Inject
    JiraIssueFactory factory;


    @Inject
    IssueService service;

    private static JiraRestClient client(final String jiraUser, final String jiraPw) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        return factory.createWithBasicHttpAuthentication(URI.create(Utility.JIRA_SERVER), jiraUser, jiraPw);
    }

    @Test
    public void getIssue() throws Throwable {
        BasicIssue testIssue = service.getIssue(Issue.TEST_ISSUE_KEY);
        assertEquals(Issue.TEST_ISSUE_KEY, testIssue.getKey());
    }

    @Test
    public void createAndDeleteIssue() throws Throwable {
        InputStream releaseIS = HelperFunctions.getResourceAsStream("release_template_int_tests.yml");
        Release releaseObj = MAPPER.readValue(releaseIS, Release.class);
//        service.clone(releaseObj, Issue.TEST_ISSUE_KEY,);
//        final var cl = service.restClient.getIssueClient();
//        try {
//            factory.extractGAVArrayForProduct("");
//            assertFalse(true);
//        } catch (JiraGavDescriptionNotParsableException ex) {
//            assertFalse(false);
//        }
        BasicIssue issueKey = service.clone(releaseObj,Issue.TEST_ISSUE_KEY,List.of());
//        createIssue(releaseObj.getComponents().get(0),
        assertTrue(issueKey != null && issueKey.getKey() != null && issueKey.getKey().length() > 0, issueKey.getKey());
        try {
            service.deleteIssues(List.of(issueKey.getKey()));
//        IssueInputBuilder iib = new IssueInputBuilder();
//        iib.setProjectKey(Issue.TEST_JIRA_PROJECT);
//        iib.setDescription("Test issue created from Unit Tests. Should be deleted automatically.");
//        iib.setSummary("DELETE: Test issue created from Unit Tests");
//        iib.setIssueTypeId(Issue.DEFAULT_ISSUE_TYPE_ID);
//        BasicIssue testIssue = service.createIssue().claim();
//        cl.deleteIssue(testIssue.getKey(), true).claim();
            assertTrue(true);
        } catch (Throwable e) {
            fail(e);
        }
    }

}