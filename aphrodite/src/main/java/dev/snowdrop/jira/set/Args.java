package dev.snowdrop.jira.set;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter
    protected List<String> parameters = new ArrayList<>();

    @Parameter(names = "-cfg", description = "Aphrodite config file", required = true)
    protected String config;

    @Parameter(names = "-url", description = "URL of the JIRA server", required = true)
    protected String jiraServerUri;

    @Parameter(names = "-issue", description = "JIRA Issue", required = true)
    protected String issue;
}