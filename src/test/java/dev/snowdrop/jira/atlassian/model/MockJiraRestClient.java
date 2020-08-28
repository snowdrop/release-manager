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

import java.io.IOException;

import javax.inject.Singleton;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import io.quarkus.test.Mock;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Mock
@Singleton
public class MockJiraRestClient implements JiraRestClient {
	@Override
	public IssueRestClient getIssueClient() {
		return null;
	}

	@Override
	public SessionRestClient getSessionClient() {
		return null;
	}

	@Override
	public UserRestClient getUserClient() {
		return null;
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
