package dev.snowdrop.release;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import dev.snowdrop.release.model.Issue;
import dev.snowdrop.release.model.Release;
import dev.snowdrop.release.reporting.ReportingService;
import dev.snowdrop.release.services.*;
import dev.snowdrop.release.services.GitService.GitConfig;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "release-manager", mixinStandardHelpOptions = true, version = "release-manager 1.0.0")
@ApplicationScoped
@QuarkusMain
public class App implements QuarkusApplication {
    public final static String CVE_REPORT_REPO_NAME = "snowdrop/reports";

    @Inject
    @ConfigProperty(name = "gitlab.buildconfig.fork.repo", defaultValue = "snowdrop/build-configurations")
    String buildConfigForkRepoName;

    static final Logger LOG = Logger.getLogger(App.class);
    @Inject
    ReleaseFactory factory;
    @Inject
    IssueService service;
    @Inject
    GitHubService github;
    @Inject
    CommandLine.IFactory cliFactory;
    @Inject
    GitService git;
    @Inject
    CVEService cveService;
    @Inject
    ReportingService reportingService;
    @Inject
    BuildConfigUpdateService buildConfigUpdateService;
    @Inject
    SpringBootBomUpdateService springBootBomUpdateService;

    @CommandLine.Option(
        names = {"-u", "--user"},
        description = "JIRA user",
        required = true,
        scope = CommandLine.ScopeType.INHERIT)
    private String user;
    @CommandLine.Option(
        names = {"-p", "--password"},
        description = "JIRA password",
        required = true,
        scope = CommandLine.ScopeType.INHERIT)
    private String password;
    @CommandLine.Option(
        names = "--url",
        description = "URL of the JIRA server",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
        defaultValue = Utility.JIRA_SERVER,
        scope = CommandLine.ScopeType.INHERIT)
    private String jiraServerURI;
    @CommandLine.Option(
        names = {"-w", "--watchers"},
        description = "Comma-separated list of user names to add to watchers",
        scope = CommandLine.ScopeType.INHERIT,
        split = ",")
    private List<String> watchers;

    public static void main(String[] argv) throws Exception {
        Quarkus.run(App.class, argv);
    }

    @Override
    public int run(String... args) throws Exception {
        int exitCode = new CommandLine(this, cliFactory).execute(args);
        return exitCode;
    }

    @CommandLine.Command(name = "get", description = "Retrieve the specified issue")
    public void get(@CommandLine.Parameters(description = "JIRA issue key") String key) {
        System.out.println(service.getIssue(key));
    }

    @CommandLine.Command(
        name = "clone",
        description = "Clone the specified issue using information from the release associated with the specified git reference")
    public void clone(
        @CommandLine.Option(
            names = {"-g", "--git"},
            description = "Git reference in the <github org>/<github repo>/<branch | tag | hash> format") String gitRef,
        @CommandLine.Parameters(
            description = "JIRA issue key",
            defaultValue = IssueService.RELEASE_TICKET_TEMPLATE,
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS) String toCloneFrom) throws Throwable {
        final Release release = factory.createFromGitRef(gitRef);
        System.out.println(service.clone(release, toCloneFrom, watchers));
    }

    @CommandLine.Command(
        name = "create-component",
        description = "Create component requests for the release associated with the specified git reference")
    public void createComponentRequests(
        @CommandLine.Option(
            names = {"-g", "--git"},
            description = "Git reference in the <github org>/<github repo>/<branch | tag | hash> format") String gitRef)
        throws Throwable {
        final Release release = factory.createFromGitRef(gitRef);
        service.createComponentRequests(release, watchers);
    }

    @CommandLine.Command(name = "delete", description = "Delete the specified comma-separated issues")
    public void delete(
        @CommandLine.Parameters(description = "Comma-separated JIRA issue keys", split = ",") List<String> issues) {
        service.deleteIssues(issues);
    }

    @CommandLine.Command(
        name = "link",
        description = "Link the issue specified by the 'from' option to the issue specified by the 'to' option")
    public void link(
        @CommandLine.Parameters(
            description = ("JIRA issue key from which a link should be created")) String fromIssue,
        @CommandLine.Option(
            names = {"-t", "--to"},
            description = ("JIRA issue key to link to"),
            required = true) String toIssue) {
        service.linkIssue(fromIssue, toIssue);
    }

