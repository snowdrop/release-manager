let $ = require('./util.js')
let operation = require('./operations.js')

module.exports = {
    Status: status
}

// Get issue by id
async function status(client, id) {
    try {
        var result = await operation.get.JiraIssue(client, id)
        issueType = $.convertJsontoObject(result)
        console.log(issueType)
    } catch (e) {
        console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
    }
}
