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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.POM;
import dev.snowdrop.release.model.Release;

import static dev.snowdrop.release.model.Release.RELEASE_SUFFIX;
import static dev.snowdrop.release.services.Utility.MAPPER;
import static dev.snowdrop.release.services.Utility.isStringNullOrBlank;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Singleton
public class ReleaseFactory {
    @Inject
    JiraRestClient restClient;
    
    /**
     * @param gitRef a GitHub reference in the form org/project/reference e.g. metacosm/spring-boot-bom/release-integration
     * @return
     * @throws Exception
     */
    public Release createFromGitRef(String gitRef) {
        return createFromGitRef(gitRef, false);
    }
    
    public Release createFromGitRef(String gitRef, boolean skipProductRequests) {
        try (InputStream releaseIS = getStreamFromGitRef(gitRef, "release.yml");
             InputStream pomIS = getStreamFromGitRef(gitRef, "pom.xml")) {
            
            return createFrom(releaseIS, pomIS, skipProductRequests);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    Release createFrom(InputStream releaseIS, InputStream pomIS) throws IOException {
        return createFrom(releaseIS, pomIS, false);
    }
    
    Release createFrom(InputStream releaseIS, InputStream pomIS, boolean skipProductRequests) throws IOException {
        final Release release = MAPPER.readValue(releaseIS, Release.class);
        
        // retrieve associated POM
        final var pom = POM.createFrom(pomIS);
        release.setPom(pom);
        
        // validate release
        final String pomVersion = pom.getVersion();
        validate(release, pomVersion, skipProductRequests);
        
        return release;
    }
    
    static InputStream getStreamFromGitRef(String gitRef, String relativePath) throws IOException {
        URI uri = URI.create("https://raw.githubusercontent.com/" + gitRef + "/" + relativePath);
        return uri.toURL().openStream();
    }
    
    public void validate(Release release, String expectedVersion, boolean skipProductRequests) throws IllegalArgumentException {
        final List<String> errors = new LinkedList<>();
        
        // validate version
        final var version = release.getVersion();
        if (Utility.isStringNullOrBlank(version)) {
            errors.add("missing version");
        } else {
            final int suffix = version.indexOf(RELEASE_SUFFIX);
            final int len = suffix > 0 ? version.length() - suffix : version.length();
            if (!version.regionMatches(0, expectedVersion, 0, len)) {
                errors.add(String.format("'%s' release version doesn't match '%s' version in associated POM", version,
                    expectedVersion));
            }
        }
        
        // validate schedule
        final var schedule = release.getSchedule();
        if (schedule == null) {
            errors.add("missing schedule");
        } else {
            if (isStringNullOrBlank(schedule.getReleaseDate())) {
                errors.add("missing release date");
            }
            try {
                schedule.getFormattedReleaseDate();
            } catch (Exception e) {
                errors.add("invalid release ISO8601 date: " + e.getMessage());
            }
            
            if (isStringNullOrBlank(schedule.getEOLDate())) {
                errors.add("missing EOL date");
            }
            try {
                schedule.getFormattedEOLDate();
            } catch (Exception e) {
                errors.add("invalid EOL ISO8601 date: " + e.getMessage());
            }
        }
        
        // validate components
        if (!skipProductRequests) {
            final var components = release.getComponents();
            components.parallelStream().forEach(component -> {
                validateIssue(errors, component, false);
                validateIssue(errors, component, true);
            });
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                errors.stream().reduce("Invalid release:\n", (s, s2) -> s + "\t- " + s2 + "\n"));
        }
    }
    
    private void validateIssue(List<String> errors, Component component, boolean isProduct) {
        var issue = isProduct ? component.getProductIssue() : component.getJira();
        if (issue != null) {
            var componentEntryName = isProduct ? "product" : "jira";
            var project = issue.getProject();
            var name = component.getName();
            var issueTypeId = issue.getIssueTypeId();
            issue.getAssignee().ifPresent(assignee ->
                {
                    try {
                        restClient.getUserClient().getUser(assignee).claim();
                    } catch (Exception e) {
                        errors.add(String.format("invalid assignee for %s project '%s': %s", componentEntryName, project, assignee));
                    }
                }
            );
            try {
                var p = restClient.getProjectClient().getProject(project).claim();
                for (IssueType issueType : p.getIssueTypes()) {
                    if (issueType.getId().equals(issueTypeId)) {
                        return;
                    }
                }
                errors.add(String.format("invalid issue type id '%d' for component '%s'", issueTypeId, name));
            } catch (Exception e) {
                errors.add(String.format("invalid %s project '%s' for component '%s'", componentEntryName, project, name));
            }
        }
    }
}
