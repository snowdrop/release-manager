# R&D : JIRA Tools

## Table Of Contents

    * [Introduction](#introduction)
    * [Clients](#clients)
      * [Nodejs client](#nodejs-client)
      * [Java Jira client](#java-jira-client)
      * [HTTP Request to get or create JIRA tickets](#http-request-to-get-or-create-jira-tickets)
         * [Get](#get)
         * [Post](#post)
       
## Introduction

R&D Project to test different JIRA Client tool. They could be used to automate Job's action or for your own personal needs to get a Jira issue, change the status, ...

TODO: Add `New` and `edit` commands

## Clients

### Java Aphrodite client

See the Set Aphrodite project for more [information](https://github.com/jboss-set/aphrodite)

- Move to the `aphrodite` folder and compile the java client
```bash
cd aphrodite
mvn clean compile 
```

- Create the `aphrodite.json` file containing your [jira credentials](https://github.com/jboss-set/aphrodite/blob/master/aphrodite.properties.json.example)
```bash
mkdir etc && touch etc/aphrodite.json
```
- Launch it to get JIRA issues
```bash
mvn exec:java -Daphrodite.config=./etc/aphrodite.json
```

### Java Atlassian Jira client

https://bitbucket.org/atlassian/jira-rest-java-client/src/75a64c9d81aa?at=master

- Move to the atlassian folder and compile the java client
```bash
cd atlassian
mvn clean package 
java -jar target/uber-jira-tool-1.0-SNAPSHOT.jar -user JBOSS_JIRA_USER -password JBOSS_JIRA_PWD -issue SB-xxx
```

### Nodejs client

- Create a `~/.jiracli.yml` file with the following information

```yaml
host:
 name: JIRA_HOST // jira.jboss.org
 user: JIRA_USER
 pwd: JIRA_PASSWORD
```
- Install the node packages/modules needed

```bash
cd nodejs
npm install && npm link
```

- Execute this command to get a ticket

```bash
jira get SB-869

Key         : SB-869
Title       : Contact Atomist support
Status      : New
Type        : Task
Author      : claprun
Description :
- Admin delegation
- Bug (?) on team admin: an invite is still pending even though a team member with that email has already been accepted. What happens if that invite is rescinded since the error message makes it sound like all references to that email would be deleted?
Sprint Name : SB-2018-09-14, state : CLOSED
Sprint Name : SB-2018-09-28, state : ACTIVE
```

- Update the status 

```bash
// To move a new ticket to status needed to Hand Over for Development
jira update SB-869 HandOver

// To move the ticket to the In Progress column of a sprint
jira update SB-869 InProgress

// To resolve/close it
jira update SB-869 ResolveIssue|CloseIssue
```
 
### HTTP Request to get or create JIRA tickets

#### Get

```bash
http --verify=no --auth user:pwd https://issues.jboss.org/rest/api/latest/issue/SB-889
```

#### Post

```bash
http --verify=no --auth user:pwd POST https://issues.jboss.org/rest/api/2/issue/ < jira.json
```