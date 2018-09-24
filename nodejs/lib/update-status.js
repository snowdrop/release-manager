let $ = require('./util.js')
var jiraClient

module.exports = {
  JiraClient,
  updateStatus
}

function JiraClient (host, username, password) {
  jiraClient = $.myJiraClient(host, username, password)
}

// Get issue by id
function updateStatus (id) {
  jiraClient.issue.getIssue(id, function (error, issue) {
    if (error) {
      console.log(error.stack)
    }
    console.log(issue.fields)
  })
}
