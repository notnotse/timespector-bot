package io.notnot.timespectorbot;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;

public class TimeDb extends CouchConnect{


    public TimeDb(String name, String url) throws MalformedURLException {
        super(name, url);

    }



    public List<Time> getAllTime(){
       return getAllDocuments(Time.class);
    }

    public Object getSpecificTime(String s){
        return getSpecificObject(Time.class, s);
    }

    public List<Time> getInterval(String from) throws ParseException {
        return getIntervalDoc(Time.class, from);
    }
}
