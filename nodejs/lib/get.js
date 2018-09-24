let $ = require('./util.js')

module.exports = {
  IssueById,
  JiraIssue
}

// Get issue by id
async function IssueById(client, id) {
  try {
    var result = await JiraIssue(client, id)

    issueType = $.convertJsontoObject(result)

    console.log('Key         : ' + id.issueKey)
    console.log('Title       : ' + issueType.summary)
    console.log('Status      : ' + issueType.status.name)
    console.log('Type        : ' + issueType.issuetype.name)
    console.log('Author      : ' + issueType.reporter.name)
    if (issueType.description != null) {
      console.log(
        'Description :\n' + issueType.description)
    }
    if (issueType.labels.length > 0) {
      console.log('Labels: ', issueType.labels)
    }

    var regex = /\[(.*?)\]/
    var sprints = issueType.customfield_12310940
    if (sprints != null) {
      for (var i = 0, lengthSprints = sprints.length; i < lengthSprints; i++) {
        // console.log("Sprint " + sprints[i]);
        var sprint = regex.exec(sprints[i])
        var pairs = sprint[1].split(',')
        var map = {}

        for (var j = 0, lengthPairs = pairs.length; j < lengthPairs; j++) {
          var str = pairs[j].split('=')
          map = $.addValueToList(map, str[0], str[1])
        }
        console.log('Sprint Name : ' + map['name'] + ', state : ' + map['state'])
      }
    }
  } catch (e) {
    console.log(`Unable to get JIRA issue - Status code of error is:\n${e}`)
  }
}

function JiraIssue(jiraclient, id) {
  return jiraclient.issue.getIssue(id)
}
