package dev.snowdrop.jira.atlassian;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import dev.snowdrop.jira.atlassian.model.Release;
import dev.snowdrop.jira.atlassian.model.ReleaseFactory;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import static dev.snowdrop.jira.atlassian.Service.RELEASE_TICKET_TEMPLATE;

@CommandLine.Command(
		name = "issue-manager", mixinStandardHelpOptions = true, version = "issues-manager 1.0.0"
)
public class Client {
	private static final Logger LOG = Logger.getLogger(Client.class);

	@CommandLine.Option(names = {"-u", "--user"}, description = "JIRA user", required = true, scope = CommandLine.ScopeType.INHERIT)
	private String user;
	@CommandLine.Option(names = {"-p", "--password"}, description = "JIRA password", required = true, scope = CommandLine.ScopeType.INHERIT)
	private String password;
	@CommandLine.Option(names = "--url", description = "URL of the JIRA server", showDefaultValue =
		CommandLine.Help.Visibility.ALWAYS, defaultValue = Utility.JIRA_SERVER, scope = CommandLine.ScopeType.INHERIT)
	private String jiraServerURI;
	@CommandLine.Option(names = {"-w", "--watchers"},
		description = "Comma-separated list of user names to add to watchers",
		scope = CommandLine.ScopeType.INHERIT, split = ",")
	private List<String> watchers;
	
	@Inject
	ReleaseFactory factory;
	
	@Inject
	Service service;
	
	public static void main(String[] argv) throws Exception {
		int exitCode = new CommandLine(new Client()).execute(argv);
		System.exit(exitCode);
	}
	
	@CommandLine.Command(name = "get", description = "Retrieve the specified issue")
	public void get(
		@CommandLine.Parameters(description = "JIRA issue key") String key
	) {
		System.out.println(service.getIssue(key));
	}

	@CommandLine.Command(name = "clone",
			description = "Clone the specified issue using information from the release associated with the specified git reference")
	public void clone(
			@CommandLine.Option(names = {"-g", "--git"},
					description = "Git reference in the <github org>/<github repo>/<branch | tag | hash> format") String gitRef,
			@CommandLine.Parameters(description = "JIRA issue key",
					defaultValue = Service.RELEASE_TICKET_TEMPLATE,
					showDefaultValue = CommandLine.Help.Visibility.ALWAYS) String toCloneFrom
	) {
		final Release release = factory.createFromGitRef(gitRef);
		System.out.println(service.clone(release, toCloneFrom, watchers));
	}

	@CommandLine.Command(name = "create-component",
			description = "Create component requests for the release associated with the specified git reference")
	public void createComponentRequests(
			@CommandLine.Option(names = {"-g", "--git"},
					description = "Git reference in the <github org>/<github repo>/<branch | tag | hash> format") String gitRef
	) {
		final Release release = factory.createFromGitRef(gitRef);
		service.createComponentRequests(release, watchers);
	}

	@CommandLine.Command(name = "delete", description = "Delete the specified comma-separated issues")
	public void delete(
			@CommandLine.Parameters(description = "Comma-separated JIRA issue keys", split = ",") List<String> issues
	) {
		service.deleteIssues(issues);
	}

	@CommandLine.Command(name = "link",
			description = "Link the issue specified by the 'from' option to the issue specified by the 'to' option")
	public void link(
			@CommandLine.Parameters(description = ("JIRA issue key from which a link should be created")) String fromIssue,
			@CommandLine.Option(names = {"-t", "--to"}, description = ("JIRA issue key to link to"), required = true) String toIssue
	) {
		service.linkIssue(fromIssue, toIssue);
	}

	@CommandLine.Command(name = "start-release",
			description = "Start the release process for the release associated with the specified git reference")
	public void startRelease(
			@CommandLine.Option(names = {"-g", "--git"},
					description = "Git reference in the <github org>/<github repo>/<branch | tag | hash> format") String gitRef
	) {
		Release release = factory.createFromGitRef(gitRef);

		BasicIssue issue;
		// first check if we already have a release ticket, in which case we don't need to clone the template
		final String releaseTicket = release.getJiraKey();
		if (!Utility.isStringNullOrBlank(releaseTicket)) {
			try {
				issue = service.getIssue(releaseTicket);
				System.out.printf("Release ticket %s already exists, skipping cloning step", releaseTicket);
			} catch (Exception e) {
				// if we got an exception, assume that it's because we didn't find the ticket
				issue = service.clone(release, RELEASE_TICKET_TEMPLATE, watchers);
			}
		} else {
			// no release ticket was specified, clone
			issue = service.clone(release, RELEASE_TICKET_TEMPLATE, watchers);
		}
		service.createComponentRequests(release, watchers);
		System.out.println(issue);
	}
}
