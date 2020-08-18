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

import static dev.snowdrop.jira.atlassian.Utility.*;

public class ReleaseService extends Service {
	private static final Logger LOG = Logger.getLogger(ReleaseService.class);
	public static final String RELEASE_TICKET_TEMPLATE = "ENTSBT-323";

	public static BasicIssue clone(Release release, String toCloneFrom) {
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
				LOG.infof("Sub task issue cloned: %s", subTaskIssue.getKey());
			}
		}

		// set the JIRA key on the release for further processing
		release.setJiraKey(clonedIssueKey);
		return clonedIssue;
	}

	public static void createComponentRequests(Release release) {
		final IssueRestClient cl = restClient.getIssueClient();
		final String jiraKey = release.getJiraKey();

		for (Component component : release.getComponents()) {
			var issue = getIssueInput(component);
			var componentIssue = cl.createIssue(issue).claim();
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
}
