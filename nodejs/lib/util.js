let JiraClient = require('jira-connector')
var map

module.exports = {
  addValueToList,
  myJiraClient
}

function myJiraClient (host, username, password) {
  // console.log("Params : ", username, password, host)

  return new JiraClient({
    host: host,
    basic_auth: {
      username: username,
      password: password
    }
  })
}

function addValueToList (key, value) {
  // if the list is already created for the "key", then uses it
  // else creates new list for the "key" to store multiple values in it.
  map[key] = map[key] || []
  map[key].push(value)
}
