package io.notnot.timespectorbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectDb extends CouchConnect {

    public ProjectDb(@Value("${projectDb}") String name,  @Value("${projectDbUrl}") String url) throws MalformedURLException {
        super(name, url);
    }

    public List<Project> getAllProjects(){
        return getAllDocuments(Project.class);

    }

    public List<Project> getAllActive(){
        return getAllDocuments(Project.class).stream().filter(project -> project.getStatus().equals("ACTIVE")).collect(Collectors.toList());
    }
}
