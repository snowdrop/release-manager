let $ = require('./util.js')
let jira = require('./get.js')
var jiraClient

module.exports = {
  JiraClient,
  UpdateStatus
}

function JiraClient (host, username, password) {
  jiraClient = $.myJiraClient(host, username, password)
}

// Get issue by id
function UpdateStatus (id) {
  var result = jira.GetJiraIssue(id)
  issueType = $.convertJsontoObject(result)
}
