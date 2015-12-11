/**
 * Created by adisidapara on 10/28/15.
 */
import static spark.Spark.*;
import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mongodb.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

             import com.mashape.unirest.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class Main {
    public static void main(String[] args) throws UnknownHostException, UnirestException {
//
        JSONParser parser = new JSONParser();
        int instance = 1;
        get("/*/*/*/*/*", (request, response) -> {
            System.out.println(request.splat()[0].toString());
                          double height = Double.parseDouble(request.splat()[0].toString());
            int heartRate = Integer.parseInt(request.splat()[1].toString());
            int activityLength = Integer.parseInt(request.splat()[2].toString());
            int lon = Integer.parseInt(request.splat()[3].toString());
            int lat = Integer.parseInt(request.splat()[4].toString());
            String query = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID=7f63027b415cfab7dd4d13c9cac16808";
                   com.mashape.unirest.http.HttpResponse<JsonNode> req = Unirest.get(query).asJson();

            Object myObj = parser.parse(req.getBody().toString());
            MongoClientURI uri  = new MongoClientURI("mongodb://asidapara:orhynnyoj7@ds041404.mongolab.com:41404/breatheasy");
            JSONObject main = (JSONObject)myObj;


            double msg = Double.parseDouble(main.get("main").toString());                                                                                                        MongoClient client = new MongoClient(uri);

                                    DB db = client.getDB(uri.getDatabase());
                     DBCollection coll = db.getCollection("testCollection");
                     BasicDBObject doc = new BasicDBObject("instance", instance)

                              .append("height", height)
                              .append("heart-rate", heartRate)
                              .append("activity-length", activityLength)
                             .append("location-info", new BasicDBObject("longitude", lon).append("latitude", lat).append("aqi", "").append("temperature", msg));





        coll.insert(doc);
        return msg;
    });
}
}
