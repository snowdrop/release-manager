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
package dev.snowdrop.jira.atlassian;

import java.net.URI;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import picocli.CommandLine;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@ApplicationScoped
public class JiraClientConfiguration {
	@Produces
	@ApplicationScoped
	public JiraRestClient client(CommandLine.ParseResult parseResult) {
		final var user = parseResult.matchedOption('u').getValue().toString();
		final var password = parseResult.matchedOption('p').getValue().toString();
		final var jiraServerUri = parseResult.hasMatchedOption("url") ? parseResult.matchedOption("url").getValue().toString() : null;
		AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		return factory.createWithBasicHttpAuthentication(URI.create(Objects.requireNonNullElse(jiraServerUri, Utility.JIRA_SERVER)), user, password);
	}
}
