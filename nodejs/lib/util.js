let JiraClient = require('jira-connector')

module.exports = {
  addValueToList,
  myJiraClient
}

function myJiraClient (host, username, password) {
  // console.log("Params : ", username, password, host)

  return new JiraClient({
    host: host,
    basic_auth: {
      base64: convertToBase64(username, password)
    }
  })
}

function addValueToList (map, key, value) {
    // if the list is already created for the "key", then uses it
    // else creates new list for the "key" to store multiple values in it.
    map[key] = map[key] || []
    map[key].push(value)
    return map
}

function convertToBase64 (username, password) {
  var userAndPassword = username + ':' + password
  return Buffer.from(userAndPassword).toString('base64')
}
