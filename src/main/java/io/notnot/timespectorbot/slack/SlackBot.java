package io.notnot.timespectorbot.slack;

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
import java.util.stream.Collectors;

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
        String projects = projectDb.getAllProjects().stream().map(p -> p.getName()).collect(Collectors.joining(", "));
        startConversation(event, "whatProject");
        reply(session, event, "What project would you like to se summary of \n" + projects);
    }

    @Controller
    public void whatProject(WebSocketSession session, Event event) throws ParseException {
        Matcher matcher = Pattern.compile("(.*) (\\d{8})").matcher(event.getText());
        ArrayList hours = new ArrayList();

        String projectName = event.getText();
        List<Time> times = null;

        for (TimeDb timeDb : timeDbs) {

            if (matcher.matches()) {
                projectName = matcher.group(1);
                times = timeDb.getInterval(matcher.group(2));
            } else {
                times = timeDb.getAllTime();
            }

            for (Time time : times) {
                if (time.getProjectName().equalsIgnoreCase(projectName)) {
                    hours.add(time.getHours());
                }
            }
        }
        reply(session, event, projectName + " contains " + hours.stream().mapToInt(p -> (int) p).sum() + " hours");
        stopConversation(event);
    }
}