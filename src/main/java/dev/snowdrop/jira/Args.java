package dev.snowdrop.jira;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter
    protected List<String> parameters = new ArrayList<>();

    @Parameter(names = "-url", description = "URL of the JIRA server")
    protected String jiraServerUri;

    @Parameter(names = "-user", description = "JIRA User", required = true)
    protected String user;

    @Parameter(names = "-issue", description = "JIRA Issue", required = true)
    protected String issue;

    @Parameter(names = "-password", description = "JIRA password", password = true, required = true)
    protected String password;
}