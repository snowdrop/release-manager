package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import org.jboss.logging.Logger;

import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.getURLFor;
import static dev.snowdrop.jira.atlassian.Utility.restClient;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String LINK_TYPE = "Dependency";

    public static void linkIssues(String fromIssue, String toIssue) {
        final IssueRestClient cl = restClient.getIssueClient();
        try {
            cl.linkIssue(new LinkIssuesInput(fromIssue, toIssue, LINK_TYPE)).claim();
        } catch (Exception e) {
            LOG.errorf("CVE issue not found: %s", toIssue);
        }
        LOG.infof("Linked the issue %s with the blocking issue %s", getURLFor(fromIssue), toIssue);
    }

    public static void getIssue(String issueNumber) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(issueNumber).claim();
        LOG.info(issue);
    }

    public static void deleteIssue(String issue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.deleteIssue(issue, false).claim();
        LOG.infof("Issue %s deleted", issue);
    }

    public static void deleteIssues(List<String> issues) {
        final IssueRestClient cl = restClient.getIssueClient();
        for (String issue : issues) {
            cl.deleteIssue(issue, false).claim();
            LOG.infof("Issue %s deleted", issue);
        }
    }

}
