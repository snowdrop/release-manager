package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import com.beust.jcommander.JCommander;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static dev.snowdrop.jira.atlassian.Utility.TEMPLATE;

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
        client.createIssue();
        //client.getIssue(args.issue);
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
        iib.setDescription(TEMPLATE);
        iib.setIssueType(taskType());
        iib.setDueDate(new DateTime());
        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        System.out.println("Issue " + issueObj.getKey() + " created successfully");
    }

    private static IssueType taskType() {
        try {
            return new IssueType(
                    new URI("https://issues.redhat.com/rest/api/2/issuetype/3"),
                    Long.valueOf("3"),
                    "A task that needs to be done.",
                    false,
                    "Task",
                    new URI("https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13278&avatarType=issuetype"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void println(Object o) {
        System.out.println(o);
    }

    private URI jiraServerUri(String uri) {
        if (uri != null) {
            return URI.create(uri);
        } else {
            return URI.create("https://issues.redhat.com/");
        }
    }
}
