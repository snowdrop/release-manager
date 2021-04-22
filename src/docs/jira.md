= JIRA manipulation

[TOC]



https://issues.redhat.com/rest/api/latest/issue

Headers:
contentType=application/json

POST

{
    "fields": {
        "summary": "[Spring Boot 2.3.10.RELEASE] Release steps CR [31 May 2021]",
        "issuetype": {
            "id": "3"
        },
        "project": {
            "key": "ENTSBT"
        },
        "description": "This is a set of tasks to be completed by the CR date defined in the Summary minus the days indicated in the individual task. Some task need to be completed prior other tasks, please see the dependency graph for the details [1]. If in doubt do not hesitate to ping Jiri Pallich - PgM - for questions.  Once this JIRA has been cloned for a specific release, please update the release spreadsheet [2] with the corresponding JIRA.\n\n[1] https:\/\/docs.google.com\/drawings\/d\/1t4Al1MNPdC8nZzaPjlqzQxztSXd9L7zDXbsxRdPNAk4\/edit\n[2] https:\/\/docs.google.com\/spreadsheets\/d\/1mwx0x_y64H1oCiejW0o9Q19-Htsi8lKT6ZV0Rvz_bRU\/edit#gid=0 \n\nFor micro releases the following steps can be excluded (marked as done when cloned)\n* 1-3\n* 8\n* 9 Only Supported Components details need to be updated\n* 18\n\n"
    },
    "properties": []
}

{
    "fields": {
       "project":
       {
          "key": "TEST"
       },
       "summary": "REST ye merry gentlemen.",
       "description": "Creating of an issue using project keys and issue type names using the REST API",
       "issuetype": {
          "name": "Bug"
       }
   }
}

curl --data '{"fields":{"summary":"[Spring Boot 2.3.10.RELEASE] Release steps CR [31 May 2021]","issuetype":{"id":"3"},"project":{"key":"ENTSBT"},"description":"This is a set of tasks to be completed by the CR date defined in the Summary minus the days indicated in the individual task. Some task need to be completed prior other tasks, please see the dependency graph for the details [1]. If in doubt do not hesitate to ping Jiri Pallich - PgM - for questions.  Once this JIRA has been cloned for a specific release, please update the release spreadsheet [2] with the corresponding JIRA.\n\n[1] https:\/\/docs.google.com\/drawings\/d\/1t4Al1MNPdC8nZzaPjlqzQxztSXd9L7zDXbsxRdPNAk4\/edit\n[2] https:\/\/docs.google.com\/spreadsheets\/d\/1mwx0x_y64H1oCiejW0o9Q19-Htsi8lKT6ZV0Rvz_bRU\/edit#gid=0 \n\nFor micro releases the following steps can be excluded (marked as done when cloned)\n* 1-3\n* 8\n* 9 Only Supported Components details need to be updated\n* 18\n\n"},"properties":[]}' -X POST "https://issues.redhat.com/rest/api/latest/issue"



List projects: https://issues.redhat.com/rest/api/2/project

  {
        "expand": "description,lead,url,projectKeys",
        "self": "https://issues.redhat.com/rest/api/2/project/12320320",
        "id": "12320320",
        "key": "ENTSBT",
        "name": "ENTSBT",
        "avatarUrls": {
            "48x48": "https://issues.redhat.com/secure/projectavatar?avatarId=17263",
            "24x24": "https://issues.redhat.com/secure/projectavatar?size=small&avatarId=17263",
            "16x16": "https://issues.redhat.com/secure/projectavatar?size=xsmall&avatarId=17263",
            "32x32": "https://issues.redhat.com/secure/projectavatar?size=medium&avatarId=17263"
        },
        "projectCategory": {
            "self": "https://issues.redhat.com/rest/api/latest/projectCategory/10030",
            "id": "10030",
            "name": "JBoss Enterprise Middleware",
            "description": "Projects related to JBoss Enterprise Middleware products."
        },
        "projectTypeKey": "software"
    }
    
List projects: https://issues.redhat.com/rest/api/2/issue/createmeta/12320320/issuetypes

{
    "maxResults": 50,
    "startAt": 0,
    "total": 10,
    "isLast": true,
    "values": [
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/1",
            "id": "1",
            "description": "A problem which impairs or prevents the functions of the product.",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13263&avatarType=issuetype",
            "name": "Bug",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/10100",
            "id": "10100",
            "description": "Portfolio Level Initiative",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=17261&avatarType=issuetype",
            "name": "Initiative",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/12",
            "id": "12",
            "description": "A task tracking an update to a bundled component",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13270&avatarType=issuetype",
            "name": "Component Upgrade",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/16",
            "id": "16",
            "description": "Created by Jira Software - do not edit or delete. Issue type for a big user story that needs to be broken down.",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13267&avatarType=issuetype",
            "name": "Epic",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/17",
            "id": "17",
            "description": "Created by Jira Software - do not edit or delete. Issue type for a user story.",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13275&avatarType=issuetype",
            "name": "Story",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/20",
            "id": "20",
            "description": "",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13275&avatarType=issuetype",
            "name": "Requirement",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/21",
            "id": "21",
            "description": "Same as Requirement type but for subtasks",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13276&avatarType=issuetype",
            "name": "Sub-requirement ",
            "subtask": true
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/3",
            "id": "3",
            "description": "A task that needs to be done.",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13278&avatarType=issuetype",
            "name": "Task",
            "subtask": false
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/5",
            "id": "5",
            "description": "The sub-task of the issue",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13276&avatarType=issuetype",
            "name": "Sub-task",
            "subtask": true
        },
        {
            "self": "https://issues.redhat.com/rest/api/2/issuetype/8",
            "id": "8",
            "description": "A one-off patch related to a customer support case",
            "iconUrl": "https://issues.redhat.com/secure/viewavatar?size=xsmall&avatarId=13270&avatarType=issuetype",
            "name": "Support Patch",
            "subtask": false
        }
    ]
}


Fields: https://issues.redhat.com/rest/api/2/issue/createmeta/12320320/issuetypes/3


