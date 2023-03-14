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

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.snowdrop.release.model.Issue;
import dev.snowdrop.release.model.POM;
import dev.snowdrop.release.model.Release;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.IntegrationTests.class)
public class IssueServiceTest {

    private static final YAMLMapper MAPPER = new YAMLMapper();

    @Inject
    IssueService service;

    @Test
    public void getIssue() throws Throwable {
        BasicIssue testIssue = service.getIssue(Issue.TEST_ISSUE_KEY);
        assertEquals(Issue.TEST_ISSUE_KEY, testIssue.getKey());
    }

    @Test
    public void cloneAndDeleteIssue() throws Throwable {
        InputStream releaseIS = HelperFunctions.getResourceAsStream("release_template_int_tests.yml");
        Release releaseObj = MAPPER.readValue(releaseIS, Release.class);
        releaseObj.setPom(POM.createFrom(HelperFunctions.getResourceAsStream("pom.xml")));
        BasicIssue issueKey = service.clone(releaseObj,Issue.TEST_ISSUE_KEY,List.of());
        assertTrue(issueKey != null && issueKey.getKey() != null && issueKey.getKey().length() > 0, issueKey.getKey());
        try {
            service.deleteIssues(List.of(issueKey.getKey()));
            assertFalse(true);
        } catch (Throwable e) {
            assertFalse(false, e.getMessage());
            assertTrue(e.getMessage().contains("You do not have permission to delete issues in this project."));
            service.changeIssueState(issueKey.getKey(), 2);
        }
    }

}