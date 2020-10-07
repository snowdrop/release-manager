package dev.snowdrop.release.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.IssueSource;
import dev.snowdrop.release.model.Release;
import io.atlassian.util.concurrent.Promise;
import org.jboss.logging.Logger;

import static dev.snowdrop.release.model.Issue.DEFAULT_JIRA_PROJECT;
import static dev.snowdrop.release.services.Utility.JIRA_ISSUES_API;
import static dev.snowdrop.release.services.Utility.getURLFor;
import static dev.snowdrop.release.services.Utility.toDateTime;

@ApplicationScoped
public class Service {
    public static final String RELEASE_TICKET_TEMPLATE = "ENTSBT-323";
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String LINK_TYPE = "Blocks";
    
    @Inject
    JiraRestClient restClient;
    
    public void linkIssue(String fromIssue, String toIssue) {
        final var cl = restClient.getIssueClient();
        final Promise<Issue> toPromise = cl.getIssue(toIssue)
            .fail(e -> LOG.errorf("Couldn't retrieve %s issue to link to: %s", toIssue, e.getLocalizedMessage()));
        
        cl.linkIssue(new LinkIssuesInput(toIssue, fromIssue, LINK_TYPE))
            .fail(e -> LOG.errorf("Exception linking %s to %s: %s", fromIssue, toIssue, e.getLocalizedMessage()))
            .claim();
        
        final Issue to = toPromise.claim();
        LOG.infof("Linked %s with the blocking issue %s: %s", getURLFor(fromIssue), toIssue, to.getSummary());
    }
    
    public Issue getIssue(String issueNumber) {
        final var cl = restClient.getIssueClient();
        return cl.getIssue(issueNumber).claim();
    }
    
    public void deleteIssues(List<String> issues) {
        final var cl = restClient.getIssueClient();
        for (String issue : issues) {
            cl.deleteIssue(issue, false).claim();
            LOG.infof("Issue %s deleted", issue);
        }
    }
    
    public void linkCVEs(Release release, String issue) {
        // Check if CVEs exist within the Release and link them to the new release ticket created
        final var searchResult = restClient.getSearchClient().searchJql("project = " + DEFAULT_JIRA_PROJECT +
            " AND text ~ \"cve-*\" AND fixVersion = " + release.getVersion()).claim();
        for (var cve : searchResult.getIssues()) {
            linkIssue(issue, cve.getKey());
        }
    }
    
    public BasicIssue clone(Release release, String toCloneFrom, List<String> watchers) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(toCloneFrom).claim();
        // Create the cloned task
        IssueInputBuilder iib = new IssueInputBuilder();
        final var projectKey = release.getProjectKey();
        iib.setProjectKey(projectKey);
        iib.setDescription(issue.getDescription());
        iib.setSummary(release.getLongVersionName());
        iib.setIssueTypeId(issue.getIssueType().getId());
        BasicIssue clonedIssue = cl.createIssue(iib.build()).claim();
        final String clonedIssueKey = clonedIssue.getKey();
        addWatchers(clonedIssueKey, watchers);
        LOG.infof("Issue cloned: %s", getURLFor(clonedIssueKey));
        
        try {
            cl.linkIssue(new LinkIssuesInput(clonedIssueKey, toCloneFrom, "Cloners")).claim();
        } catch (Exception e) {
            LOG.error("Couldn't link " + clonedIssueKey + " as clone of " + toCloneFrom, e);
        }
        
        // Get the list of the sub-tasks
        Iterable<Subtask> subTasks = issue.getSubtasks();
        for (Subtask subtask : subTasks) {
            // Fetch the SubTask from the server as the subTask object dont contain the assignee :-(
            Issue fetchSubTask = cl.getIssue(subtask.getIssueKey()).claim();
            if (fetchSubTask != null) {
                // Create a sub-task that we will link to the parent
                iib = new IssueInputBuilder();
                iib.setProjectKey(projectKey);
                iib.setSummary(subtask.getSummary());
                iib.setIssueTypeId(subtask.getIssueType().getId());
                if (fetchSubTask.getAssignee() != null) {
                    iib.setAssignee(fetchSubTask.getAssignee());
                }
                iib.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", clonedIssueKey));
                BasicIssue subTaskIssue = cl.createIssue(iib.build()).claim();
                addWatchers(subTaskIssue.getKey(), watchers);
                LOG.infof("Sub task issue cloned: %s", subTaskIssue.getKey());
            }
        }
        
        // set the JIRA key on the release for further processing
        release.setJiraKey(clonedIssueKey);
        return clonedIssue;
    }
    
    public void createComponentRequests(Release release, List<String> watchers) {
        final String jiraKey = release.getJiraKey();
        
        for (Component component : release.getComponents()) {
            final var componentIssue = createIssue(component, watchers);
            
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
                final var productIssue = createIssue(product, watchers);
                // link the newly created product issue with our component issue
                linkIssue(componentIssue.getKey(), productIssue.getKey());
            }
        }
    }
    
    private BasicIssue createIssue(IssueSource source, List<String> watchers) {
        final IssueRestClient cl = restClient.getIssueClient();
        var issue = getIssueInput(source);
        BasicIssue componentIssue;
        try {
            componentIssue = cl.createIssue(issue).claim();
        } catch (Exception e) {
            LOG.errorf("Couldn't create request for %s", source);
            throw e;
        }
        addWatchers(componentIssue.getKey(), watchers);
        LOG.infof("Issue %s created successfully for %s component", getURLFor(componentIssue.getKey()), source.getName());
        return componentIssue;
    }
    
    private static IssueInput getIssueInput(IssueSource source) {
        IssueInputBuilder iib = new IssueInputBuilder();
        final var jira = source.getJira();
        iib.setProjectKey(jira.getProject());
        iib.setIssueTypeId(jira.getIssueTypeId());
        jira.getAssignee().ifPresent(iib::setAssigneeName);
        iib.setSummary(source.getTitle());
        iib.setDescription(source.getDescription());
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
    
    private void addWatchers(final String issueKey, final List<String> watchers) {
        final IssueRestClient cl = restClient.getIssueClient();
        try {
            final URI jiraUri = new URI(JIRA_ISSUES_API + "issue/" + issueKey + "/watchers");
            if (watchers != null && !watchers.isEmpty()) {
                watchers.forEach(associate -> {
                    LOG.debug("associate: " + associate);
                    cl.addWatcher(jiraUri, associate).claim();
                });
            }
        } catch (URISyntaxException e) {
            LOG.error("Error adding watcher: " + e.getMessage());
        }
    }
}
