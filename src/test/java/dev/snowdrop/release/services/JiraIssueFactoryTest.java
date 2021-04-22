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

import dev.snowdrop.release.exception.JiraGavDescriptionNotParsableException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.CoreTags.class)
public class JiraIssueFactoryTest {

    @Inject
    JiraIssueFactory factory;

    @Test
    public void emptyDescriptionShouldRaiseSpecificException() throws Throwable {
        try {
            factory.extractGAVArrayForProduct("");
            assertFalse(true);
        } catch (JiraGavDescriptionNotParsableException ex) {
            assertFalse(false);
        }
    }

    @Test
    public void descriptionWithOnlySplitterShouldRaiseSpecificException() {
        try {
            factory.extractGAVArrayForProduct("===");
            assertFalse(true);
        } catch (JiraGavDescriptionNotParsableException ex) {
            assertFalse(false);
        }
    }

    @Test
    public void checkProductResult() throws Throwable {
        final String jiraDescription = HelperFunctions.getStreamContents(HelperFunctions.getResourceAsStream("jira_description_eap.txt"));
        try {
            factory.extractGAVArrayForProduct(jiraDescription);
            Map<String, String> gavMap = factory.getInfoForProductGroup(0);
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV).equals("OrgHibernateHibernateCore")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION).equals("5.3.20.Final-redhat-00001")) {
                assertTrue(false,"<"+gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION)+">");
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION).equals("5.3.20.Final")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION));
            }
            gavMap = factory.getInfoForProductGroup(1);
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV).equals("OrgHibernateHibernateEntitymanager")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION).equals("5.3.20.Final-redhat-00001")) {
                assertTrue(false,gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION).equals("5.3.20.Final")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION));
            }
            gavMap = factory.getInfoForProductGroup(2);
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV).equals("OrgHibernateValidatorHibernateValidator")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION).equals("6.0.21.Final-redhat-00001")) {
                assertTrue(false,gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION).equals("6.0.21.Final")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION));
            }
            gavMap = factory.getInfoForProductGroup(6);
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV).equals("OrgJbossResteasyResteasyJaxrs")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION).equals("3.11.3.Final-redhat-00001")) {
                assertTrue(false,gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION));
            }
            if (!gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION).equals("3.11.3.Final")) {
                assertTrue(false, gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION));
            }
            assertTrue(true);
        } catch (JiraGavDescriptionNotParsableException ex) {
            assertTrue(false);
        }
    }

}