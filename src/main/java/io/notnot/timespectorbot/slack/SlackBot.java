package io.notnot.timespectorbot.slack;

import io.notnot.timespectorbot.Project;
import io.notnot.timespectorbot.ProjectDb;
import io.notnot.timespectorbot.Time;
import io.notnot.timespectorbot.TimeDb;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.JBot;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.WebSocketSession;
import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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

    @Controller(pattern = "(timesum)", next = "whatProject")
    public void timeSum(WebSocketSession session, Event event) {
        List<Project> allProjects = projectDb.getAllProjects();

        String projects="0 All projects\n";
        int idx = 0;
        for(Project project : allProjects) {
            idx++;
            projects = projects.concat( idx+" "+project.getName()+"\n");
        }

        startConversation(event, "whatProject");
        reply(session, event, "What project would you like to se summary of \n" + projects +"\n");
    }

    @Controller
    public void whatProject(WebSocketSession session, Event event) throws ParseException {
        Matcher pattern = Pattern.compile("(.*) (\\d{8})").matcher(event.getText());
        ArrayList hours = new ArrayList();

        int projectNumber = 0;
        List<Time> timeList = null;
        List<Project> allProjects = projectDb.getAllProjects();
        String projectName= "All projects";

        for (TimeDb timeDb : timeDbs) {
            if (pattern.matches()) {
                projectNumber = Integer.parseInt(pattern.group(1));
                timeList = timeDb.getInterval(pattern.group(2));
            } else {
                projectNumber = Integer.parseInt(event.getText());
                timeList = timeDb.getAllTime();
            }

            if (projectNumber>0){
                projectName = allProjects.get(projectNumber - 1).getName();
            }

            for (Time time : timeList) {
                if (projectNumber==0){
                    hours.add(time.getHours());
                } else if (time.getProjectName().equals(projectName)) {
                    hours.add(time.getHours());
                }
            }
        }

        reply(session, event,  projectName +" contains " + hours.stream().mapToInt(p -> (int) p).sum() + " hours");
        stopConversation(event);
    }

    @Controller(pattern = "(sumall)")
    public void sumall(WebSocketSession session, Event event) throws ParseException {
        Matcher pattern = Pattern.compile("(.*) (\\d{8})").matcher(event.getText());

        String projects = "";
        List<Project> allProjects = projectDb.getAllProjects();
        List<Time> timeList = null;
        ArrayList hours = new ArrayList();
        ArrayList allHours = new ArrayList();

        for (Project project : allProjects){
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
                    }
                }
            }
            projects = projects.concat(project.getName()+" "+hours.stream().mapToInt(p -> (int)p).sum()+" hours"+"\n");
            hours.clear();
        }

        reply(session, event, "All projects with corresponding hours\n\n"+projects+"\n\nAll hours combined: "+allHours.stream().mapToInt(p -> (int)p).sum());
        stopConversation(event);
    }
}