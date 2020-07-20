# JIRA Tools

## Table of Contents

  * [Introduction](#introduction)
  * [Issues Manager](#issues-manager)
     * [Create JIRA "Component/starter" issues](#create-jira-componentstarter-issues)
     * [Link JIRA issues to a parent](#link-jira-issues-to-a-parent)
     * [Clone a JIRA Release issue and their subtasks](#clone-a-jira-release-issue-and-their-subtasks)
  * [HTTP Request to get or create JIRA tickets](#http-request-to-get-or-create-jira-tickets)
     * [Get](#get)
     * [Post](#post)


## Introduction

This project has been designed to investigate different technology able to manage the creation, update or deletion of the JIRA tickets as documented under the table of content.
They could be used to automate Job's action or for your own personal needs to get a Jira issue, change the status, ...

## Issues manager

Instructions:

- Build the code
```bash
mvn clean package 
```

### Create JIRA "Component/starter" issues

To create a bulk of issues for a component/starter which are blocking a JIRA Issue release, use the following command: 
```bash
java -jar target/uber-issues-manager-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -cfg etc/release.yaml \
    -action create-component \
    -url https://issues.redhat.com
```

**IMPORTANT**: If the `release.yaml` includes a `jiraKey` field, then the newly component issue created will be linked to the Release Issue !

### Link JIRA issues to a parent

To Link different issues to a JIRA issue using as relation type `Is Blocked By`, then execute the following command:  
```bash
java -jar target/uber-issues-manager-1.0-SNAPSHOT.jar \
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
 java -jar target/uber-issues-manager-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -url https://issues.redhat.com \
    -cfg etc/release.yaml \
    -action clone \
    -issue ENTSBT-ddd
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