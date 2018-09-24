var request = require('request');

var bodyData = `{
  "transition": { "id": "4" }
}`

var options = {
    method: 'POST',
    url: 'https://jira.jboss.org/rest/api/2/issue/SB-896/transitions',
    auth: {username: 'cmoulliard', password: 'xxxxxx'},
    headers: {
        'Content-Type': 'application/json'
    },
    body: bodyData
}

function update() {
    request(options, function (error, response, body) {
        if (error) throw new Error(error);
        console.log(
            'Response: ' + response.statusCode + ' ' + response.statusMessage
        );
        console.log(body);
    });
}