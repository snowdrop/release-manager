package dev.snowdrop.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
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

        StringWriter writer = new StringWriter();
        m.execute(writer, scopes).flush();
        assertEquals(writer.toString().contains("2.3.0.RELEASE"),true);
    }
}
