# Release Manager Tool

## Table of Contents

  * [Table of Contents](#table-of-contents)
  * [Introduction](#introduction)
  * [Release process](#release-process)
      * [Instructions](#instructions)
      * [Release definition](#release-definition)
      * [Start a new Snowdrop release](#start-a-new-snowdrop-release)
      * [Create JIRA stakeholder request issues](#create-jira-stakeholder-request-issues)
      * [Link JIRA issues to a parent](#link-jira-issues-to-a-parent)
      * [Clone a JIRA Release issue and their subtasks](#clone-a-jira-release-issue-and-their-subtasks)
      * [List CVE](#list-cve)
      * [Update Build Config](#update-build-config)
  * [Tricks](#tricks)
      * [Get JIRA issue](#get-jira-issue)
      * [Post a new JIRA ticket](#post-a-new-jira-ticket)
    
## Introduction

This project has been designed to investigate different technology able to manage the creation, update or deletion of the JIRA
tickets as documented under the table of content. It is being also used to automate release process tasks.

## Release process

### Instructions

- Build the code

```bash
./mvnw clean package 
```

- Build the container image
```bash
docker build -f src/main/docker/Dockerfile.jvm -t snowdrop/release-manager:1.0 .
docker tag snowdrop/release-manager:1.0 quay.io/snowdrop/release-manager:1.0
docker push quay.io/snowdrop/release-manager:1.0
```
**Remark**: No `ENTRYPOINT` has been defined within the container in order to be able to execute the script `/deployments/run-java.sh` manually, using a Jenkins Pipeline Job. The UID of the user
is `1000` like also the UID of the Jenkins JNLP Agent !

### Release definition

A release metadata is captured in a `release.yml` file. This metadata links the release to the components that constitute it.
This, in particular, allows for automated creation of stakeholder issue marking the beginning of a new release cycle.

The `release.yml` file lives in the `snowdrop/spring-boot-bom` repository right next to the `pom.xml` file so that they can
evolve concurrently as needed and be kept in sync. This file should be updated each time the team starts working on a new
release.

An example of such `release.yml` can be found at: https://github.com/snowdrop/spring-boot-bom/blob/sb-2.3.x/release.yml

### Start a new Snowdrop release

Starting a release means cloning the template issue, creating stakeholder requests, linking them to the master release ticket
that just got created and linking any CVE associated with the release. This process consumes a `release.yml` file associated
with the code base that has been updated from upstream Spring Boot, which contains all the metadata for this tool to perform the
needed operations. This is done by running the following command:

```bash
java -jar target/quarkus-app/quarkus-run.jar \
    -u JBOSS_JIRA_USER \
    -p JBOSS_JIRA_PWD \
    start-release \
    -g <github org>/<github repo>/<git reference: branch, tag, hash> \
    -o <github token> \
    -w john,doe,foo  
```

The `-w` option allows to optionally add the list of comma-separated JIRA user names to the list of watchers for the issues that
have been created.

### Create JIRA stakeholder request issues

To create a bulk of issues for a component/starter which are blocking a JIRA Issue release, use the following command:

```bash
java -jar target/quarkus-app/quarkus-run.jar \
    -u JBOSS_JIRA_USER \
    -p JBOSS_JIRA_PWD \
    create-component \
    -g <github org>/<github repo>/<git reference: branch, tag, hash> 
```

This will parse the `release.yml` file found at the specified git reference, retrieve all the defined components , retrieve
their associated version from the Snowdrop `pom.xml` and create a corresponding JIRA issue in the appropriate JIRA project.
If `release.yml` includes a `key` field in its main `issue` field, this key is used to identify the main release issue to which
the component requests tickets will be linked.

Each component is identified by its associated JIRA project name (which is used to create the corresponding JIRA request) and a
set of properties used to identify which artifacts are linked to this particular component. The name of the component's
properties follow the name version properties defined in the POM. For example, for Hibernate, the version property is
named `hibernate.version` and the associated component property is named `hibernate`
(`release-manager` takes care of matching that property to the one used in the POM). If we also want to associate other
properties to the same component, we can add more. For example, the Hibernate component is associated with
the `hibernate-validator` property.

**IMPORTANT**:

### Link JIRA issues to a parent

To Link different issues to a JIRA issue using as relation type `Is Blocked By`, then execute the following command:

```bash
java -jar target/quarkus-app/quarkus-run.jar \
    -u JBOSS_JIRA_USER \
    -p JBOSS_JIRA_PWD \
    link \
    ENTSBT-xxx \
    --to EAP-yyy 
```

The `to` option represents the issue which currently blocks the release issue referenced by the `issue` specified as a
parameter.

### Clone a JIRA Release issue and their subtasks

To clone a Release issue and their sub-tasks

```bash
 java -jar target/quarkus-app/quarkus-run.jar \
    -u JBOSS_JIRA_USER \
    -p JBOSS_JIRA_PWD \
    clone \
    ENTSBT-ddd \    
    --git <github org>/<github repo>/<git reference: branch, tag, hash> 
```

### List CVE

Generate a list of CVE. This process will print the list of CVE in a report. 
If the `-r` option is used this list will also be pushed to GitHub, being the `-o` option required for that.

```bash
 java -jar target/quarkus-app/quarkus-run.jar \
    -u JBOSS_JIRA_USER \
    -p JBOSS_JIRA_PWD \
    list-cves \
    -r \
    -o <github token> \ 
    version
```

### Update Build Config

This task gathers information from the JIRA issues stored in the release file, updates the build configuration file and pushes 
the changes to the gitlab repository to be merged.

The required parameters are the following:

| Parameter | Required | Description |
| --- | --- | --- |
| -g, --git | YES | Git reference in the <github org>/<github repo> format |
| -o, --token | YES | Github API token |
| -glu, --gluser | YES | Gitlab user name |
| -glt, --gltoken | YES | Gitlab API token |
| -r, --release | YES | Release number (e.g. 2.4.3) |
| -q, --qualifier | YES | Qualifier [Alpha, Beta, Final, SP] (e.g. Beta1) |
| -m, --milestone | YES | Milestone [DR*, ER*, CR*]  | 

Execution example: 

```bash
$ java -jar target/quarkus-app/quarkus-run.jar \
  -u ${JBOSS_JIRA_USER} -p ${JBOSS_JIRA_PWD} \
  update-build-config \
  -g snowdrop/spring-boot-bom -o ${GITHUB_TOKEN} -glu ${GITLAB_USER} -glt ${GITLAB_TOKEN} -r 2.4.3  -q Alpha1 -m "DR*"
```
## Tricks

To query the JIRA server using `HTTP` requests (GET, POST, ...), you can execute the following commands

**Reference documentation** is available here at Atlassian [REST API v2 doc](https://docs.atlassian.com/software/jira/docs/api/REST/8.10.0/)

### Get JIRA issue

```bash
http --verify=no --follow --auth user:pwd https://issues.redhat.com/rest/api/2/issue/SB-889
```

### Post a new JIRA ticket

```bash
http --verify=no --follow  --auth user:pwd POST https://issues.redhat.com/rest/api/2/issue/ < jira.json
```

