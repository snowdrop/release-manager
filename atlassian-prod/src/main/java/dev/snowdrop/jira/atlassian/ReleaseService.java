package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Cve;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

import java.util.stream.StreamSupport;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class ReleaseService extends Service {
    private static final Logger LOG = Logger.getLogger(ReleaseService.class);

    /*
     * Clone an existing JIRA issue to create the Release issue
     * If subtasks exist, then the corresponding issues will be created and linked to the release issue
     *
     * If CVEs have been defined within the Release YAML, then we will link them also as blocking links to the release issue
     *
     */
    public static void cloneIssue(String issueToClone) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(issueToClone).claim();
        Release release = (Release) pojo;
        if (issueToClone != null) {
            // Create the cloned task
            IssueInputBuilder iib = new IssueInputBuilder();
            iib.setProjectKey(issue.getProject().getKey());
            iib.setDescription(issue.getDescription());
            iib.setSummary(release.getLongVersionName());
            iib.setIssueType(TASK_TYPE());
            IssueInput ii = iib.build();
            BasicIssue issueCloned = cl.createIssue(ii).claim();
            LOG.infof("Issue cloned: %s", issueCloned.getKey());

            // Check if CVEs exist within the Release and link them to the new release ticket created
            for(Cve cve : release.getCves()){
                linkIssues(issueCloned.getKey(),cve.getJiraProject() + "-" + cve.getIssue());
            }

            // Get the list of the sub-tasks
            Iterable<Subtask> subTasks = issue.getSubtasks();
            if (CollectionSize(subTasks) > 0) {
                for (Subtask subtask : subTasks) {
                    // Fetch the SubTask from the server as the subTask object dont contain the assignee :-(
                    Issue fetchSubTask = cl.getIssue(subtask.getIssueKey()).claim();
                    if (fetchSubTask != null) {
                        // Create a sub-task that we will link to the parent
                        iib = new IssueInputBuilder();
                        iib.setProjectKey(issue.getProject().getKey());
                        iib.setSummary(subtask.getSummary());
                        iib.setIssueType(subtask.getIssueType());
                        if (fetchSubTask.getAssignee() != null) {
                            iib.setAssignee(fetchSubTask.getAssignee());
                        }
                        iib.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", issueCloned.getKey()));
                        ii = iib.build();
                        BasicIssue subTaskIssue = cl.createIssue(ii).claim();
                        LOG.infof("Sub task issue cloned: %s", subTaskIssue.getKey());
                    }
                }
            }

        } else {
            LOG.warnf("Issue to be cloned not found : %s !", issueToClone);
        }
    }

    public static void createComponentIssues() {
        final IssueRestClient cl = restClient.getIssueClient();
        Release release = (Release) pojo;

        for (Component component : release.getComponents()) {
            IssueInputBuilder iib = new IssueInputBuilder();
            iib.setProjectKey(component.getJiraProject());
            iib.setSummary(release.getTitle());
            iib.setDescription(generateIssueDescription(release, component));
            iib.setIssueType(TASK_TYPE());
            iib.setDueDate(toDateTime(release.getDueDate()));
            /*
             TODO: To be investigated

             snowdrop-bot user cannot set the following field
             iib.setDueDate(formatDueDate(release.getDueDate()));

             See: https://github.com/snowdrop/jira-tool/issues/7

             IF we know the custom_field value, then we can fill the following field
             iib.setFieldValue(TARGET_RELEASE_CUSTOMFIELD_ID,setTargetRelease());
            */

            IssueInput issue = iib.build();
            BasicIssue newIssue = cl.createIssue(issue).claim();
            LOG.infof("Issue %s created successfully", newIssue.getKey());

            /*
             * If the Release jira key field is not null, then we will link the newly component/starter created Issue to the
             * release issue
             */
            if (release.getJiraKey() != null) {
                linkIssues(release.getJiraKey(),newIssue.getKey());
            }
        }
    }

    private static long CollectionSize(Iterable<Subtask> data) {
        return StreamSupport.stream(data.spliterator(), false).count();
    }

}
