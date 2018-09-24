let $ = require('./util.js')
var operation = require('./get.js')

module.exports = {
    Status: status
}

// Get issue by id
async function status(client, id) {
    try {
        var result = await operation.GetJiraIssue(client, id)
        issueType = $.convertJsontoObject(result)
        operation.PrintIssue(id, issueType)

        issueType.status.name="New"
        issueTypeUpdated = update(client, issueType, id)
        operation.PrintIssue(id, issueTypeUpdated)
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}

function update(jiraclient, type, id) {
    return jiraclient.issue.editIssue(id, null, type)
}
