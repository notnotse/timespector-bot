package io.notnot.timespectorbot.slack;

import io.notnot.timespectorbot.Project;
import io.notnot.timespectorbot.ProjectDb;
import io.notnot.timespectorbot.Time;
import io.notnot.timespectorbot.TimeDb;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.common.JBot;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JBot
@Profile("slack")
public class SlackBot extends Bot {

    @Value("${slackBotToken}")
    private String slackToken;

    @Value("${timeDbs}")
    private String timeDbNames;

    @Value("${timeDbUrl}")
    private String timeDbUrl;
    private List<TimeDb> timeDbs = new ArrayList<>();

    private final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(1);

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);

    @Autowired
    private ProjectDb projectDb;

    @PostConstruct
    public void init() throws MalformedURLException {
        String[] dbNames = timeDbNames.split(",");
        for (String dbName : dbNames) {
            timeDbs.add(new TimeDb(dbName, timeDbUrl));
        }

    }

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket is now connected: {}", session);
        logger.info("Scheduling ping");

        long initialDelay = 10;
        long period = 300;
        TimeUnit unit = TimeUnit.SECONDS;
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("Sending Spring PingMessage");
                    session.sendMessage(new PingMessage());
                } catch (Exception e) {
                    logger.error("Could not send ping", e);
                } 			}
        }, initialDelay, period, unit);
    }

    @Controller(pattern = "(timesum)", events = {EventType.DIRECT_MESSAGE, EventType.MESSAGE}, next = "whatProject")
    public void timeSum(WebSocketSession session, Event event) {
        List<Project> allActive = projectDb.getAllActive();
        String projects="0 All projects\n";
        int idx = 0;
        for(Project project : allActive) {
            idx++;
            projects = projects.concat( idx+" "+project.getName()+"\n");
        }
        startConversation(event, "whatProject");
        reply(session, event, "What project would you like to se summary of \n" + projects + "\n");
    }

    @Controller(events = {EventType.DIRECT_MESSAGE, EventType.MESSAGE})
    public void whatProject(WebSocketSession session, Event event) throws ParseException {
        Matcher pattern = Pattern.compile("(.*) (\\d{8})").matcher(event.getText());

        int projectNumber = 0;
        List<Time> timeList = null;
        List<Project> allActive = projectDb.getAllActive();
        String projectName= "All projects";
        String projetId= null;
        ArrayList allHours = new ArrayList();
        ArrayList billableHours = new ArrayList();
        ArrayList nonBillableHours = new ArrayList();


        for (TimeDb timeDb : timeDbs) {
            if (pattern.matches()) {
                projectNumber = Integer.parseInt(pattern.group(1));
                timeList = timeDb.getInterval(pattern.group(2));
            } else {
                projectNumber = Integer.parseInt(event.getText());
                timeList = timeDb.getAllTime();
            }
            if (projectNumber>0){
                projectName = allActive.get(projectNumber - 1).getName();
                projetId = allActive.get(projectNumber -1).getId();
            }
            for (Time time : timeList) {
                if (projectNumber==0){
                    allHours.add(time.getHours());
                } else if (projetId.equals(time.getProjectId())) {
                    allHours.add(time.getHours());
                    if (time.isBillable()){
                        billableHours.add(time.getHours());
                    } else nonBillableHours.add(time.getHours());
                }
            }
        }

        reply(session, event,  projectName+"\n\n" + billableHours.stream().mapToInt(p -> (int)p).sum() + " Hrs Billable\n" + nonBillableHours.stream().mapToInt(p -> (int) p).sum() + " Hrs Non-Billable\n" + allHours.stream().mapToInt(p -> (int) p).sum() + " Hrs Total");
        stopConversation(event);
    }

    @Controller(pattern = "(sumall)", events = {EventType.DIRECT_MESSAGE, EventType.MESSAGE})
    public void sumall(WebSocketSession session, Event event) throws ParseException {
        Matcher pattern = Pattern.compile("(.*) (\\d{8})").matcher(event.getText());

        String projects = "";
        List<Project> allActive = projectDb.getAllActive();
        List<Time> timeList = null;
        ArrayList hours = new ArrayList();
        ArrayList allHours = new ArrayList();
        ArrayList billableHours = new ArrayList();
        ArrayList nonBillableHours = new ArrayList();
        ArrayList billableTotal = new ArrayList();
        ArrayList nonBillableTotal = new ArrayList();

        for (Project project : allActive){
            for (TimeDb timeDb : timeDbs){
                if (pattern.matches()){
                    timeList = timeDb.getInterval(pattern.group(2));
                } else {
                    timeList = timeDb.getAllTime();
                }
                for (Time time : timeList) {
                    if (project.getName().equals(time.getProjectName())) {
                            hours.add(time.getHours());
                            allHours.add(time.getHours());
                            if (time.isBillable()){
                                billableHours.add(time.getHours());
                                billableTotal.add(time.getHours());
                            } else{
                                nonBillableHours.add(time.getHours());
                                nonBillableTotal.add(time.getHours());
                            }
                        }
                    }
                }
            projects = projects.concat(project.getName() + "\n" + billableHours.stream().mapToInt(p -> (int)p).sum() + " Hrs Billable\n" + nonBillableHours.stream().mapToInt(p -> (int)p).sum() + " Hrs Non-Billable\n"+ hours.stream().mapToInt(p -> (int)p).sum() + " Hrs Total\n\n ");
            hours.clear();
            billableHours.clear();
            nonBillableHours.clear();
        }
        reply(session, event, "All projects with corresponding hours\n\n" + projects + "\n\nSummary for all projects\n" + billableTotal.stream().mapToInt(p -> (int)p).sum() + " Hrs Billable\n" + nonBillableTotal.stream().mapToInt(p -> (int)p).sum() + " Hrs Non-Billable\n"+ allHours.stream().mapToInt(p -> (int)p).sum() + " Hrs Total");
    }
}