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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.net.URI;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@ApplicationScoped
@TestProfile(TestProfiles.IntegrationTests.class)
public class JiraClientConfigurationProperties {

    @Produces
    @ApplicationScoped
    public JiraRestClient client() {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient jiraRestClient;
        final String pat = ConfigProvider.getConfig().getValue("jboss.jira.pat", String.class);
        if (pat != null) {
            JiraBearerHttpAuthenticationHandler handler = new JiraBearerHttpAuthenticationHandler(pat);
            jiraRestClient = factory.createWithAuthenticationHandler(URI.create(Utility.JIRA_SERVER), handler);
        } else {
            final String user = ConfigProvider.getConfig().getValue("jboss.jira.user", String.class);
            final String password = ConfigProvider.getConfig().getValue("jboss.jira.password", String.class);
            jiraRestClient = factory.createWithBasicHttpAuthentication(URI.create(Utility.JIRA_SERVER), user, password);
        }
        return jiraRestClient;
    }
}
