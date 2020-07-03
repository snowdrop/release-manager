package dev.snowdrop.jira.set;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import com.beust.jcommander.JCommander;

import java.net.URI;

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
        client.getIssue(args.issue);
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
        final IssueRestClient issueClient = restClient.getIssueClient();
        // Get Issue Info
        Issue issue = issueClient.getIssue(issueNumber).claim();

        println(issue);
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
