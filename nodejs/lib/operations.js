let $ = require('./util.js')
let JiraClient = require('jira-connector')

module.exports = {
  newClient,
  GetIssueById: require('./get.js').GetIssueById,
  PrintIssue: require('./get.js').PrintIssue,
  UpdateIssueStatus: require('./update.js').UpdateIssueStatus,
  EditIssue: require('./update.js').EditIssue
}

function newClient (host, username, password) {
  $.Log.debug('Params : ', username, password, host)
  return new JiraClient({
    host: host,
    basic_auth: {
      base64: $.convertToBase64(username, password)
    }
  })
}
