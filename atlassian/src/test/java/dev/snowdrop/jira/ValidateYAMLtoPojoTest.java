package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ValidateYAMLtoPojoTest {
    private static String YAML = "components:\n" +
            " - jira_project: # # https://issues.redhat.com/projects/EAPSUP/issues\n" +
            "   name: hibernate\n" +
            "   version: 5.0.15";

    @Test
    public void yamlToObjectTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        Release release = mapper.readValue(YAML, Release.class);
        assertNotNull(release);
    }

}
