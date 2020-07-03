package dev.snowdrop.jira.set;

import org.jboss.logging.Logger;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AphroditeClient {

    private static final Logger LOG = Logger.getLogger(AphroditeClient.class);

    public static void main(String[] argv) throws Exception {
        init();
    }

    private static void init() throws Exception {
        try(Aphrodite aphrodite = Aphrodite.instance()){
            // Get issues
            try {
                List<Issue> issues = getIssues(aphrodite, "https://issues.redhat.com/projects/ENTSBT/issues/ENTSBT-448");
                for (Issue i : issues) {
                    LOG.info("Issue : " + i.getDescription());
                }
            } catch (MalformedURLException e) {
                LOG.error("Incorrect URL", e);
            }
        }
    }

    private static List<Issue> getIssues(Aphrodite aphrodite, String ref) throws MalformedURLException {
        Collection<URL> urls = new ArrayList<>();
        urls.add(new URL(ref));
        return aphrodite.getIssues(urls);
    }
}
