package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Utility {
    private static final Logger LOG = Logger.getLogger(Service.class);
    private static final String MUSTACHE_PATH = "etc/description.mustache";
    private static DateTimeFormatter dateTimeParser = ISODateTimeFormat.dateTimeParser();
    public static final String JIRA_ISSUES_API = "https://issues.redhat.com/rest/api/2/";
    public static Object pojo;
    public static JiraRestClient restClient;
    public static Mustache m;

    static {
        MustacheFactory mf = new DefaultMustacheFactory();
        try {
            m = mf.compile(new FileReader(MUSTACHE_PATH),MUSTACHE_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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

    public static IssueType SUB_TASK_TYPE() {
        try {
            // TODO: Add a method able to fetch the IssueTypes and selecting `Request`, if not available `Task`
            return new IssueType(
                    new URI(JIRA_ISSUES_API + "issuetype/5"),
                    5L,
                    "Sub-task",
                    true,
                    "The sub-task of the issue",
                    new URI("https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13276&avatarType=issuetype"));
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

    public static String generateIssueDescription(Release r, Component c) {
        StringWriter writer = new StringWriter();

        // Calculate additional fields such as DueDate
        r.setDueDateFormatted(toDateTime(r.getDueDate()).toString("dd MMM YYYY"));

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("release", r);
        scopes.put("component", c);

        if (c.getIsStarter() == null) {
            scopes.put("isComponent",true);
            scopes.put("type","component");
        } else {
            scopes.put("isStarter",true);
            scopes.put("type","starter");
        }

        try {
            m.execute(writer, scopes).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static Iterable<Version> setFixVersion() {
        List<Version> versions = new ArrayList<Version>();
        // TODO: Add a method able to fetch the versions and match the one passed within the Release
        Version version = null;
        try {
            version = new Version(
                    new URI(JIRA_ISSUES_API + "/version/12345960"),
                    12345960L,
                    "2.3.0.GA",
                    "2.3.0.GA",
                    false,
                    false,
                    null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        versions.add(version);
        return versions;
    }

    public static Version setTargetRelease() {
        try {
            return new Version(
                    new URI(JIRA_ISSUES_API + "/version/12345960"),
                    12345960L,
                    "2.3.0.GA",
                    "Spring Boot 2.3 Release",
                    false,
                    false,
                    null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DateTime toDateTime(String dateTimeSt) {
        return dateTimeParser.parseDateTime(dateTimeSt);
    }
}
