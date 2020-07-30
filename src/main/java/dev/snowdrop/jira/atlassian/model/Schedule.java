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

import com.fasterxml.jackson.annotation.JsonProperty;

import static dev.snowdrop.jira.atlassian.Utility.getFormatted;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class Schedule {
	@JsonProperty
	private String release;
	@JsonProperty
	private String due;
	@JsonProperty
	private String eol;

	public String getFormattedReleaseDate() {
		return getFormatted(release);
	}

	public String getFormattedDueDate() {
		return getFormatted(due);
	}

	public String getDueDate() {
		return due;
	}

	public String getFormattedEOLDate() {
		return getFormatted(eol);
	}

	public String getReleaseDate() {
		return release;
	}

	public String getEOLDate() {
		return eol;
	}
}
