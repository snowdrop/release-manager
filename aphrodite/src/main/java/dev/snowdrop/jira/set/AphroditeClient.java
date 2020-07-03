package dev.snowdrop.jira.set;

import com.beust.jcommander.JCommander;
import org.jboss.logging.Logger;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.spi.AphroditeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AphroditeClient {

    private static final Logger LOG = Logger.getLogger(AphroditeClient.class);
    private static final String CFG_FILE = "aphrodite.config";
    private static Args args;
    private static Aphrodite aphrodite;
    private static String jiraServerUri;

    public static void main(String[] argv) throws Exception {
        AphroditeClient client = new AphroditeClient();
        args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        jiraServerUri = args.jiraServerUri;
        System.setProperty(CFG_FILE, args.config);

        client.init();
        client.getIssues(args.issue);

        aphrodite.close();
    }

    private void init() {
        try {
            aphrodite = Aphrodite.instance();
        } catch (AphroditeException e) {
            LOG.error(e);
        }
    }

    private void getIssues(String issueKey) throws MalformedURLException {
        Collection<URL> urls = new ArrayList<>();
        urls.add(new URL(jiraServerUri + "/" + issueKey));
        List<Issue> issues = aphrodite.getIssues(urls);
        for (Issue i : issues) {
            LOG.info("Issue : " + i.getDescription());
        }
    }
}
