let $ = require('./util.js')

module.exports = {
    ToNewStatus: toNewStatus
}

var transitions = new Map()
transitions.set('InProgress','4')

// Change the status of the JIRA Issue to a new Transition
async function toNewStatus(client, id) {
    try {
         updateTransition(client, id, transitions.get('InProgress'))
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}

function updateTransition(jiraclient, id, transitionId) {
    jiraclient.issue.transitionIssue(
        { issueKey: id,
          transition: { "id": transitionId }
        })
}
