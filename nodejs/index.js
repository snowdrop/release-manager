// Syntax
// node index.js -u JIRA_USER -p JIRA_PWD -k SB-888
//
const JiraClient = require('jira-connector');
const YAML = require('json2yaml');
const yaml = require('js-yaml');

const argv = require('minimist')(process.argv.slice(2));

var id = { issueKey: 'SB-889' };

if (argv.k) {
    id = {issueKey: argv.k}
}

var jira = new JiraClient( {
    host: 'jira.jboss.org',
    basic_auth: {
        username: argv.u,
        password: argv.p
    }
});

jira.issue.getIssue(id, function(error, issue) {
    ymlText = YAML.stringify(issue.fields);
    // console.log(ymlText);

    yamlIssue = yaml.safeLoad(ymlText);
    console.log("Key: ", id.issueKey)
    console.log("Title: ", yamlIssue.summary)
    console.log("Status: ", yamlIssue.status.name)
    console.log("Type: ", yamlIssue.issuetype.name)
    console.log("Author: ", yamlIssue.reporter.name)
    console.log("Description: ", yamlIssue.description)
});