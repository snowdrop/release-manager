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
package dev.snowdrop.release.reporting;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_FixedWidth;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import dev.snowdrop.release.model.CVE;
import dev.snowdrop.release.model.Issue;
import dev.snowdrop.release.services.Utility;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import net.steppschuh.markdowngenerator.text.TextBuilder;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * @author <a href="antcosta@redhat.com">Antonio costa</a>
 */
@ApplicationScoped
public class ReportingService {

    public static String CR = "\n";

    public static final String JIRA_LINK_PREFIX = Utility.JIRA_SERVER + "browse/";
    public static final String JIRA_LINK_SUFFIX = "?filter=12347131";
    public static final String BUGZILLA_LINK_PREFIX = "https://bugzilla.redhat.com/show_bug.cgi?id=";
    public static final int DUE_DATE_WARNING_WEEKS = 3;

    public String buildAsciiReport(Collection<? extends Issue> issues) {
        AsciiTable at = new AsciiTable(new AT_Context().setWidth(130));
        at.getRenderer().setCWC(new CWC_FixedWidth().add(11).add(12).add(14).add(7).add(8).add(10).add(30).add(32).add(40));
        at.setTextAlignment(TextAlignment.LEFT);

        at.addRule();
        at.addRow("Issue", "Status", "CVE", "BZ", "Fix versions", "Due date", "Revisit", "Blocked", "Summary");

        at.addRule();
        issues.forEach(issue -> {
            final var isCVE = issue instanceof CVE;
            at.addRow(issue.getKey(),
                issue.getStatus(),
                isCVE ? ((CVE) issue).getId() : "",
                isCVE ? ((CVE) issue).getBugzilla() : "",
                String.join("<br/>", issue.getFixVersions()),
                getDueDateAsciiString(issue.getDueDate()),
                issue.getRevisit().orElse(""),
                issue.getBlockedBy().stream().map(b -> "- " + b).collect(Collectors.joining("<br><br>")),
                issue.getSummary());
            at.addRule();
        });
        return at.render();
    }

    public String buildMdReport(Collection<? extends Issue> issues, final String reportName) {
        StringBuilder sb = new StringBuilder().append(new Heading(reportName, 1)).append(CR);
        List<TableRow> lstTableRows = new LinkedList();
        TableRow tableHeader = new TableRow(new ArrayList<>(List.of("Issue", "Status", "CVE", "BZ", "Fix versions", "Due date", "Revisit", "Blocked", "Summary")));
        lstTableRows.add(tableHeader);
        issues.forEach(issue -> {
            final var isCVE = issue instanceof CVE;
            TableRow tableRow = new TableRow(
                List.of(new TextBuilder().append(new Link(issue.getKey(),JIRA_LINK_PREFIX + issue.getKey() + JIRA_LINK_SUFFIX)),
                    issue.getStatus(),
                    isCVE ? ((CVE) issue).getId() : "",
                    isCVE ? new TextBuilder().append(new Link(((CVE) issue).getBugzilla(), BUGZILLA_LINK_PREFIX + ((CVE) issue).getBugzilla())) : "",
                    String.join("<br/>", issue.getFixVersions()),
                    getDueDateMdString(issue.getDueDate()),
                    issue.getRevisit().orElse(""),
                    issue.getBlockedBy().stream().map(b -> "- " + b).collect(Collectors.joining("<br><br>")),
                    issue.getSummary())
            );
            lstTableRows.add(tableRow);
        });
        Table mdTable = new Table();
        mdTable.setRows(lstTableRows);
        sb.append(CR).append(CR).append(mdTable.serialize()).append(CR);
        System.out.println(sb.toString());
        return sb.toString();
    }

    private String getDueDateAsciiString(DateTime dueDate) {
        if (dueDate == null) {
            return "";
        }
        return dueDate.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
    }

    private String getDueDateMdString(DateTime dueDate) {
        if (dueDate == null) {
            return "";
        }
        String dueDateString = dueDate.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
        if (dueDate.minusWeeks(DUE_DATE_WARNING_WEEKS).isBeforeNow()) {
            return "**" + dueDateString + "**";
        }
        return dueDateString;
    }
}
