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
    var cfg = $.parseJIRAConfig()
    cli = operation.newClient(cfg.host.name, cfg.host.user, cfg.host.pwd)
}

init()

var op = argv._[0]

switch (op) {
    case 'get':
        // Get Issue
        if (argv._[1]) {
            operation.GetIssueById(cli, argv._[1])
        }
        break
    case 'update':
        operation.UpdateIssueStatus(cli, argv._[1], argv._[2])
        break
    case 'edit':
        operation.EditIssue(cli, argv._[1])
}
