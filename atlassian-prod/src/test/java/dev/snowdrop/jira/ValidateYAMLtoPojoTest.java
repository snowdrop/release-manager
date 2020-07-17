package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {

    private static Release release;

    @BeforeAll
    public static void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        release = mapper.readValue(getFileFromResources("release.yaml"), Release.class);
    }

    @Test
    public void checkDataOfAComponentTest() {
        assertNotNull(release);
        assertEquals(release.getComponents().get(0).getVersion(),"5.0.15");
        assertEquals(release.getComponents().get(0).getJiraProject(),"EAPSUP");
        assertEquals(release.getComponents().get(0).getName(),"Hibernate");
    }

    @Test
    public void checkIsAStarterTest() throws JsonProcessingException {
        assertNotNull(release);
        assertEquals(release.getComponents().get(2).getJiraProject(),"RESTEASY");
        assertEquals(release.getComponents().get(2).getName(),"RESTEasy");
        assertEquals(release.getComponents().get(2).getIsStarter(),true);
    }

    @Test
    public void checkSkipCreationTest() throws JsonProcessingException {
        assertNotNull(release);
        assertEquals(release.getComponents().get(0).getSkipCreation(),false);
        assertEquals(release.getComponents().get(1).getSkipCreation(),true);
    }

    @Test
    public void checkCVEsTest() throws JsonProcessingException {
        assertNotNull(release);
        assertEquals(release.getCves().get(0).getJiraProject(),"ENTSBT");
        assertEquals(release.getCves().get(0).getIssue(),"1010");

        assertEquals(release.getCves().get(1).getJiraProject(),"ENTSBT");
        assertEquals(release.getCves().get(1).getIssue(),"1020");
    }

    // Get file from classpath, resources folder
    private static File getFileFromResources(String fileName) {
        ClassLoader classLoader = ValidateYAMLtoPojoTest.class.getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }
}
