## JIRA Java Client

https://bitbucket.org/atlassian/jira-rest-java-client/src/75a64c9d81aa?at=master

## Nodejs client

```bash
cd nodejsnpm install
node index.js -u JBOSS_JIRA_USER -p JBOSS_JIRA_PWD -k SB-xxx

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