let JiraClient = require('jira-connector')
let util = require('./util.js')
var jiraClient

module.exports = {
    newClient,
    jiraClient: jiraClient,
    get    : require('./get.js'),
    update : require('./update.js')
}


function newClient(host, username, password) {
    // console.log("Params : ", username, password, host)

    jiraClient = new JiraClient({
        host: host,
        basic_auth: {
            base64: util.convertToBase64(username, password)
        }
    })
}