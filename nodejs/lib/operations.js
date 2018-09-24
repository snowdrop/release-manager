let util = require('./util.js')

module.exports = {
    newClient,
    get    : require('./get.js'),
    update : require('./update.js')
}


function newClient(host, username, password) {
    // console.log("Params : ", username, password, host)
    JiraClient = require('jira-connector')

    return new JiraClient({
        host: host,
        basic_auth: {
            base64: util.convertToBase64(username, password)
        }
    })
}