# Timespector Bot
 Timespector bot gives a summary of hours from the Timespector app

 ## How to use this image
 ---
Start the bot with:
```
$ docker run -d --name timespector-bot \
-e slackBotToken="SLACK_API_TOKEN" \
-e timeDbs="COUCH_DB_TIME_NAME,COUCH_DB_TIME_NAME" \
-e timeDbUrl="COUCH_DB_TIME_URL" \
-e projectDbUrl="COUCH_DB_PROJECT_URL" \
notnot/timespector-bot
```

 ## Setup
---
#### Create Slack Bot integration
Go to [Create a Slack Bot](https://api.slack.com/bot-users) page and, if you haven't already, generate a token. This is your `SLACK_API_TOKEN`.

## Usage
---
* Type `timesum` in chanel where timespector-bot is active.
* Timespector-bot will reply you with a list of all projects.
* Chose a project from the list with a number and Timespector-bot reply's with corresponding hours.
* If you enter a date ex. 20180410 after the project number the the summary you receive will be from that date.


* Type `sumall` in chanel where timespector-bot is active.
* Timespector-bot will reply with a list of all projects.
* If you enter `sumall` with a date after ex.`20180410` the summary you receive will be from that date.

## Environment Variables
---
Before you start the timespector-bot there are four environment variables you need to add to the Dockerfile, and one variable that you use before you run the docker command.

`SLACK_API_TOKEN`
This token can be generated att the [Create a Slack Bot](https://api.slack.com/bot-users) page.

`COUCH_DB_TIME_NAME`
This variable holds the name of the time database. It can handle multiple databases and is separated with a ","
Default value: time,time2

`COUCH_DB_PROJECT_NAME`
This variable holds the name of the project database.
Name of database where projects ar retrieved.
Default value: project

`COUCH_DB_TIME_URL`
This variable holds the url to the time database.
Default value: http://localhost:5984

`COUCH_DB_PROJECT_URL`
This variable holds the url to the project database.
Default value: http://localhost:5984