    @CommandLine.Command(
        name = "new-maj-min",
        description = "Initialize repositories for a new Major.Minor release.")
    public void newMajorMinor(
        @CommandLine.Option(
            names = {"-g", "--git"},
            description = "Git reference in the <github org>/<github repo>/<branch> format",
            required = true) String gitRef,
        @CommandLine.Option(
            names = {"-t", "--test"},
            description = "Create a test release ticket using the SB project for all requests") boolean test,
        @CommandLine.Option(
            names = {"-o", "--token"},
            description = "Github API token",
            required = true) String token,
        @CommandLine.Option(names = {"-glu", "--gluser"}, description = "Gitlab user name", required = true) String gluser,
        @CommandLine.Option(names = {"-glt", "--gltoken"}, description = "Gitlab API token", required = true) String gltoken,
        @CommandLine.Option(names = {"-r", "--release"}, description = "release", required = true) String release,
        @CommandLine.Option(names = {"-pr", "--previous-release"},description = "Previous release",required = true) String previousRelease
    ) throws Throwable {
        final String[] releaseMajorMinorFix = release.split("\\.");
        final String[] prevReleaseMajorMinorFix = previousRelease.split("\\.");
        final GitConfig bomGitConfig = GitConfig.githubConfig(gitRef, token, Optional.of(String.format("sb-%s.%s.x", prevReleaseMajorMinorFix[0], prevReleaseMajorMinorFix[1])));
        git.initRepository(bomGitConfig);
        if (!test) {
            springBootBomUpdateService.newMajorMinor(bomGitConfig);
        }

        final GitConfig buildConfigGitlabConfig = GitConfig.gitlabConfig(release,gluser,gltoken,buildConfigForkRepoName,Optional.of("master"));
        git.initRepository(buildConfigGitlabConfig);
        if (!test) {
            buildConfigUpdateService.newMajorMinor(buildConfigGitlabConfig,  releaseMajorMinorFix[0], releaseMajorMinorFix[1], prevReleaseMajorMinorFix[0], prevReleaseMajorMinorFix[1]);
        }
    }


