let $ = require('./util.js')
let YAML = require('json2yaml')
let yaml = require('js-yaml')
var jiraClient, ymlText, yamlIssue, regex, sprints

module.exports = {
  JiraClient,
  getIssueById
}

function JiraClient (host, username, password) {
  jiraClient = $.myJiraClient(host, username, password)
}

// Get issue by id
function getIssueById (id) {
  jiraClient.issue.getIssue(id, function (error, issue) {
    if (error) {
      console.log(error.stack)
    }
    ymlText = YAML.stringify(issue.fields)
    yamlIssue = yaml.safeLoad(ymlText)
    regex = /\[(.*?)\]/

    console.log('Key         : ' + id.issueKey)
    console.log('Title       : ' + yamlIssue.summary)
    console.log('Status      : ' + yamlIssue.status.name)
    console.log('Type        : ' + yamlIssue.issuetype.name)
    console.log('Author      : ' + yamlIssue.reporter.name)
    console.log('Description :\n' + yamlIssue.description)
    if (yamlIssue.labels.length > 0) {
      console.log('Labels: ', yamlIssue.labels)
    }
    ;

    sprints = yamlIssue.customfield_12310940
    if (sprints != null) {
      for (var i = 0, lengthSprints = sprints.length; i < lengthSprints; i++) {
        // console.log("Sprint " + sprints[i]);
        var sprint = regex.exec(sprints[i])
        var pairs = sprint[1].split(',')
        var map = {}

        for (var j = 0, lengthPairs = pairs.length; j < lengthPairs; j++) {
          var str = pairs[j].split('=')
          $.addValueToList(str[0], str[1])
        }
        console.log('Sprint Name : ' + map['name'] + ', state : ' + map['state'])
      }
    }
  })
}
