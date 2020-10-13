package dev.snowdrop.release.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import dev.snowdrop.release.model.CVE;
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
    private static final long UNRESOLVED_CVES = 12347131;
    private static final Matcher CVE_PATTERN = Pattern.compile(".*(CVE-\\d{4}-\\d{1,6}).*").matcher("");
    private static final Matcher BZ_PATTERN = Pattern.compile(".*https://bugzilla.redhat.com/show_bug.cgi\\?id=(\\d{7}).*").matcher("");
    
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
    
    public Iterable<CVE> listCVEs(Optional<String> releaseVersion) {
        final var searchResult = new SearchResult[1];
        final var searchClient = restClient.getSearchClient();
        releaseVersion.ifPresentOrElse(
            version -> searchResult[0] = searchClient.searchJql("project = " + DEFAULT_JIRA_PROJECT + " AND text ~ \"cve-*\" AND fixVersion = " + version).claim(),
            () -> searchResult[0] = searchClient.getFilter(UNRESOLVED_CVES).flatMap(f -> searchClient.searchJql(f.getJql())).claim()
        );
        final var issues = searchResult[0].getIssues();
        final var cves = new LinkedList<CVE>();
        for (Issue issue : issues) {
            final var resolution = issue.getResolution();
            final var resolutionAsString = resolution == null ? "Unspecified" : resolution.getName();
            final var versions = issue.getFixVersions();
            final List<String> fixVersions;
            if (versions != null) {
                fixVersions = new LinkedList<>();
                versions.forEach(v -> fixVersions.add(v.getName()));
            } else {
                fixVersions = Collections.emptyList();
            }
            var summary = issue.getSummary();
            String id = "";
            // extract CVE id from summary
            if (CVE_PATTERN.reset(summary).matches()) {
                id = CVE_PATTERN.group(1);
                // remove id from summary
                summary = summary.substring(CVE_PATTERN.end(1)).trim();
            }
            final var cve = new CVE(issue.getKey(), summary, resolutionAsString, fixVersions, issue.getStatus().getName());
            cve.setId(id);
            final var labels = issue.getLabels();
            final var description = issue.getDescription();
            if (description != null) {
                final var lines = description.lines()
                    .filter(Predicate.not(String::isBlank))
                    .iterator();
                boolean impactFound = false;
                while (lines.hasNext()) {
                    var line = lines.next();
                    // first line should be the one where impact is recorded
                    if (!impactFound && line.startsWith("Impact")) {
                        cve.setImpact(line.substring(line.indexOf(':') + 1).trim());
                        impactFound = true;
                        continue;
                    }
                    
                    // only check for BZ link after CVE is found since that's where it is (at least, in the JIRAs we've seen so far)
                    if (BZ_PATTERN.reset(line).matches()) {
                        cve.setBugzilla(BZ_PATTERN.group(1));
                        break;
                    }
                }
            }
            cves.add(cve);
        }
        return cves;
    }
    
    public void linkCVEs(String releaseVersion, String issue) {
        for (var cve : listCVEs(Optional.of(releaseVersion))) {
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
