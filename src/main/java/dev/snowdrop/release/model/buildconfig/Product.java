package dev.snowdrop.release.model.buildconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.snowdrop.release.model.Issue;

import java.util.List;

public class Product {
    @JsonProperty
    private String name;
    @JsonProperty
    private String abbreviation;
    @JsonProperty
    private String stage;
    @JsonProperty
    private String issueTrackerUrl;
}
