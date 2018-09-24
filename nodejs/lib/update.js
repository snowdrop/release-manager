let $ = require('./util.js')
var get = require('./get.js')

module.exports = {
    Status: status
}

// Get issue by id
async function status(client, id) {
    try {
        var result = await get.GetJiraIssue(client, id)
        issueType = $.convertJsontoObject(result)
        get.PrintIssue(id, issueType)

        issueType.status.name="New"
        issueTypeUpdated = update(client, issueType, id)
        get.PrintIssue(id, issueTypeUpdated)
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}

function update(jiraclient, type, id) {
    return jiraclient.issue.editIssue(id, null, type)
}
