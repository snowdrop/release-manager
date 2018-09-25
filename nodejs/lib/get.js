let $ = require('./util.js')

module.exports = {
  GetIssueById,
  PrintIssue: printIssue
}

// Get issue by id
async function GetIssueById (client, id) {
  try {
    var result = await client.issue.getIssue({
      issueKey: id
    })
    printIssue(id, $.convertJsontoObject(result))
  } catch (e) {
    $.Log.error(`Unable to get JIRA issue - Status code of error is:\n${e}`)
  }
}

function printIssue (id, issueType) {
  $.Log.info('Key         : ' + id.issueKey)
  $.Log.info('Title       : ' + issueType.summary)
  $.Log.info('Status      : ' + issueType.status.name)
  $.Log.info('Type        : ' + issueType.issuetype.name)
  $.Log.info('Author      : ' + issueType.reporter.name)
  if (issueType.description != null) {
    $.Log.info(
      'Description :\n' + issueType.description)
  }
  if (issueType.labels.length > 0) {
    $.Log.info('Labels: ', issueType.labels)
  }

  var regex = /\[(.*?)\]/
  var sprints = issueType.customfield_12310940
  if (sprints != null) {
    for (var i = 0, lengthSprints = sprints.length; i < lengthSprints; i++) {
      // $.Log.info("Sprint " + sprints[i]);
      var sprint = regex.exec(sprints[i])
      var pairs = sprint[1].split(',')
      var map = {}

      for (var j = 0, lengthPairs = pairs.length; j < lengthPairs; j++) {
        var str = pairs[j].split('=')
        map = $.addValueToList(map, str[0], str[1])
      }
      $.Log.info('Sprint Name : ' + map['name'] + ', state : ' + map['state'])
    }
  }
}
