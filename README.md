## JIRA Java Client

https://bitbucket.org/atlassian/jira-rest-java-client/src/75a64c9d81aa?at=master

## Nodejs client

- Create a `~/.jiraclient.yml` file with the following information

```yaml
host:
 name: JIRA_HOST // jira.jboss.org
 user: JIRA_USER
 pwd: JIRA_PASSWORD
```
- Next execute a command to get a ticket

```bash
cd nodejs
npm install
node index.js get SB-869

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
node index.js update SB-869 HandOver

// To move the ticket to the In Progress column of a sprint
node index.js update SB-869 InProgress

// To resolve/close it
node index.js update SB-869 Resolve|Close
```


## Java Jira client

```bash
mvn clean package 
java -jar target/uber-jira-tool-1.0-SNAPSHOT.jar -user JBOSS_JIRA_USER -password JBOSS_JIRA_PWD -issue SB-xxx
```
 
## HTTP Request to get or create JIRA tickets

### Get

```bash
http --verify=no --auth user:pwd https://issues.jboss.org/rest/api/latest/issue/SB-889
```

### Post

```bash
http --verify=no --auth user:pwd POST https://issues.jboss.org/rest/api/2/issue/ < jira.json
```