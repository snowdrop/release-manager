package dev.snowdrop.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.snowdrop.jira.atlassian.model.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidateYAMLtoPojoTest {
    private static String YAML =
            "projectKey: SB\n" +
            "title: This is a task\n" +
            "description: lorem ipsum.lorem ipsum.lorem ipsum.lorem ipsum.lorem ipsum.";

    @Test
    public void checkIssueTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Issue issue = mapper.readValue(YAML, Issue.class);

        assertNotNull(issue);
        assertEquals(issue.getProjectKey(),"SB");
        assertEquals(issue.getTitle(),"This is a task");
    }

}
