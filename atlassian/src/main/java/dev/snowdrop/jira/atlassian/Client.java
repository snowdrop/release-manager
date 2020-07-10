package dev.snowdrop.jira.atlassian;

import com.beust.jcommander.JCommander;

import dev.snowdrop.jira.atlassian.model.Issue;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static Args args;

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
                Service.getIssue(args.issue);
                break;

            case "create" :
                Service.createIssue();
                break;

            case "create-release" :
                ReleaseService.createReleaseIssues();
                break;

            case "clone" :
                ReleaseService.cloneIssue(args.issue);
                break;

            case "link" :
                Service.linkIssues(args.issue,args.toIssue);
                break;

            case "delete" :
                Service.deleteIssue(args.issue);
                break;

            case "delete-bulk" :
                Service.deleteIssues(args.issues);
                break;
        }
    }

    private void init() {
        try {
            // Parse YAML config
            if(args.cfg.contains("issue.yaml")) {
                readYaml(args.cfg, Issue.class);
            } else {
                // We assume that we want to generate the issues for a release
                readYaml(args.cfg, Release.class);
            }

            // Create JIRA authenticated client
            initRestClient(args.jiraServerUri,args.user,args.password);

        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void println(Object o) {
        System.out.println(o);
    }
}
