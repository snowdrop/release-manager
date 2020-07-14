package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Release;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {
    private static String YAML =
            "version: 2.3.0.RELEASE\n" +
            "components:\n" +
            " - jiraProject: EAPSUP\n" +
            "   name: hibernate\n" +
            "   version: 5.0.15\n" +
            " - jiraProject: RESTEASY\n" +
            "   name: RESTEasy\n" +
            "   isStarter: true\n" +
            "cves:\n" +
            "  - jiraProject: ENTSBT\n" +
            "    issue: 1010\n" +
            "  - jiraProject: ENTSBT\n" +
            "    issue: 1020";

    @Test
    public void checkDataOfAComponentTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Release release = mapper.readValue(YAML, Release.class);

        assertNotNull(release);
        assertEquals(release.getComponents().get(0).getVersion(),"5.0.15");
        assertEquals(release.getComponents().get(0).getJiraProject(),"EAPSUP");
        assertEquals(release.getComponents().get(0).getName(),"hibernate");
    }

    @Test
    public void checkIsAStarterTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Release release = mapper.readValue(YAML, Release.class);

        assertNotNull(release);
        assertEquals(release.getComponents().get(1).getJiraProject(),"RESTEASY");
        assertEquals(release.getComponents().get(1).getName(),"RESTEasy");
        assertEquals(release.getComponents().get(1).getIsStarter(),"true");
    }

    @Test
    public void checkCVEsTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Release release = mapper.readValue(YAML, Release.class);

        assertNotNull(release);
        assertEquals(release.getCves().get(0).getJiraProject(),"ENTSBT");
        assertEquals(release.getCves().get(0).getIssue(),"1010");

        assertEquals(release.getCves().get(1).getJiraProject(),"ENTSBT");
        assertEquals(release.getCves().get(1).getIssue(),"1020");
    }



}
