FROM frolvlad/alpine-oraclejdk8:slim
ADD classes/artifacts/timespector_bot_jar/timespector-bot.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-Dspring.profiles.active=slack"
ENV rtmUrl="https://slack.com/api/rtm.start?token={token}&simple_latest&no_unreads"
ENV timeDbs="time,time2"
ENV projectDb="project"
ENV timeDbUrl="http://localhost:5984"
ENV projectDbUrl="http://localhost:5984"
EXPOSE 8080
#RUN apk add --update openssl
COPY run.sh /run.sh
CMD sh /run.sh