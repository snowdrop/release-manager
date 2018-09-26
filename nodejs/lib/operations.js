let util = require('./util.js')
let JiraClient = require('jira-connector')

module.exports = {
  newClient,
  GetIssueById: require('./get.js').GetIssueById,
  PrintIssue: require('./get.js').PrintIssue,
  UpdateIssueStatus: require('./update.js').UpdateIssueStatus,
  EditIssue: require('./edit.js').EditIssue
}

function newClient (host, username, password) {
  //util.Log.debug('Params : ', username, password, host)
  return new JiraClient({
    host: host,
    basic_auth: {
      base64: util.convertToBase64(username, password)
    }
  })
}
