package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Component;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {
    private static String YAML =
            "version: 2.3.0.RELEASE\n" +
            "components:\n" +
            " - jiraProject: # # https://issues.redhat.com/projects/EAPSUP/issues\n" +
            "   name: hibernate\n" +
            "   version: 5.0.15";

    @Test
    public void yamlToObjectTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        Release release = mapper.readValue(YAML, Release.class);
        assertNotNull(release);
        assertEquals(release.getComponents().get(0).getVersion(),"5.0.15");
    }

    @Test
    public void snakeYamlToObjectTest() {
        Constructor constructor = new Constructor(Release.class);//Release.class is root
        TypeDescription componentDescription = new TypeDescription(Release.class);
        componentDescription.putListPropertyType("components", Component.class);
        constructor.addTypeDescription(componentDescription);
        Yaml yaml = new Yaml(constructor);
        Release release = yaml.load(YAML);
        assertNotNull(release);
    }

}
