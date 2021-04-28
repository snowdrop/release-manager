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

import com.atlassian.jira.rest.client.api.*;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Singleton
@Alternative
public class MockJiraRestClient implements JiraRestClient {
    @Override
    public IssueRestClient getIssueClient() {
        return new MockIssueRestClient();
    }

    @Override
    public SessionRestClient getSessionClient() {
        return null;
    }

    @Override
    public UserRestClient getUserClient() {
        return new MockUserRestClient();
    }

    @Override
    public GroupRestClient getGroupClient() {
        return null;
    }

    @Override
    public ProjectRestClient getProjectClient() {
        return new MockProjectRestClient();
    }

    @Override
    public ComponentRestClient getComponentClient() {
        return null;
    }

    @Override
    public MetadataRestClient getMetadataClient() {
        return null;
    }

    @Override
    public SearchRestClient getSearchClient() {
        return null;
    }

    @Override
    public VersionRestClient getVersionRestClient() {
        return null;
    }

    @Override
    public ProjectRolesRestClient getProjectRolesRestClient() {
        return null;
    }

    @Override
    public AuditRestClient getAuditRestClient() {
        return null;
    }

    @Override
    public MyPermissionsRestClient getMyPermissionsRestClient() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
