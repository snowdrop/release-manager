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

import dev.snowdrop.jira.atlassian.model.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class POMParser {
	public static List<Artifact> getArtifactsWith(String property) {
		try {
			MavenXpp3Reader mavenreader = new MavenXpp3Reader();
			final URL resource = POMParser.class.getClassLoader().getResource("pom.xml");
			final InputStream inputStream = resource.openStream();
			Model model = mavenreader.read(inputStream);

			final String versionProp = property + ".version";
			final String version = model.getProperties().get(versionProp).toString();

			return model.getDependencyManagement().getDependencies().stream()
					.filter(d -> d.getVersion().contains(versionProp))
					.map(d -> new Artifact(d.getGroupId(), d.getArtifactId(), version))
					.collect(Collectors.toList());

		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
