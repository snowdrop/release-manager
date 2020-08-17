package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.IssueSource;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class ReleaseService extends Service {
    public static final String RELEASE_TICKET_TEMPLATE = "ENTSBT-323";
    private static final Logger LOG = Logger.getLogger(ReleaseService.class);

    public static void startRelease(Args args) {
        Release release = Release.createFromGitRef(gitRefOrFail(args));

        // first check if we already have a release ticket, in which case we don't need to clone the template
        final String releaseTicket = release.getJiraKey();
        if (!Utility.isStringNullOrBlank(releaseTicket)) {
            final IssueRestClient cl = restClient.getIssueClient();
            cl.getIssue(releaseTicket)
            // set the JIRA key of the release if not already set
            .done(i -> LOG.infof("Release ticket %s already exists, skipping cloning step", releaseTicket))
            // if the issue doesn't exist, create it by cloning the template ticket, which should set the JIRA key
            .fail(e -> clone(release, RELEASE_TICKET_TEMPLATE, args.watcherList));
        } else {
            // no release ticket was specified, clone
            clone(release, RELEASE_TICKET_TEMPLATE, args.watcherList);
        }
        createComponentRequests(release, args.watcherList);
    }

    private static String gitRefOrFail(Args args) {
        if (args.gitRef == null) {
            throw new IllegalArgumentException("Must provide a Git reference to retrieve release.yml from");
        }
        return args.gitRef;
    }

    /*
     * Clone an existing JIRA issue to create the Release issue
     * If subtasks exist, then the corresponding issues will be created and linked to the release issue
     *
     * If CVEs have been defined within the Release YAML, then we will link them also as blocking links to the release issue
     *
     */
    public static void cloneIssue(Args args) {
        final String toCloneFrom = args.issue != null ? args.issue : RELEASE_TICKET_TEMPLATE;
        Release release = Release.createFromGitRef(gitRefOrFail(args));

        clone(release, toCloneFrom, args.watcherList);
    }

    private static void clone(Release release, String toCloneFrom, final String watcherList) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(toCloneFrom).claim();
        // Create the cloned task
        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey(issue.getProject().getKey());
        iib.setDescription(issue.getDescription());
        iib.setSummary(release.getLongVersionName());
        iib.setIssueTypeId(issue.getIssueType().getId());
        IssueInput ii = iib.build();
        BasicIssue clonedIssue = cl.createIssue(ii).claim();
        final String clonedIssueKey = clonedIssue.getKey();
        addWatchers(clonedIssueKey, watcherList);
        LOG.infof("Issue cloned: %s", getURLFor(clonedIssueKey));

        try {
            cl.linkIssue(new LinkIssuesInput(clonedIssueKey, toCloneFrom, "Cloners")).claim();
        } catch (Exception e) {
            LOG.error("Couldn't link " + clonedIssueKey + " as clone of " + toCloneFrom, e);
        }

        // Check if CVEs exist within the Release and link them to the new release ticket created
        for (dev.snowdrop.jira.atlassian.model.Issue cve : release.getCves()) {
            linkIssue(clonedIssueKey, cve.getProject() + "-" + cve.getKey());
        }

        // Get the list of the sub-tasks
        Iterable<Subtask> subTasks = issue.getSubtasks();
        for (Subtask subtask : subTasks) {
            // Fetch the SubTask from the server as the subTask object dont contain the assignee :-(
            Issue fetchSubTask = cl.getIssue(subtask.getIssueKey()).claim();
            if (fetchSubTask != null) {
                // Create a sub-task that we will link to the parent
                iib = new IssueInputBuilder();
                iib.setProjectKey(issue.getProject().getKey());
                iib.setSummary(subtask.getSummary());
                iib.setIssueTypeId(subtask.getIssueType().getId());
                if (fetchSubTask.getAssignee() != null) {
                    iib.setAssignee(fetchSubTask.getAssignee());
                }
                iib.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", clonedIssueKey));
                ii = iib.build();
                BasicIssue subTaskIssue = cl.createIssue(ii).claim();
                addWatchers(subTaskIssue.getKey(), watcherList);
                LOG.infof("Sub task issue cloned: %s", subTaskIssue.getKey());
            }
        }

        // set the JIRA key on the release for further processing
        release.setJiraKey(clonedIssueKey);
    }

    public static void createComponentIssues(Args args) {
        final Release release = Release.createFromGitRef(gitRefOrFail(args));
        createComponentRequests(release, args.watcherList);
    }

    private static void createComponentRequests(Release release, final String watcherList) {
        final IssueRestClient cl = restClient.getIssueClient();
        final String jiraKey = release.getJiraKey();

        for (Component component : release.getComponents()) {
            var issue = getIssueInput(component);
            var componentIssue = cl.createIssue(issue).claim();
            addWatchers(componentIssue.getKey(), watcherList);
            LOG.infof("Issue %s created successfully", getURLFor(componentIssue.getKey()));

            /*
             * If the Release jira key field is not null, then we will link the newly component/starter created Issue to the
             * release issue
             */
            if (jiraKey != null) {
                linkIssue(jiraKey, componentIssue.getKey());
            }

            // if component also defines a product field, then we should create a ticket for the associated product team
            // and link it to the component request
            final var product = component.getProduct();
            if (product != null) {
                issue = getIssueInput(product);
                var productIssue = cl.createIssue(issue).claim();
                addWatchers(productIssue.getKey(), watcherList);
                LOG.infof("Product Issue %s created successfully for %s component", getURLFor(productIssue.getKey()),
                component.getName());
                // link the newly created product issue with our component issue
                linkIssue(componentIssue.getKey(), productIssue.getKey());
            }
        }
    }

    private static IssueInput getIssueInput(IssueSource source) {
        IssueInputBuilder iib = new IssueInputBuilder();
        final var jira = source.getJira();
        iib.setProjectKey(jira.getProject());
        iib.setSummary(source.getTitle());
        iib.setDescription(source.getDescription());
        iib.setIssueTypeId(jira.getIssueTypeId());
        iib.setDueDate(toDateTime(source.getParent().getSchedule().getDueDate()));
                /*
                 TODO: To be investigated

                 snowdrop-bot user cannot set the following field
                 iib.setDueDate(formatDueDate(release.getDueDate()));

                 See: https://github.com/snowdrop/jira-tool/issues/7

                 IF we know the custom_field value, then we can fill the following field
                 iib.setFieldValue(TARGET_RELEASE_CUSTOMFIELD_ID,setTargetRelease());
                */

        return iib.build();
    }

    private static long CollectionSize(Iterable<Subtask> data) {
        return StreamSupport.stream(data.spliterator(), false).count();
    }

    private static void addWatchers(final String issueKey, final String watcherList) {
        final IssueRestClient cl = restClient.getIssueClient();
        try {
            final URI jiraUri = new URI(JIRA_ISSUES_API + "issue/" + issueKey + "/watchers");
            if (watcherList != null && watcherList.length() > 0) {
                Arrays.asList(watcherList.split(",")).forEach(associate -> {
                    LOG.debug("associate: " + associate);
                    cl.addWatcher(jiraUri, associate).claim();
                });
            }
        } catch (URISyntaxException e) {
            LOG.error("Error adding wather: " + e.getMessage());
        }
    }
}
