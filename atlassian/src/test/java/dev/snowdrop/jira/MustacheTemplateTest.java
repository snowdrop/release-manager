package dev.snowdrop.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MustacheTemplateTest {

    private static final String YAML_FILE = "release.yaml";
    private static final String MUSTACHE_FILE = "description.mustache";
    private String expected = "The snowdrop team is really pleased to contact you as we will release in 12 weeks a new Snowdrop BOM based on the following Spring Boot version: 2.3.0.RELEASE\n" +
            "\n" +
            "As product owner of the following component: Hibernate, we would like to know if you plan to release a new component for the version: 5.0.15\n" +
            "that we will start to test the: 08/18/2020.\n" +
            "\n" +
            "If you don't plan to release a new component, could you please test and control if your component will continue to work with this version of spring boot\n" +
            "We expect, in this case, that you will perform QE test and provide us a signoff !\n" +
            "\n" +
            "The EOL of this Snowdrop release is scheduled: September 2021\n" +
            "\n" +
            "We expect to get from you:\n" +
            "\n" +
            "- Product name and version (e.g. JWS - 5.3.1)\n" +
            "- component & Version supported (e.g. Apache Tomcat - 9.0.30-redhat-00001)\n" +
            "- PNC/Indy URL (e.g. http://indy.psi.redhat.com/browse/maven/...)\n" +
            "- MRRC URL, if already released (e.g. https://maven.repository.redhat.com/ga/...)";

    @Test
    public void issueDescriptionTest() throws URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        Release release = mapper.readValue(
                Paths.get(ClassLoader.getSystemResource(YAML_FILE).toURI()).toFile(),
                Release.class);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile(MUSTACHE_FILE);

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("release", release);

        Component c = release.getComponents().get(0);
        scopes.put("component", c);

        if (c.getIsStarter() == null) {
            scopes.put("isComponent",true);
            scopes.put("type","component");
        } else {
            scopes.put("isStarter",true);
            scopes.put("type","starter");
        }

        StringWriter writer = new StringWriter();
        m.execute(writer, scopes).flush();
        assertEquals(expected,writer.toString());
    }
}
