package dev.snowdrop.jira.atlassian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.jira.atlassian.POMParser;

import java.util.LinkedList;
import java.util.List;

public class Component {
    @JsonProperty
    private Issue issue;

    @JsonProperty
    private List<String> properties;

    @JsonIgnore
    private List<Artifact> artifacts;

	@JsonIgnore
	private Release parent;

	public Release getParent() {
		return parent;
	}

	public void setParent(Release release) {
		this.parent = release;
	}

	public String getTitle() {
		return "TODO: Component title";
	}

    public Issue getIssue() {
        return issue;
    }

    public List<Artifact> getArtifacts() {
        artifacts = new LinkedList<>();
        for (String property : properties) {
            artifacts.addAll(POMParser.getArtifactsWith(property));
        }
        return artifacts;
    }
}
