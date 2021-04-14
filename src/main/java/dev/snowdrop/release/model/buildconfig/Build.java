package dev.snowdrop.release.model.buildconfig;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.Issue;
import dev.snowdrop.release.model.POM;
import dev.snowdrop.release.model.Schedule;

import java.text.ParseException;
import java.util.*;

import static dev.snowdrop.release.services.Utility.isStringNullOrBlank;

public class Build {
    @JsonProperty
    private String name;
    @JsonProperty
    private String project;
    @JsonProperty
    private String buildScript;
    @JsonProperty
    private String scmUrl;
    @JsonProperty
    private String scmRevision;
    @JsonProperty
    private List<String> dependencies;
    @JsonProperty
    private List<String> alignmentParameters;

    @JsonAnySetter
    private Map<String,Object> additionalProperties = new HashMap<>();

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Build{");
        sb.append("name='").append(name).append('\'');
        sb.append(", project='").append(project).append('\'');
        sb.append(", buildScript='").append(buildScript).append('\'');
        sb.append(", scmUrl='").append(scmUrl).append('\'');
        sb.append(", scmRevision='").append(scmRevision).append('\'');
        sb.append(", dependencies=").append(dependencies);
        sb.append(", alignmentParameters=").append(alignmentParameters);
        sb.append(", additionalProperties=").append(additionalProperties);
        sb.append('}');
        return sb.toString();
    }
}
