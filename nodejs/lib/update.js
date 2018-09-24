let $ = require('./util.js')
let jira = require('./get.js')
var jiraClient

module.exports = {
  JiraClient,
  UpdateStatus: Update
}

function JiraClient (host, username, password) {
  jiraClient = $.myJiraClient(host, username, password)
}

// Get issue by id
function Update (id) {
  var result = jira.GetJiraIssue(id)
  issueType = $.convertJsontoObject(result)
}
