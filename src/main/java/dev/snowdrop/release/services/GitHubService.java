package dev.snowdrop.release.services;

import java.io.IOException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

@ApplicationScoped
public class GitHubService {

    static final Logger LOG = Logger.getLogger(GitHubService.class);

    public String getCveIssueTitle(final String version) {
        return "CVE " + (version != null ? "for " + version : "for triage");
    }

    public void createGithubIssue(
        final String mdText,
        final String issueTitle,
        final String label,
        final String token,
        final String repoName) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository gitHubRepo = github.getRepository(repoName);
        GHIssueBuilder githubIssueBuilder = gitHubRepo.createIssue(issueTitle);
        githubIssueBuilder.body(mdText);
        githubIssueBuilder.label(label);
        githubIssueBuilder.create();
    }

    /**
     * Close issues
     *
     * @param label
     * @param token
     * @param repoName
     * @param version
     * @throws IOException
     */
    public void closeOldCveIssues(final String label, final String token, final String repoName, final String version)
        throws IOException {
        final String issueTitle = this.getCveIssueTitle(version);
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository gitHubRepo = github.getRepository(repoName);
        List<GHIssue> ghIssues = gitHubRepo.getIssues(GHIssueState.OPEN);
        ghIssues.stream().filter(issue -> {
            try {
                LOG.warnf("Issue title: %s; calculated title: %s;", issue.getTitle(), issueTitle);
                return (issue.getLabels().stream().anyMatch(labels -> {
                    return labels.getName().equalsIgnoreCase(label);
                }) && (issue.getTitle().equalsIgnoreCase(issueTitle)));
            } catch (IOException e) {
                LOG.warnf("Error filtering issue %s. Error: %s", issue.getNumber(), e.getLocalizedMessage());
                return false;
            }
        }).forEach(issue -> {
            try {
                issue.close();
            } catch (IOException e) {
                LOG.warnf("Error closing issue {}. Error: {}", issue.getNumber(), e.getLocalizedMessage());
            }
        });
    }

}
