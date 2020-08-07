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

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import dev.snowdrop.jira.atlassian.ReleaseService;
import dev.snowdrop.jira.atlassian.Utility;
import dev.snowdrop.jira.atlassian.model.Release;

import java.util.concurrent.Callable;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class CloneIssue implements Callable<BasicIssue> {
	private String gitRef;
	private String toCloneFrom;

	public CloneIssue(String gitRef, String toCloneFrom) {
		this.gitRef = gitRef;
		this.toCloneFrom = Utility.isStringNullOrBlank(toCloneFrom) ? ReleaseService.RELEASE_TICKET_TEMPLATE : toCloneFrom;
	}

	@Override
	public BasicIssue call() throws Exception {
		final Release release = Release.createFromGitRef(gitRef);
		return ReleaseService.clone(release, toCloneFrom);
	}
}