    @CommandLine.Command(
        name = "start-release",
        description = "Start the release process for the release associated with the specified git reference")
    public void startRelease(
        @CommandLine.Option(
            names = {"-g", "--git"},
            description = "Git reference in the <github org>/<github repo>/<branch> format",
            required = true) String gitRef,
        @CommandLine.Option(
            names = {"-s", "--skip"},
            description = "Skip product requests") boolean skipProductRequests,
        @CommandLine.Option(
            names = {"-t", "--test"},
            description = "Create a test release ticket using the SB project for all requests") boolean test,
        @CommandLine.Option(
            names = {"-o", "--token"},
            description = "Github API token",
            required = true) String token,
        @CommandLine.Option(
            names = {"-r", "--release-date"},
            description = "Release Date(yyyy-mm-dd)",
            required = true) String releaseDate,
        @CommandLine.Option(
            names = {"-e", "--eol-date"},
            description = "End of Life Date(yyyy-mm-dd)",
            required = true) String eolDate) throws Throwable {
        final GitConfig config = GitConfig.githubConfig(gitRef, token, Optional.empty());
        if (!test) {
            git.initRepository(config); // init git repository to be able to update release
        }

        Release release = factory.createFromGitRef(gitRef, skipProductRequests, true);
        release.setTest(test);

        try {
            release.setSchedule(releaseDate, eolDate);
        } catch (Exception e) {
            LOG.error("Invalid release date", e);
            return;
        }

        List<String> errors = release.validateSchedule();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.stream().reduce("Invalid release:\n", Utility.errorsFormatter(
                0)));
        }

        BasicIssue issue;
        // first check if we already have a release ticket, in which case we don't need
        // to clone the template
        final String releaseTicket = release.getJiraKey();
        if (!Utility.isStringNullOrBlank(releaseTicket)) {
            try {
                issue = service.getIssue(releaseTicket);
                LOG.infof("Release ticket %s already exists, skipping cloning step", releaseTicket);
            } catch (Exception e) {
                // if we got an exception, assume that it's because we didn't find the ticket
                issue = clone(release, token);
            }
        } else {
            // no release ticket was specified, clone
            issue = clone(release, token);

        }

        // link CVEs
        for (var cve : cveService.listCVEs(Optional.of(release.getVersion()), false)) {
            service.linkIssue(issue.getKey(), cve.getKey());
        }

        if (!skipProductRequests) {
            service.createComponentRequests(release, watchers);
        }

        if (!release.isTestMode()) {
            git.commitAndPush("chore: update release issues' key [release-manager]", config, repo -> Stream
                .of(factory.updateRelease(repo, release)));
        }
        System.out.println(issue);
    }

    @CommandLine.Command(
        name = "list-cves",
        description = "List CVEs for the specified release or, if not specified, unresolved CVEs")
    public void listCVEs(
        @CommandLine.Option(
            names = {"-g", "--publish"},
            description = "Publish the CVE list to github") boolean release,
        @CommandLine.Option(
            names = {"-o", "--token"},
            description = "Github API token. Required if --publish is enabled") String token,
        @CommandLine.Parameters(
            description = "Release for which to retrieve the CVEs, e.g. 2.2.10",
            arity = "0..1") String version) throws Throwable {
        final var cves = cveService.listCVEs(Optional.ofNullable(version), true);
        System.out.println(reportingService.buildAsciiReport(cves));
        if (release) {
            if (Optional.ofNullable(token).isPresent()) {
                github.closeOldCveIssues("cve", token, CVE_REPORT_REPO_NAME, version);
                String cveIssueTitle = github.getCveIssueTitle(version);
                String mdReport = reportingService.buildMdReport(cves, cveIssueTitle);
                github.createGithubIssue(mdReport, cveIssueTitle, "cve", token, CVE_REPORT_REPO_NAME);
            } else {
                LOG.error(
                    "Cannot release CVE to GitHub. Github API token is required if --publish is enabled. Please specify the github token using --token.");
            }
        }
    }

    @CommandLine.Command(
        name = "update-build-config",
        description = "updates the build-config.yml file in build-configurations repo")
    public void updateBuildConfig(
        @CommandLine.Option(names = {"-g", "--git"}, description = "Git reference in the <github org>/<github repo> format", required = true, defaultValue = "snowdrop/spring-boot-bom") String gitRef,
        @CommandLine.Option(names = {"-o", "--token"}, description = "Github API token", required = true) String token,
        @CommandLine.Option(names = {"-glu", "--gluser"}, description = "Gitlab user name", required = true) String gluser,
        @CommandLine.Option(names = {"-glt", "--gltoken"}, description = "Gitlab API token", required = true) String gltoken,
        @CommandLine.Option(names = {"-r", "--release"}, description = "release", required = true) String release,
        @CommandLine.Option(names = {"-q", "--qualifier"}, description = "qualifier", required = true) String qualifier,
        @CommandLine.Option(names = {"-m", "--milestone"}, description = "milestone", required = true) String milestone)
        throws Throwable {
        LOG.infof("release: %s; qualifier: %s; milestone: %s", release, qualifier, milestone);
        String[] releaseMMF = release.split("\\.");
        final String gitFullRef = String.format("%s/sb-%s.%s.x", gitRef, releaseMMF[0], releaseMMF[1]);
        Release releaseObj = factory.createFromGitRef(gitFullRef, false, true, release);

        GitConfig config = GitConfig.gitlabConfig(release, gluser, gltoken, buildConfigForkRepoName, Optional.of(String.format("sb-%s.%s.x", releaseMMF[0], releaseMMF[1])));
        git.initRepository(config);

        git.commitAndPush("chore: update " + release + " release issues' key [release-manager]", config, repo -> Stream.of(buildConfigUpdateService
            .updateBuildConfig(repo, releaseObj, release, qualifier, milestone)));
    }
    
    @CommandLine.Command(name = "status", description = "Compute the release status")
    public void status(
        @CommandLine.Option(
            names = {"-g", "--git"},
            description = "Git reference in the <github org>/<github repo>/<branch> format") String gitRef)
        throws Throwable {
        Release release = factory.createFromGitRef(gitRef, false, false);
        final var blocked = new LinkedList<Issue>();
        final var cves = cveService.listCVEs(Optional.ofNullable(release.getVersion()), true);
        release.computeStatus();
        blocked.addAll(cves);
        blocked.addAll(release.getBlocked());
        System.out.println(release.getBlockedNumber() + " blocked issues out of " + release.getConsideredNumber()
            + " considered");
        System.out.println(reportingService.buildAsciiReport(blocked));
    }

    private BasicIssue clone(Release release, String token) throws IOException {
        final var issue = service.clone(release, IssueService.RELEASE_TICKET_TEMPLATE, watchers);
        release.setJiraKey(issue.getKey());
        return issue;
    }
}
