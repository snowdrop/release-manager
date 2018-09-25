/**
 * How to use the Node JS Jira client
 * npm install
 * node index.js -u JIRA_USER -p JIRA_PWD -k SB-Num
 */

let $ = require('./lib/util.js')
let operation = require('./lib/operations.js')
let argv = require('minimist')(process.argv.slice(2))
var cli

// Instantiate JIRA Client using command line parameters
function init() {
    var host
    if (argv.h == null) {
        host = 'jira.jboss.org'
    } else {
        host = argv.h
    }
    cli = operation.newClient(host, argv.u, argv.p)
}

init()

switch (argv.o) {
    case 'get':
        // Call Get Issue
        if (argv.k) {
            operation.GetIssueById(cli, argv.k)
        } else {
            operation.GetIssueById(cli, 'SB-889')
        }
        break
    case 'update':
        operation.UpdateIssueStatus(cli, argv.k, argv.s)
        break
    case 'edit':
        operation.EditIssue(cli, argv.k)
}
