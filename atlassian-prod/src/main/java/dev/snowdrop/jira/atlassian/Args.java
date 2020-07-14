package dev.snowdrop.jira.atlassian;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter
    public List<String> parameters = new ArrayList<>();

    @Parameter(names = "-cfg", description = "YAML file path")
    public String cfg;

    @Parameter(names = "-action", description = "Action to be performed: search, create, delete")
    public String action;

    @Parameter(names = "-issue", description = "JIRA Issue")
    public String issue;

    @Parameter(names = "-issues", description = "of Bulk JIRA Issues", variableArity = true)
    public List<String> issues;

    @Parameter(names = "-to_issue", description = "To JIRA Issue")
    public String toIssue;

    @Parameter(names = "-url", description = "URL of the JIRA server")
    public String jiraServerUri;

    @Parameter(names = "-user", description = "JIRA User", required = true)
    public String user;

    @Parameter(names = "-password", description = "JIRA password", password = true, required = true)
    public String password;
}