package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Utility {
    public static Release release;

    public static IssueType TASK_TYPE() {
        try {
            return new IssueType(
                    new URI("https://issues.redhat.com/rest/api/2/issuetype/3"),
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

    public static void readYaml(String path) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        try {
            release = mapper.readValue(new File(path), Release.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DateTime formatDueDate(String dueDate) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
        return formatter.parseDateTime(dueDate);
    }
}
