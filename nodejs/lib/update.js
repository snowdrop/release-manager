module.exports = {
  UpdateIssueStatus,
  EditIssue
}

var transitions = new Map()
transitions.set('InProgress', '4')

async function EditIssue (client, id) {
  try {
    await client.issue.editIssue({
      issueKey: id,
      issue: {
        fields: {
          summary: 'This is a test',
          labels: [
            'bugfix',
            'blitz_test'
          ]
        }
      }
    })
  } catch (e) {
      $.Log.error(`Unable to get JIRA issue - Status code of error is:\n${e}`)
  }
}

// Change the status of the JIRA Issue to a new Transition
async function UpdateIssueStatus (client, id) {
  try {
    client.issue.transitionIssue({
      issueKey: id,
      transition: { 'id': transitions.get('InProgress') }
    })
  } catch (e) {
      $.Log.error(`Unable to get JIRA issue - Status code of error is:\n${e}`)
  }
}
