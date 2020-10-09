package dev.snowdrop.release.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.snowdrop.release.services.Utility;

@JsonPropertyOrder({"name", "jira", "product", "properties"})
public class Component implements IssueSource {
    private static final String COMPONENT_TEMPLATE = "component.mustache";
    
    @JsonProperty
    private Issue jira;
    
    @JsonProperty
    private Issue product;
    
    @JsonProperty
    private String name;
    
    @JsonProperty
    private List<String> properties;
    
    @JsonIgnore
    private List<Artifact> artifacts;
    
    @JsonIgnore
    private Release parent;
    
    @Override
    public Release getParent() {
        return parent;
    }
    
    public void setParent(Release release) {
        this.parent = release;
    }
    
    @Override
    public String getName() {
        if (Utility.isStringNullOrBlank(name)) {
            // infer name from first listed property
            final String s = properties.get(0);
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } else {
            return name;
        }
    }
    
    @Override
    public String getTitle() {
        return getName() + " compatibility information for Spring Boot " + parent.getVersion();
    }
    
    @Override
    public Issue getJira() {
        return jira;
    }
    
    public Issue getProductIssue() {
        return product;
    }
    
    public Product getProduct() {
        if (product == null) {
            return null;
        }
        return new Product(this);
    }
    
    public List<Artifact> getArtifacts() {
        this.artifacts = new LinkedList<>();
        final Map<String, List<Artifact>> artifacts = parent.getPOM().getArtifacts();
        for (String property : properties) {
            this.artifacts.addAll(artifacts.getOrDefault(property, Collections.emptyList()));
        }
        return this.artifacts;
    }
    
    @Override
    public String getDescription() {
        StringWriter writer = new StringWriter();
        try {
            Utility.mf.compile(COMPONENT_TEMPLATE).execute(writer, this).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
    
    public String getReleaseDate() {
        return getParentSchedule().getFormattedReleaseDate();
    }
    
    private Schedule getParentSchedule() {
        return getParent().getSchedule();
    }
    
    public String getEndOfSupportDate() {
        return getParentSchedule().getFormattedEOLDate();
    }
    
    public String getDueDate() {
        return getParentSchedule().getFormattedDueDate();
    }
    
    public String getVersion() {
        return getParent().getVersion();
    }
    
    @Override
    public String toString() {
        return "Component '" + name + "': project => " + jira + " / product => " + product;
    }
    
    public List<String> validate(JiraRestClient restClient) {
        final var errors = new LinkedList<String>();
        final var formatter = Utility.errorsFormatter(2);
        validateIssue(getJira(), errors, formatter, restClient, "jira");
        validateIssue(getProductIssue(), errors, formatter, restClient, "product");
        return errors.isEmpty() ? Collections.emptyList() :
            Collections.singletonList(errors.stream().reduce("Invalid '" + getName() + "' component:\n", Utility.errorsFormatter(1)));
    }
    
    private void validateIssue(Issue issue, LinkedList<String> errors, BinaryOperator<String> formatter, JiraRestClient restClient, final String issueType) {
        if (issue != null) {
            var issueErrors = issue.validate(restClient);
            if (!issueErrors.isEmpty()) {
                errors.add(issueErrors.stream().reduce("Invalid " + issueType + ":\n", formatter));
            }
        }
    }
}
