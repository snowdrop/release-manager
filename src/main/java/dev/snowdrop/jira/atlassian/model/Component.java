package dev.snowdrop.jira.atlassian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		this.artifacts = new LinkedList<>();
		final Map<String, List<Artifact>> artifacts = parent.getPOM().getArtifacts();
		for (String property : properties) {
			this.artifacts.addAll(artifacts.getOrDefault(property, Collections.emptyList()));
		}
		return this.artifacts;
	}
}
