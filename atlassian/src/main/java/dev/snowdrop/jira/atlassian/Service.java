package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import org.jboss.logging.Logger;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String TARGET_RELEASE_CUSTOMFIELD_ID = "customfield_12311240";
    private static final String LINK_TYPE = "Dependency";

    public static void linkIssues(String fromIssue, String toIssue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.linkIssue(new LinkIssuesInput(fromIssue, toIssue, LINK_TYPE)).claim();
        LOG.infof("Linked the issue %s with the target/blocling issue %s", fromIssue, toIssue);
    }

    public static void getIssue(String issueNumber) {
        final IssueRestClient cl = restClient.getIssueClient();
        // Get Issue Info
        Issue issue = cl.getIssue(issueNumber).claim();
        LOG.info(issue);
    }

    public static void deleteIssue(String issue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.deleteIssue(issue, false);
        LOG.infof("Issue %s deleted",issue);
    }

    public static void createIssue() {
        final IssueRestClient cl = restClient.getIssueClient();

        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey("ENTSBT");
        iib.setSummary(release.getTitle());
        iib.setDescription(
                String.format(release.getTemplate(),
                        release.getComponents().get(0).getName(),
                        release.getVersion(),
                        release.getDate(),
                        release.getEOL()));
        iib.setIssueType(TASK_TYPE());
        // iib.setDueDate(formatDueDate(release.getDueDate()));
        // See: https://github.com/snowdrop/jira-tool/issues/7
        // iib.setFixVersions(setFixVersion());
        /*
         * {
         *   id=customfield_12311240,
         *   name=Target
         *   Release,
         *   type=null,
         *   value=
         * {
         *   "self": "https:\/\/issues.redhat.com\/rest\/api\/2\/version\/12345960",
         *   "id": "12345960",
         *   "description": "Spring Boot 2.3 Release",
         *   "name": "2.3.0.GA",
         *   "archived": false,
         *   "released": false,
         *   "releaseDate": "2020-09-14"
         * }
         */
        iib.setFieldValue(TARGET_RELEASE_CUSTOMFIELD_ID,setTargetRelease());

        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        LOG.infof("Issue %s created successfully",issueObj.getKey());
    }

}
