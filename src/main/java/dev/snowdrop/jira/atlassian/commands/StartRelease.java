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
package dev.snowdrop.jira.atlassian.commands;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import dev.snowdrop.jira.atlassian.ReleaseService;
import dev.snowdrop.jira.atlassian.Utility;
import dev.snowdrop.jira.atlassian.model.Release;

import java.util.concurrent.Callable;

import static dev.snowdrop.jira.atlassian.ReleaseService.RELEASE_TICKET_TEMPLATE;
import static dev.snowdrop.jira.atlassian.Utility.restClient;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class StartRelease implements Callable<BasicIssue> {
	private String gitRef;

	public StartRelease(String gitRef) {
		this.gitRef = gitRef;
	}

	@Override
	public BasicIssue call() throws Exception {
		Release release = Release.createFromGitRef(gitRef);

		BasicIssue issue;
		// first check if we already have a release ticket, in which case we don't need to clone the template
		final String releaseTicket = release.getJiraKey();
		if (!Utility.isStringNullOrBlank(releaseTicket)) {
			final IssueRestClient cl = restClient.getIssueClient();
			try {
				issue = cl.getIssue(releaseTicket).claim();
				System.out.printf("Release ticket %s already exists, skipping cloning step", releaseTicket);
			} catch (Exception e) {
				// if we got an exception, assume that it's because we didn't find the ticket
				issue = ReleaseService.clone(release, RELEASE_TICKET_TEMPLATE);
			}
		} else {
			// no release ticket was specified, clone
			issue = ReleaseService.clone(release, RELEASE_TICKET_TEMPLATE);
		}
		ReleaseService.createComponentRequests(release);
		return issue;
	}
}
