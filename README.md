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

### Release definition

A release metadata is captured in a `release.yml` file. This metadata links the release to the components that
constitute it. This, in particular, allows for automated creation of stakeholder issue marking the beginning of a
new release cycle.

The `release.yml` file lives in the `snowdrop/spring-boot-bom` repository right next to the `pom.xml` file so that they
can evolve concurrently as needed and be kept in sync. This file should be updated each time the team starts working
 on a new release.

An example of such `release.yml` can be found at: https://github.com/metacosm/spring-boot-bom/blob/release-integration/release.yml

### Create JIRA stakeholder request issues

To create a bulk of issues for a component/starter which are blocking a JIRA Issue release, use the following command:
```bash
java -jar target/uber-issues-manager-1.0-SNAPSHOT.jar \
    -user JBOSS_JIRA_USER \
    -password JBOSS_JIRA_PWD \
    -action create-component \
    -watcher_list john,doe \
    -git <github org>/<github repo>/<git reference: branch, tag, hash>
```

This will parse the `release.yml` file found at the specified git reference, retrieve all the defined components
, retrieve their associated version from the Snowdrop `pom.xml` and create a corresponding JIRA issue in the
 appropriate JIRA project. If `release.yml` includes a `key` field in its main `issue` field, this key is used to
  identify the main release issue to which the component requests tickets will be linked.

Each component is identified by its associated JIRA project name (which is used to create the corresponding JIRA
request) and a set of properties used to identify which artifacts are linked to this particular component. The
name of the component's properties follow the name version properties defined in the POM. For example, for Hibernate,
the version property is named `hibernate.version` and the associated component property is named `hibernate`
(`issues-manager` takes care of matching that property to the one used in the POM). If we also want to associate
other properties to the same component, we can add more. For example, the Hibernate component is associated with
the `hibernate-validator` property.

**IMPORTANT**:

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