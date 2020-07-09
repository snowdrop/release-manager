package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;

import java.util.List;

import static dev.snowdrop.jira.atlassian.Utility.*;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String LINK_TYPE = "Dependency";

    public static void linkIssues(String fromIssue, String toIssue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.linkIssue(new LinkIssuesInput(fromIssue, toIssue, LINK_TYPE)).claim();
        LOG.infof("Linked the issue %s with the blocking issue %s", fromIssue, toIssue);
    }

    public static void getIssue(String issueNumber) {
        final IssueRestClient cl = restClient.getIssueClient();
        Issue issue = cl.getIssue(issueNumber).claim();
        LOG.info(issue);
    }

    public static void deleteIssue(String issue) {
        final IssueRestClient cl = restClient.getIssueClient();
        cl.deleteIssue(issue, false).claim();
        LOG.infof("Issue %s deleted", issue);
    }

    public static void deleteIssues(List<String> issues) {
        final IssueRestClient cl = restClient.getIssueClient();
        for (String issue : issues) {
            cl.deleteIssue(issue, false).claim();
            LOG.infof("Issue %s deleted", issue);
        }
    }

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

        } else {
            LOG.warnf("Issue to be cloned not found : %s !", issueToClone);
        }
    }

    public static void createReleaseIssues() {
        final IssueRestClient cl = restClient.getIssueClient();
        Release release = (Release) pojo;

        for (Component component : release.getComponents()) {
            IssueInputBuilder iib = new IssueInputBuilder();
            iib.setProjectKey(component.getProjectKey());
            iib.setSummary(release.getTitle());
            iib.setDescription(generateIssueDescription(release, component));
            iib.setIssueType(TASK_TYPE());
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

    public static void createIssue() {
        final IssueRestClient cl = restClient.getIssueClient();
        dev.snowdrop.jira.atlassian.model.Issue issuePOJO = (dev.snowdrop.jira.atlassian.model.Issue) pojo;

        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey(issuePOJO.getProjectKey());
        iib.setSummary(issuePOJO.getTitle());
        iib.setDescription(issuePOJO.getDescription());
        iib.setIssueType(TASK_TYPE());
        IssueInput issue = iib.build();
        BasicIssue issueObj = cl.createIssue(issue).claim();
        LOG.infof("Issue %s created successfully", issueObj.getKey());
    }

}
