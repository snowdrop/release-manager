package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import io.atlassian.util.concurrent.Promise;
import org.jboss.logging.Logger;

import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.getURLFor;
import static dev.snowdrop.jira.atlassian.Utility.restClient;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String LINK_TYPE = "Dependency";
    private static final IssueRestClient cl = restClient.getIssueClient();

    public static void linkIssue(String fromIssue, String toIssue) {
        final Promise<Issue> toPromise = cl.getIssue(toIssue)
              .fail(e -> LOG.errorf("Couldn't retrieve %s issue to link to: %s", toIssue, e.getLocalizedMessage()));

        cl.linkIssue(new LinkIssuesInput(fromIssue, toIssue, LINK_TYPE))
              .fail(e -> LOG.errorf("Exception linking %s to %s: %s", fromIssue, toIssue, e.getLocalizedMessage()))
              .claim();

        final Issue to = toPromise.claim();
        LOG.infof("Linked %s with the blocking issue %s: %s", getURLFor(fromIssue), toIssue, to.getSummary());
    }

    public static Issue getIssue(String issueNumber) {
        Issue issue = cl.getIssue(issueNumber).claim();
        LOG.info(issue);
        return issue;
    }

    public static void deleteIssues(List<String> issues) {
        for (String issue : issues) {
            cl.deleteIssue(issue, false).claim();
            LOG.infof("Issue %s deleted", issue);
        }
    }

}
