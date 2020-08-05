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

import dev.snowdrop.jira.atlassian.Utility;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class Product implements IssueSource {
	private static final String PRODUCT_TEMPLATE = "product.mustache";

	private final Component component;

	public Product(Component component) {
		this.component = component;
	}

	@Override
	public Release getParent() {
		return component.getParent();
	}

	@Override
	public String getName() {
		return component.getName();
	}

	@Override
	public String getTitle() {
		return component.getTitle();
	}

	@Override
	public Issue getJira() {
		return component.getProductIssue();
	}

	public String getReleaseDate() {
		return getParentSchedule().getFormattedReleaseDate();
	}

	private Schedule getParentSchedule() {
		return component.getParent().getSchedule();
	}

	public String getEndOfSupportDate() {
		return getParentSchedule().getFormattedEOLDate();
	}

	public String getDueDate() {
		return getParentSchedule().getFormattedDueDate();
	}

	public String getVersion() {
		return component.getParent().getVersion();
	}

	@Override
	public String getDescription() {
		StringWriter writer = new StringWriter();
		try {
			Utility.mf.compile(PRODUCT_TEMPLATE).execute(writer, this).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
}
