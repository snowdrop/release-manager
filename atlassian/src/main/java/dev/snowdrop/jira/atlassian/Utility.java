package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class Utility {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String MUSTACHE_PATH = "etc/description.mustache";
    public static final String JIRA_ISSUES_API = "https://issues.redhat.com/rest/api/2/";
    public static Object pojo;
    public static JiraRestClient restClient;

    public static void initRestClient(String jiraServerUri, String user, String password) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri(jiraServerUri), user, password);
        try {
            restClient.getSessionClient().getCurrentSession().get().getLoginInfo().getFailedLoginCount();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            LOG.error(e);
        }
    }

    public static IssueType TASK_TYPE() {
        try {
            // TODO: Add a method able to fetch the IssueTypes and selecting `Request`, if not available `Task`
            return new IssueType(
                    new URI(JIRA_ISSUES_API + "issuetype/3"),
                    Long.valueOf("3"),
                    "A task that needs to be done.",
                    false,
                    "Task",
                    new URI("https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13278&avatarType=issuetype"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static URI jiraServerUri(String uri) {
        if (uri != null) {
            return URI.create(uri);
        } else {
            return URI.create("https://issues.redhat.com/");
        }
    }

    public static void readYaml(String path, Class<?> clazz) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        try {
            pojo = mapper.readValue(new File(path), clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
