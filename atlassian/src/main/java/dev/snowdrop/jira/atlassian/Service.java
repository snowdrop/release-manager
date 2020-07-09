package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

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
        cl.deleteIssue(issue, false);
        LOG.infof("Issue %s deleted",issue);
    }

    public static void createReleaseIssues() {
        final IssueRestClient cl = restClient.getIssueClient();
        Release release = (Release)pojo;

        for(Component component : release.getComponents()) {
            IssueInputBuilder iib = new IssueInputBuilder();
            iib.setProjectKey(component.getProjectKey());
            iib.setSummary(release.getTitle());
            iib.setDescription(generateIssueDescription(release,component));
            iib.setIssueType(TASK_TYPE());
            /*
             TODO: To be investigated

             snowdrop-bot user cannot set the following field
             iib.setDueDate(formatDueDate(release.getDueDate()));

             See: https://github.com/snowdrop/jira-tool/issues/7

             IF we know the custom_field value, then we can fill the following field
             iib.setFieldValue(TARGET_RELEASE_CUSTOMFIELD_ID,setTargetRelease());
            */

            IssueInput issue = iib.build();
            BasicIssue issueObj = cl.createIssue(issue).claim();
            LOG.infof("Issue %s created successfully",issueObj.getKey());
        }
    }

    public static void createIssue() {
        final IssueRestClient cl = restClient.getIssueClient();
        dev.snowdrop.jira.atlassian.model.Issue issuePOJO = (dev.snowdrop.jira.atlassian.model.Issue)pojo;

        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey(issuePOJO.getProjectKey());
        iib.setSummary(issuePOJO.getTitle());
        iib.setDescription(issuePOJO.getDescription());
        iib.setIssueType(TASK_TYPE());
        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        LOG.infof("Issue %s created successfully",issueObj.getKey());
    }

}
