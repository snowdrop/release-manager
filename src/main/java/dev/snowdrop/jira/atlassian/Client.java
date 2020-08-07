package dev.snowdrop.jira.atlassian;

import com.beust.jcommander.JCommander;
import dev.snowdrop.jira.atlassian.commands.*;
import org.jboss.logging.Logger;

import static dev.snowdrop.jira.atlassian.Utility.gitRefOrFail;
import static dev.snowdrop.jira.atlassian.Utility.initRestClient;

public class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);

    public static void main(String[] argv) throws Exception {

        Client client = new Client();
        final Args args = new Args();

        JCommander.newBuilder()
              .addObject(args)
              .build()
              .parse(argv);

        client.init(args);

        switch (args.action) {
            case "get":
                final var issue = new GetIssue(args.issue).call();
                LOG.info(issue);
                return;

            case "create-component":
                new CreateComponentIssues(gitRefOrFail(args)).call();
                return;

            case "clone":
                new CloneIssue(gitRefOrFail(args), args.issue).call();
                return;

            case "start-release":
                new StartRelease(gitRefOrFail(args)).call();
                return;

            case "link":
                new LinkIssue(args.issue, args.toIssue).call();
                return;

            case "delete":
                new DeleteIssues(args.issues).call();
                return;
            default:
                throw new RuntimeException("Unknown action: " + args.action);
        }
    }

    private void init(Args args) {
        try {
            // Create JIRA authenticated client
            initRestClient(args.jiraServerUri, args.user, args.password);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
