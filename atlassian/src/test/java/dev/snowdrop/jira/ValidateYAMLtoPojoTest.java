package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {
    private static String YAML =
            "version: 2.3.0.RELEASE\n" +
            "artifacts:\n" +
            " - jira-project: https://issues.redhat.com/projects/EAPSUP/issues\n" +
            "   name: hibernate\n" +
            "   version: 5.0.15";

    @Test
    public void yamlToObjectTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        Release release = mapper.readValue(YAML, Release.class);
        assertNotNull(release);
        assertEquals(release.getArtifacts().get(0).getVersion(),"5.0.15");
    }

/*    @Test
    public void snakeYamlToObjectTest() {
        Constructor constructor = new Constructor(Release.class);//Release.class is root
        TypeDescription componentDescription = new TypeDescription(Release.class);
        componentDescription.putListPropertyType("components", Component.class);
        constructor.addTypeDescription(componentDescription);
        Yaml yaml = new Yaml(constructor);
        Release release = yaml.load(YAML);
        assertNotNull(release);
    }*/

    static class Release {
        private String version;
        private List<Artifact> artifacts;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<Artifact> getArtifacts() {
            return artifacts;
        }

        public void setArtifacts(List<Artifact> artifacts) {
            this.artifacts = artifacts;
        }

    }

    static class Artifact {
        @JsonProperty("jira-project")
        private String jiraProject;
        private String name;
        private String version;
        public String getJiraProject() {
            return jiraProject;
        }

        public void setJiraProject(String jiraProject) {
            this.jiraProject = jiraProject;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

    }
}
