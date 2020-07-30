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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utility {
    private static final String MUSTACHE_PATH = "etc/description.mustache";
    private static final DateTimeFormatter dateTimeParser = ISODateTimeFormat.dateTimeParser();
    public static final String JIRA_SERVER = "https://issues.redhat.com/";
    public static final String JIRA_ISSUES_API = "https://issues.redhat.com/rest/api/2/";
    public static JiraRestClient restClient;
    public static Mustache m;

    static {
        MustacheFactory mf = new DefaultMustacheFactory();
        try {
            m = mf.compile(new FileReader(MUSTACHE_PATH), MUSTACHE_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // jackson databind
    public static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public static void initRestClient(String jiraServerUri, String user, String password) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri(jiraServerUri), user, password);
        try {
            restClient.getSessionClient().getCurrentSession().get().getLoginInfo().getFailedLoginCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public static String generateIssueDescription(Component c) {
        StringWriter writer = new StringWriter();

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("release", c.getParent());
        scopes.put("component", c);

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

    public static String getFormatted(String dateTimeSt) {
        final DateTime jodaDate = toDateTime(dateTimeSt);
        return jodaDate.toString("dd MMM YYYY");
    }

    public static String getURLFor(String issueKey) {
        return JIRA_SERVER + "browse/" + issueKey;
    }

    public static boolean isStringNullOrBlank(String s) {
        return s == null || s.isBlank();
    }
}
