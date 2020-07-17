package dev.snowdrop.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static dev.snowdrop.jira.Utility.getFileFromResources;
import static dev.snowdrop.jira.atlassian.Utility.toDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class YAMLParsingAndMustacheProcessingTest {

    private static final String YAML_FILE = "release.yaml";
    private static final String MUSTACHE_FILE = "jira_description.mustache";
    private static Release release;
    private static Mustache m;

    @BeforeAll
    private static void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        release = mapper.readValue(getFileFromResources(YAML_FILE), Release.class);
        // Calculate additional fields such as DueDateFormatted
        release.setDueDateFormatted(toDateTime(release.getDueDate()).toString("dd MMM YYYY"));

        MustacheFactory mf = new DefaultMustacheFactory();
        m = mf.compile(MUSTACHE_FILE);
    }
    @Test
    public void componentWithVersionDescriptionTest() throws URISyntaxException, IOException {
        // Fetch the expected response
        URL resource = YAMLParsingAndMustacheProcessingTest.class.getResource("/component_description.txt");
        String expected = Files.readString(Path.of(resource.toURI()));

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("release", release);

        Component c = release.getComponents().get(0);
        scopes.put("component", c);

        if (c.getIsStarter()) {
            scopes.put("isStarter",true);
            scopes.put("type","starter");
        } else {
            scopes.put("isComponent",true);
            scopes.put("type","component");
        }

        StringWriter writer = new StringWriter();
        m.execute(writer, scopes).flush();
        assertEquals(expected,writer.toString());
    }

    @Test
    public void starterDescriptionTest() throws URISyntaxException, IOException {
        // Fetch the expected response
        URL resource = YAMLParsingAndMustacheProcessingTest.class.getResource("/starter_description.txt");
        String expected = Files.readString(Path.of(resource.toURI()));

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("release", release);

        // Check release.yaml component list
        Component c = release.getComponents().get(2);
        scopes.put("component", c);

        if (c.getIsStarter()) {
            scopes.put("isStarter",true);
            scopes.put("type","starter");
        } else {
            scopes.put("isComponent",true);
            scopes.put("type","component");
        }

        StringWriter writer = new StringWriter();
        m.execute(writer, scopes).flush();
        assertEquals(expected,writer.toString());
    }
}
