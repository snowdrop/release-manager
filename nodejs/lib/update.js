let $ = require('./util.js')

module.exports = {
    ToNewStatus: toNewStatus,
    Issue: toNewIssue
}

var transitions = new Map()
transitions.set('InProgress', '4')

async function toNewIssue(client, id) {
    try {
        client.issue.editIssue({
            issueKey: id,
            issue: {
                fields: {
                    summary: "This is a test"
                }
            }
        })
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}

// Change the status of the JIRA Issue to a new Transition
async function toNewStatus(client, id) {
    try {
        jiraclient.issue.transitionIssue({
            issueKey: id,
            transition: {"id": transitions.get('InProgress')}
        })
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}