let $ = require('./util.js')

module.exports = {
  Status: Status
}

// Get issue by id
function Status (jiraclient, id) {
  var result = jiraClient.issue(id)
  issueType = $.convertJsontoObject(result)
}
