## JIRA Java Client

https://bitbucket.org/atlassian/jira-rest-java-client/src/75a64c9d81aa?at=master

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