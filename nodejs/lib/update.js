let util = require('./util.js')

module.exports = {
  UpdateIssueStatus
}

var transitions = new Map()
// New to Open status (ticket displayed within the to do column of the sprint will be now able to move to In Progress)
transitions.set('HandOver', '791')
// Open to Coding In Progress
transitions.set('InProgress', '4')
// Revert to Open
transitions.set('StopProgress', '301')
// Status to close/resolve the issue
transitions.set('ResolveIssue', '5')
transitions.set('CloseIssue', '2')
transitions.set('LinkPR', '711')

// Change the status of the JIRA Issue to a new Transition
async function UpdateIssueStatus (client, id, transitionName) {
  try {
    client.issue.transitionIssue({
      issueKey: id,
      transition: { 'id': transitions.get(transitionName) }
    })
  } catch (e) {
    util.Log.error(`Unable to update status of the JIRA issue - Status code of error is:\n${e}`)
  }
}
