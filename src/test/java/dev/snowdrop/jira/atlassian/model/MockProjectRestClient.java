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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import io.atlassian.util.concurrent.Promise;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class MockProjectRestClient implements ProjectRestClient {
	public static final String ENTSBT = "ENTSBT";
	public static final String EAPSUP = "EAPSUP";
	private static final Set<String> KNOWN_PROJECTS = Set.of(ENTSBT, EAPSUP, "KEYCLOAK", "RESTEASY", "ENTMQCL", "JWS",
		"TRACING", "JBTM", "ENTVTX", "JDG", "SB");
	private static URI uri;

	static {
		try {
			uri = new URI("urn:foo:bar");
		} catch (URISyntaxException e) {
			// ignore
		}
	}

	private static final List<IssueType> issueTypes = List.of(
			new IssueType(uri, 1L, "type 1", false, "", uri),
			new IssueType(uri, 2L, "type 2", false, "", uri),
			new IssueType(uri, 3L, "type 3", false, "", uri)
	);
	public static final Project PROJECT = new Project(Collections.emptyList(), uri, ENTSBT, 123L, ENTSBT,
			"", null, uri, Collections.emptyList(), Collections.emptyList(),
			new OptionalIterable<>(issueTypes), Collections.emptyList());


	@Override
	public Promise<Project> getProject(String key) {
		return new MockPromise<>() {
			@Override
			public Project claim() {
				if (EAPSUP.equals(key)) {
					return new Project(Collections.emptyList(), uri, EAPSUP, 456L, EAPSUP,
							"", null, uri, Collections.emptyList(), Collections.emptyList(),
							new OptionalIterable<>(List.of(new IssueType(uri, 10600L, "Question", false, "", uri))),
							Collections.emptyList());
				} else {
					if (KNOWN_PROJECTS.contains(key)) {
						return PROJECT;
					}
				}
				throw new RuntimeException("Unknown project " + key);
			}
		};
	}

	@Override
	public Promise<Project> getProject(URI projectUri) {
		return null;
	}

	@Override
	public Promise<Iterable<BasicProject>> getAllProjects() {
		return null;
	}
}
