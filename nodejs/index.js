/**
 * How to use the Node JS Jira client
 * npm install
 * node index.js -u JIRA_USER -p JIRA_PWD -k SB-Num
 */

let GET_OPERATION = require('./lib/get.js')
let UPDATE_OPERATION = require('./lib/update.js')
let argv = require('minimist')(process.argv.slice(2))
var host

// Instantiate JIRA Client using command line parameters
function init () {
  if (argv.h == null) {
    host = 'jira.jboss.org'
  } else {
    host = argv.h
  }
  GET_OPERATION.JiraClient(host, argv.u, argv.p)
}

init()

switch (argv.o) {
    case "get":
        // Call Get Issue
        if (argv.k) {
            GET_OPERATION.GetIssueById({ issueKey: argv.k })
        } else {
            GET_OPERATION.GetIssueById({ issueKey: 'SB-889' })
        }
    case "update":
        UPDATE_OPERATION.UpdateStatus({ issueKey: argv.k })
}
