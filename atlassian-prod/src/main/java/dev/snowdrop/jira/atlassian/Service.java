package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import org.jboss.logging.Logger;

import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String LINK_TYPE = "Dependency";

    public static void linkIssues(String fromIssue, String toIssue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.linkIssue(new LinkIssuesInput(fromIssue, toIssue, LINK_TYPE)).claim();
        LOG.infof("Linked the issue %s with the blocking issue %s", fromIssue, toIssue);
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
