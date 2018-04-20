package io.notnot.timespectorbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;

@Component
public class ProjectDb extends CouchConnect {

    public ProjectDb(@Value("${projectDb}") String name,  @Value("${projectDbUrl}") String url) throws MalformedURLException {
        super(name, url);
    }

    public List<Project> getAllProjects(){
        return getAllDocuments(Project.class);

    }
}
