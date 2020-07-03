package dev.snowdrop.jira.atlassian;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Utility {
    public static Release release;

    public static final String TEMPLATE = "The snowdrop team is really pleased to contact you as we will release in 12 weeks a new Snowdrop BOM based on the following Spring Boot version: *%s*\n" +
            "As product owner of the following component and/or starter: A.B.C, we would like to know if you plan to release a new component (see version defined within the BOM file) that we will start to test at: *%s*.\n" +
            "If you don't plan to release a new component/starter, could you please test and control if your component will continue to work with this version of spring boot and patch it for CVEs.\n" +
            "\n" +
            "The EOL of this Snowdrop release is scheduled: %s\n" +
            "\n" +
            "We expect to get from you:\n" +
            "\n" +
            "Product name and version : Code Name - Version (e.g. JWS - 5.3.1)\n" +
            "Artifact / Component name & Version supported: Apache Tomcat - 9.0.30-redhat-00001\n" +
            "PNC/Indy URL : http://indy.psi.redhat.com/browse/maven\n" +
            "MRRC URL (if already released) : https://maven.repository.redhat.com/ga/\n" +
            "where A.B.C could be: Apache Tomcat Embed, Undertow, Resteasy, ...\"";

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

    public static void readYaml() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File(classLoader.getResource("release.yaml").getFile());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        release = mapper.readValue(file, Release.class);
    }
}
