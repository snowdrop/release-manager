package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import com.beust.jcommander.JCommander;

import org.jboss.logging.Logger;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static Args args;
    private static JiraRestClient restClient;

    public static void main(String[] argv) {
        Client client = new Client();
        args = new Args();

        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        client.init();

        switch(args.action) {
            case "get" :
                client.getIssue(args.issue);
                break;

            case "create" :
                client.createIssue();
                break;

            case "delete" :
                client.deleteIssue(args.issue);
                break;
        }
    }

    private void init() {
        try {
            // Parse YAML config
            readYaml(args.cfg);

            // Create JIRA authenticated client
            AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            restClient = factory.createWithBasicHttpAuthentication(jiraServerUri(args.jiraServerUri), args.user, args.password);
            restClient.getSessionClient().getCurrentSession().get().getLoginInfo().getFailedLoginCount();

        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void getIssue(String issueNumber) {
        final IssueRestClient cl = restClient.getIssueClient();
        // Get Issue Info
        Issue issue = cl.getIssue(issueNumber).claim();
        println(issue);
    }

    private void deleteIssue(String issue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.deleteIssue(issue, false);
        LOG.infof("Issue %s deleted",issue);
    }

    private void createIssue() {
        final IssueRestClient cl = restClient.getIssueClient();

        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey("ENTSBT");
        iib.setSummary(release.getTitle());
        iib.setDescription(
                String.format(release.getTemplate(),
                        release.getVersion(),
                        release.getDate(),
                        release.getEOL()));
        iib.setIssueType(TASK_TYPE());
        iib.setDueDate(formatDueDate(release.getDueDate()));
        iib.setFixVersions(setFixVersion());
        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        LOG.infof("Issue %s created successfully",issueObj.getKey());
    }

    private void println(Object o) {
        System.out.println(o);
    }
}
