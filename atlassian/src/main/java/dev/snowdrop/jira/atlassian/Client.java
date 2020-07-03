package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import com.beust.jcommander.JCommander;
import org.joda.time.DateTime;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Client {
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
                // Statements
                break;
        }
    }

    private void init() {
        try {
            AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            restClient = factory.createWithBasicHttpAuthentication(jiraServerUri(args.jiraServerUri), args.user, args.password);
            restClient.getSessionClient().getCurrentSession().get().getLoginInfo().getFailedLoginCount();



        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void getIssue(String issueNumber) {
        final IssueRestClient cl = restClient.getIssueClient();
        // Get Issue Info
        Issue issue = cl.getIssue(issueNumber).claim();
        println(issue);
    }

    private void createIssue() {
        final IssueRestClient cl = restClient.getIssueClient();

        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey("ENTSBT");
        iib.setSummary("Test Summary");
        iib.setDescription(String.format(TEMPLATE,"2.3.RELEASE","September 1st 2020","September 2021"));
        iib.setIssueType(TASK_TYPE());
        iib.setDueDate(new DateTime());
        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        System.out.println("Issue " + issueObj.getKey() + " created successfully");
    }

    private void println(Object o) {
        System.out.println(o);
    }
}
