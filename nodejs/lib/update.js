let $ = require('./util.js')
let cli = require('./operations.js')

module.exports = {
  Status: Status
}

// Get issue by id
function Status (id) {
  var result = cli.jiraClient.issue(id)
  issueType = $.convertJsontoObject(result)
}
