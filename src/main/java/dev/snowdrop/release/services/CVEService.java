/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.snowdrop.release.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import dev.snowdrop.release.model.CVE;

import static dev.snowdrop.release.model.Issue.DEFAULT_JIRA_PROJECT;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@ApplicationScoped
public class CVEService {
    private static final Matcher CVE_PATTERN = Pattern.compile(".*(CVE-\\d{4}-\\d{1,6}).*").matcher("");
    private static final Matcher BZ_PATTERN = Pattern.compile(".*https://bugzilla.redhat.com/show_bug.cgi\\?id=(\\d{7}).*").matcher("");
    private static final long UNRESOLVED_CVES = 12347131;
    
    @Inject
    JiraRestClient restClient;
    
    public Iterable<CVE> listCVEs(Optional<String> releaseVersion) {
        final var searchResult = new SearchResult[1];
        final var searchClient = restClient.getSearchClient();
        releaseVersion.ifPresentOrElse(
            version -> searchResult[0] = searchClient.searchJql("project = " + DEFAULT_JIRA_PROJECT + " AND text ~ \"cve-*\" AND fixVersion = " + version).claim(),
            () -> searchResult[0] = searchClient.getFilter(UNRESOLVED_CVES).flatMap(f -> searchClient.searchJql(f.getJql())).claim()
        );
        final var issues = searchResult[0].getIssues();
        final var cves = new LinkedList<CVE>();
        for (Issue issue : issues) {
            final CVE cve = create(issue);
            cves.add(cve);
        }
        return cves;
    }
    
    public CVE create(Issue issue) {
        final var versions = issue.getFixVersions();
        final List<String> fixVersions;
        if (versions != null) {
            fixVersions = new LinkedList<>();
            versions.forEach(v -> fixVersions.add(v.getName()));
        } else {
            fixVersions = Collections.emptyList();
        }
        var summary = issue.getSummary();
        String id = "";
        // extract CVE id from summary
        if (CVE_PATTERN.reset(summary).matches()) {
            id = CVE_PATTERN.group(1);
            // remove id from summary
            summary = summary.substring(CVE_PATTERN.end(1)).trim();
        }
        
        final var cve = new CVE(issue.getKey(), summary, fixVersions, issue.getStatus().getName());
        cve.setId(id);
        cve.setJiraClient(restClient);
        
        final var description = issue.getDescription();
        if (description != null) {
            final var lines = description.lines()
                .filter(Predicate.not(String::isBlank))
                .iterator();
            boolean impactFound = false;
            while (lines.hasNext()) {
                var line = lines.next();
                // first line should be the one where impact is recorded
                if (!impactFound && line.startsWith("Impact")) {
                    cve.setImpact(line.substring(line.indexOf(':') + 1).trim());
                    impactFound = true;
                    continue;
                }
    
                // only check for BZ link after CVE is found since that's where it is (at least, in the JIRAs we've seen so far)
                if (BZ_PATTERN.reset(line).matches()) {
                    cve.setBugzilla(BZ_PATTERN.group(1));
                    break;
                }
            }
        }
    
        cve.computeStatus(issue);
        return cve;
    }
}
