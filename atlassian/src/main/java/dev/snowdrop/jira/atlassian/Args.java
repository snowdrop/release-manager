package dev.snowdrop.jira.atlassian;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter
    protected List<String> parameters = new ArrayList<>();

    @Parameter(names = "-cfg", description = "YAML file path")
    protected String cfg;

    @Parameter(names = "-action", description = "Action to be performed: search, create, delete")
    protected String action;

    @Parameter(names = "-issue", description = "JIRA Issue")
    protected String issue;

    @Parameter(names = "-issues", description = "of Bulk JIRA Issues", variableArity = true)
    protected List<String> issues;

    @Parameter(names = "-to_issue", description = "To JIRA Issue")
    protected String toIssue;

    @Parameter(names = "-url", description = "URL of the JIRA server")
    protected String jiraServerUri;

    @Parameter(names = "-user", description = "JIRA User", required = true)
    protected String user;

    @Parameter(names = "-password", description = "JIRA password", password = true, required = true)
    protected String password;
}