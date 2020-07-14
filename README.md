# JIRA Tools

## Table of Contents

  * [Introduction](#introduction)
  * [Java Atlassian Jira client](#java-atlassian-jira-client)
     * [Create an issue](#create-an-issue)
     * [Get an issue](#get-an-issue)
     * [Delete an issue](#delete-an-issue)
     * [Delete bulk issues](#delete-bulk-issues)
  * [Java Prod client](#java-prod-client)
     * [Create JIRA "Component/starter" issues](#create-jira-componentstarter-issues)
     * [Link JIRA issues to a parent](#link-jira-issues-to-a-parent)
     * [Clone a JIRA Release issue and their subtasks](#clone-a-jira-release-issue-and-their-subtasks)
  * [Java Aphrodite client](#java-aphrodite-client)
  * [Nodejs client](#nodejs-client)
  * [HTTP Request to get or create JIRA tickets](#http-request-to-get-or-create-jira-tickets)
     * [Get](#get)
     * [Post](#post)


## Introduction

This project has been designed to investigate different technology able to manage the creation, update or deletion of the JIRA tickets as documented under the table of content.
They could be used to automate Job's action or for your own personal needs to get a Jira issue, change the status, ...

## Java Atlassian Jira client

**REMARK**: The `Aphrodite project` don t manage all the CRUD operations but a few and don`t support to create or clone an issue !

References:

- https://developer.atlassian.com/server/jira/platform/java-apis/#java-api-policy-for-jira

- https://bitbucket.org/atlassian/jira-rest-java-client/src/75a64c9d81aa?at=master

Instructions:

- Move to the atlassian folder and compile the java client
```bash
cd atlassian
mvn clean package 
```

### Create an issue
```bash
java -jar target/uber-atlassian-1.0-SNAPSHOT.jar \
    -user JIRA_USER \
    -password JIRA_PWD \
    -cfg etc/release.yaml \
    -action create \
    -url https://issues.redhat.com
```
### Get an issue
```bash
java -jar target/uber-atlassian-1.0-SNAPSHOT.jar \
    -user JIRA_USER \
    -password JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action get \
    -issue ENTSBT-xxx
```
### Delete an issue
```bash
java -jar target/uber-atlassian-1.0-SNAPSHOT.jar \
    -user JIRA_USER \
    -password JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action delete \
    -issue ENTSBT-xxx
```

### Delete bulk issues
```bash
java -jar target/uber-atlassian-1.0-SNAPSHOT.jar \
    -user JIRA_USER \
    -password JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action delete-bulk \
    -issue ENTSBT-xxx ENTSBT-yyy ENTSBT-zzz
```

## Java Prod client

Instructions:

- Move to the atlassian folder and compile the java client
```bash
cd atlassian-prod
mvn clean package 
```

### Create JIRA "Component/starter" issues

To create a bulk of issues for a component/starter which are blocking a JIRA Issue release, use the following command: 
```bash
java -jar target/uber-atlassian-prod-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -cfg etc/release.yaml \
    -action create-component \
    -url https://issues.redhat.com
```

**REMARK**: Next link the issues created to block the JIRA release issue !

### Link JIRA issues to a parent

To Link different issues to a JIRA issue using as relation type `Is Blocked By`, then execute the following command:  
```bash
java -jar target/uber-atlassian-prod-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action link \
    -issue ENTSBT-xxx \
    -to_issue EAP-yyy 
```

The `to_issue` parameter represents the issue which currently blocks the release issue referenced by the parameter `issue`.

### Clone a JIRA Release issue and their subtasks

To clone a Release issue and their sub-tasks
```bash
 java -jar target/uber-atlassian-prod-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action clone \
    -issue ENTSBT-ddd
```

## Java Aphrodite client

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
mvn clean package 
java -jar target/uber-aphrodite-1.0-SNAPSHOT.jar \
            -cfg ./etc/aphrodite.json \
            -url https://issues.redhat.com \
            -issue ENTSBT-343
```

## Nodejs client

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
 
## HTTP Request to get or create JIRA tickets

Atlassian REST API v2 doc: https://docs.atlassian.com/software/jira/docs/api/REST/8.10.0/

### Get

```bash
http --verify=no --follow --auth user:pwd https://issues.jboss.org/rest/api/2/issue/SB-889
```

### Post

```bash
http --verify=no --follow  --auth user:pwd POST https://issues.jboss.org/rest/api/2/issue/ < jira.json
```