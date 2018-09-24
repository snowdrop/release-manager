/**
 * How to use the Node JS Jira client
 * npm install
 * node index.js -u JIRA_USER -p JIRA_PWD -k SB-Num
 */

let $ = require('./lib/get.js')
let argv = require('minimist')(process.argv.slice(2))
var host

// Instantiate JIRA Client using command line parameters
function init () {
  if (argv.h == null) {
    host = 'jira.jboss.org'
  } else {
    host = argv.h
  }
  $.JiraClient(host, argv.u, argv.p)
}

init()

// Call Get Issue
if (argv.k) {
  $.getIssueById({ issueKey: argv.k })
} else {
  $.getIssueById({ issueKey: 'SB-889' })
}
