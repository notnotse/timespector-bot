package io.notnot.timespectorbot;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ViewQuery;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public abstract class CouchConnect {

    private final CouchDbConnector db;



    public CouchConnect(String name, String url) throws MalformedURLException {
        db = connect(name, url);
    }

    private CouchDbConnector connect(String dbName, String url) throws MalformedURLException {
        CouchDbConnector db;
        HttpClient httpClient = new StdHttpClient.Builder()
                .url(url)
                .build();


        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        db = new StdCouchDbConnector(dbName, dbInstance);
        return db;
    }


    public  <T> List<T> getAllDocuments(Class<T> aClass){
        ViewQuery q = new ViewQuery() .allDocs() .endKey("_") .includeDocs(true);
        return db.queryView(q, aClass);
    }

    public <T> Object getSpecificObject(Class<T> aClass, String s){
        return db.get(aClass, s);
    }

    public <T> List<T> getIntervalDoc(Class<T> aClass, String dateFrom) throws ParseException {
        DateFormat srcDf = new SimpleDateFormat("yyyymmdd");
        Date date = srcDf.parse(dateFrom);
        DateFormat destDf = new SimpleDateFormat("yyyy-mm-dd");
        dateFrom = destDf.format(date);


        ViewQuery q = new ViewQuery() .allDocs() .startKey(dateFrom) .endKey("_") .includeDocs(true);
        return db.queryView(q, aClass);
    }

}