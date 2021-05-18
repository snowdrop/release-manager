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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import dev.snowdrop.release.model.CVE;
import dev.snowdrop.release.model.cpaas.SecurityImpact;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.snowdrop.release.model.Issue.DEFAULT_JIRA_PROJECT;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@ApplicationScoped
public class CVEService {
    public static final String EMBARGOED_PREFIX = "EMBARGOED";
    private static final Matcher CVE_PATTERN = Pattern.compile("[EMBARGOED]?.*(CVE-\\d{4}-\\d{1,6}).*").matcher("");
    private static final Matcher CVE_TITLE_PATTERN = Pattern.compile("[EMABRGOED]?[ ]*(CVE-\\d{4}-\\d{1,6}) ([a-zA-Z0-9 \\-]*):(.*) [\\[?[a-zA-Z0-9]*\\]?.*]").matcher("");
    private static final Matcher BZ_PATTERN = Pattern.compile(".*https://bugzilla.redhat.com/show_bug.cgi\\?id=(\\d{7}).*").matcher("");
    private static final long UNRESOLVED_CVES = 12347131;
    @Inject
    JiraRestClient restClient;

    public List<CVE> listCVEs(Optional<String> releaseVersion, boolean computeStatus) {
        final var searchResult = new SearchResult[1];
        final var searchClient = restClient.getSearchClient();
        releaseVersion.ifPresentOrElse(
            version -> searchResult[0] = searchClient.searchJql("project = " + DEFAULT_JIRA_PROJECT + " AND text ~ \"cve-*\" AND fixVersion = " + version).claim(),
            () -> searchResult[0] = searchClient.getFilter(UNRESOLVED_CVES).flatMap(f -> searchClient.searchJql(f.getJql())).claim()
        );
        final var issues = searchResult[0].getIssues();
        final var cves = new LinkedList<CVE>();
        for (Issue issue : issues) {
            final CVE cve = create(issue, computeStatus);
            cves.add(cve);
        }
        return cves;
    }

    CVE create(Issue issue, boolean computeStatus) {
        final List<String> fixVersions = Utility.getVersionsAsStrings(issue);
        var summary = issue.getSummary();
        String id = "";
        // extract CVE id from summary
        if (CVE_PATTERN.reset(summary).matches()) {
            id = CVE_PATTERN.group(1);
            // remove id from summary
            summary = summary.substring(0, CVE_PATTERN.start(1)).concat(summary.substring(CVE_PATTERN.end(1))).trim();
        }

        final var cve = new CVE(issue.getKey(), summary, fixVersions, issue.getStatus().getName(), issue.getDueDate());
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
                    cve.setImpact(SecurityImpact.valueOf(line.substring(line.indexOf(':') + 1).trim()));
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

        if (computeStatus) {
            cve.computeStatus(issue);
        }
        return cve;
    }

    public List<String> cveToAdvisory(List<CVE> cveList) {
        StringBuffer sbCve = new StringBuffer();
        List<String> cvrStrList = new ArrayList<>(cveList.size());
        if (cveList != null && cveList.size() > 0) {
            cveList.forEach(cveItem -> {
                final var summary = cveItem.getSummary();
                String id = "";
                // extract CVE id from summary
                if (CVE_TITLE_PATTERN.reset(summary).find()) {
                    sbCve.append(CVE_TITLE_PATTERN.group(2)).append(":").append(CVE_TITLE_PATTERN.group(3)).append(" (").append(CVE_TITLE_PATTERN.group(1)).append(")");
                    cvrStrList.add(new StringBuffer(CVE_TITLE_PATTERN.group(2)).append(":").append(CVE_TITLE_PATTERN.group(3)).append(" (").append(CVE_TITLE_PATTERN.group(1)).append(")").toString());
                }
            });
        }
        return cvrStrList;
    }

}
