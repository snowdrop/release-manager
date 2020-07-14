package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

import java.util.stream.StreamSupport;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class ReleaseService extends Service {
    private static final Logger LOG = Logger.getLogger(ReleaseService.class);

    public static void cloneIssue(String issueToClone) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(issueToClone).claim();
        if (issueToClone != null) {
            // Create the cloned task
            IssueInputBuilder iib = new IssueInputBuilder();
            iib.setProjectKey(issue.getProject().getKey());
            iib.setDescription(issue.getDescription());
            iib.setSummary("[Spring Boot 2.4] Release steps CR [Q1-2121]");
            iib.setIssueType(TASK_TYPE());
            IssueInput ii = iib.build();
            BasicIssue issueCloned = cl.createIssue(ii).claim();
            LOG.infof("Issue cloned: %s", issueCloned.getKey());

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
            iib.setProjectKey(component.getProjectKey());
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
            BasicIssue issueObj = cl.createIssue(issue).claim();
            LOG.infof("Issue %s created successfully", issueObj.getKey());
        }
    }

    private static long CollectionSize(Iterable<Subtask> data) {
        return StreamSupport.stream(data.spliterator(), false).count();
    }

}
