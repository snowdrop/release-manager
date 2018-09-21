/**
* How to use the Node JS Jira client
* npm install
* node index.js -u JIRA_USER -p JIRA_PWD -k SB-Num
*/
const JiraClient = require('jira-connector');
const YAML = require('json2yaml');
const yaml = require('js-yaml');

const argv = require('minimist')(process.argv.slice(2));

var regex = /\[(.*?)\]/;

// Default value if no id is passed to query the JIRA JBoss Repo
var id

if (argv.k) {
    id = {issueKey: argv.k}
} else {
    id = {issueKey: 'SB-889'}
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
    yamlIssue = yaml.safeLoad(ymlText);

    console.log("Key         : ", id.issueKey)
    console.log("Title       : ", yamlIssue.summary)
    console.log("Status      : ", yamlIssue.status.name)
    console.log("Type        : ", yamlIssue.issuetype.name)
    console.log("Author      : ", yamlIssue.reporter.name)
    console.log("Description : \n", yamlIssue.description)
    if (yamlIssue.labels.length > 0 ) {console.log("Labels: ", yamlIssue.labels)};

    sprints = yamlIssue.customfield_12310940;
    for (var i = 0, lengthSprints = sprints.length; i < lengthSprints; i++) {
        // console.log("Sprint " + sprints[i]);
        sprint = regex.exec(sprints[i]);
        pairs = sprint[1].split(",");
        map = {};

        for (var j = 0, lengthPairs = pairs.length; j < lengthPairs; j++) {
            str = pairs[j].split("=");
            addValueToList(str[0],str[1])
        }
        console.log("Sprint Name : " + map["name"] + ", state : " + map["state"])
    }

});

function addValueToList(key, value) {
    //if the list is already created for the "key", then uses it
    //else creates new list for the "key" to store multiple values in it.
    map[key] = map[key] || [];
    map[key].push(value);
}