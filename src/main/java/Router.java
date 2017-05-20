/**
 * Adi Sidapara
 * Mr. Truong
 * AP CS
 * 2/19/16
 */

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mongodb.*;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.port;

public class Router {
    int id = 1;

    public static String address = null;
    public static MongoClientURI uri  = new MongoClientURI(address);
    public static MongoClient client= new MongoClient(uri);
    public static DB db = client.getDB(uri.getDatabase());
    public static Logger mongoLogger = Logger.getLogger( "org.mongodb.driver");

    public static void main(String[] args) {
        mongoLogger.setLevel(Level.SEVERE);
    port(1234);
        get("/:userId/compute/:length/:heartrate/:time/:latitude/:longitude", (request, response) -> {

            int userId = Integer.parseInt(request.params("userId"));

            int activitylength = Integer.parseInt(request.params(":length"));
            double heartRate = Double.parseDouble(request.params(":heartrate"));
            int time = Integer.parseInt(request.params(":time"));
            double latitude = (double) (Float.parseFloat(request.params(":latitude")));
            double longitude = (double) (Float.parseFloat(request.params(":longitude")));
            update(userId, longitude, latitude, activitylength, heartRate);
            NeuralNetDataset(userId, ((lastTemperature(lastDoc(userId)) - getTemperature(userId, longitude, latitude)) / time), (lastAQI(lastDoc(userId)) - getAQI(userId, longitude, latitude)) / time, (lastActivityLength(lastDoc(userId)) - activitylength) /time, (lastHeartRate(lastDoc(userId)) - heartRate) / time, heartRate);
            double[] input = {(lastTemperature(lastDoc(userId)) - getTemperature(userId, longitude, latitude)) / time, (lastAQI(lastDoc(userId)) - getAQI(userId, longitude, latitude)) / time, (lastActivityLength(lastDoc(userId)) - activitylength) /time, (lastHeartRate(lastDoc(userId)) - heartRate) / time, heartRate};
            ImmediateLayer layer = new ImmediateLayer(userId);
            double[] normalized = {(normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 0) * input[0]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 1) * input[1]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 2) * input[2]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 3) * input[3])};
            double val = (layer.computeValue(normalized) / normalFactor(outputNN(dataComp(DatabaseConnect(userId)))));
            /*
            Objectives:
            1) parse post / get requests
            2) tie this post / get request to neural networks
            3) create a method that runs the underlying network to train it
            4) upload it as a jar
            5) do swag

             */
            return val;
        });
        get("/:userId/add/:length/:heartrate/:time/:latitude/:longitude", (request, response) -> {

            int userId = Integer.parseInt(request.params("userId"));
            int activitylength = Integer.parseInt(request.params(":length"));
            double heartRate = Double.parseDouble(request.params(":heartrate"));
            int time = Integer.parseInt(request.params(":time"));
            double latitude = (double) (Float.parseFloat(request.params(":latitude")));
            double longitude = (double) (Float.parseFloat(request.params(":longitude")));
            update(userId, longitude, latitude, activitylength, heartRate);
            double[][] input = {{(getTemperature(userId, longitude, latitude)) / time, (getAQI(userId, longitude, latitude)) / time, (activitylength) /time, (heartRate) / time}};
            System.out.println(Arrays.deepToString(input));
            /*
            Objectives:
            1) parse post / get requests
            2) tie this post / get request to neural networks
            3) create a method that runs the underlying network to train it
            4) upload it as a jar
            5) do swag

             */
            response.status(200);
            response.body("added");
            return response.toString();
        });
        get("/:userId/train", (request, response) -> {
            int userId = Integer.parseInt(request.params("userId"));
            double [][] inputs = normalized(inputNN(dataComp(DatabaseConnect(userId))));
            double [][] outputs = normalized(idealNN(dataComp(DatabaseConnect(userId))));
            UnderlyingNet NN = new UnderlyingNet(inputs, outputs, userId);
            NN.compute();
            response.body("trained");
            response.status(200);
            return response.toString();
        });
        get("/:userId/normalize", (request, response) -> {
            int userId = Integer.parseInt(request.params("userId"));

            double val1 = normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 0);
return "swag";
        });

        get("/:userId/computeTest/:length/:heartrate/:time/:temperature/:AQI", (request, response) -> {
            int userId = Integer.parseInt(request.params("userId"));

            int activitylength = Integer.parseInt(request.params(":length"));
            double heartRate = Double.parseDouble(request.params(":heartrate"));
            int time = Integer.parseInt(request.params(":time"));
            double temp = Double.parseDouble(request.params(":temperature"));
            int AQI = Integer.parseInt(request.params(":AQI"));

            double[] input = {(temp - lastTemperature(lastDoc(userId))) / time, (AQI - lastAQI(lastDoc(userId))) / time, (activitylength - lastActivityLength(lastDoc(userId))) /time, (heartRate - lastHeartRate(lastDoc(userId))) / time, heartRate};
            ImmediateLayer layer = new ImmediateLayer(userId);
            double[] normalized = {(normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 0) * input[0]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 1) * input[1]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 2) * input[2]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 3) * input[3])};
            double val = (layer.computeValue(normalized) / normalFactor(outputNN(dataComp(DatabaseConnect(userId)))));

            return val;
        });

    }

public static DBObject lastDoc(int userId){

    DBCollection coll = db.getCollection("userData");

    BasicDBObject fields = new BasicDBObject();
    fields.put("_id", -1);
    BasicDBObject field = new BasicDBObject();
    DBCursor doc = coll.find(field).sort(fields).limit(1);
    DBObject dOC = doc.next();
            return dOC;
}
    public static double lastHeartRate(DBObject doc){
        String heartRateOld = doc.get("heartRate").toString();
        return Double.parseDouble(heartRateOld);
    }
    public static int lastActivityLength(DBObject doc){
        String heartRateOld = doc.get("activityLength").toString();
        return Integer.parseInt(heartRateOld);
    }
    public static double lastTemperature(DBObject doc){
        String heartRateOld = doc.get("temperature").toString();
        return Double.parseDouble(heartRateOld);
    }
    public static int lastAQI(DBObject doc){
        String heartRateOld = doc.get("AQI").toString();
        return Integer.parseInt(heartRateOld);
    }
    public static double getTemperature(int userId, double longitude, double latitude){
        try{
        com.mashape.unirest.http.HttpResponse<JsonNode> obJ = null;
        try {
            obJ = Unirest.get("http://api.openweathermap.org/data/2.5/weather?lat=33.4930&lon=-111.9689&APPID=7f63027b415cfab7dd4d13c9cac16808").asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        org.json.JSONObject obj2 = obJ.getBody().getObject().getJSONObject("main");
        //the values
        double temp = obj2.getDouble("temp");
        return temp;}

        catch(JSONException e){
            return lastAQI(lastDoc(userId));
        }
    }
    public static int getAQI(int userId, double longitude, double latitude){
        try{
        com.mashape.unirest.http.HttpResponse<JsonNode> obj = null;
        try {
            obj = Unirest.get("http://www.airnowapi.org/aq/observation/latLong/current/?format=application/json&latitude=" + latitude + "&longitude=" + longitude + "&distance=100&API_KEY=B73EE5E8-A96B-435C-814B-2369BDA5B24F").asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        org.json.JSONArray array = obj.getBody().getArray();
        org.json.JSONObject obj1 = array.getJSONObject(0);
        int AQI = obj1.getInt("AQI");
        return AQI;}
        catch(JSONException e){
            return lastAQI(lastDoc(userId));
        }
    }
    public static void update(int userId, double longitude, double latitude, int activityLength, double heartRate) throws UnirestException {


        DBCollection coll = db.getCollection("userData");
        double temp = getTemperature(userId, longitude, latitude);
        int AQI = getAQI(userId, longitude, latitude);
        BasicDBObject data = new BasicDBObject();
        data.append("_Userid", userId);
        //adds the respective elements into an arraylist, which will later be stored in a different collection
        data.append("temperature", temp);
        data.append("AQI", AQI);
        data.append("activityLength", activityLength);
        data.append("heartRate", heartRate);
        //this is how the arraylist is formatted: { ∆temp, ∆AQI, ∆ActivityLength, ∆heartRate }

        coll.insert(data);

        //add the part where neuralnet sends data back as well
    }
    public static void NeuralNetDataset(int userId, double temperature, int AQI, int activityLength, double heartRateVariability, double presentHeartRate){

        DBCollection neuralNetDataColl = db.getCollection("neuralNetData");
        BasicDBObject doc = new BasicDBObject();
        doc.append("heart-rate", heartRateVariability);
        doc.append("presentHeartRate", presentHeartRate);
        doc.append("activity-length", activityLength);
        doc.append("_Userid", userId);
        BasicDBObject doc2 = new BasicDBObject();
        doc2.append("aqi", AQI);
        doc2.append("temperature", temperature);
        doc.append("location-info", doc2);
        neuralNetDataColl.insert(doc);
    }
    public static AggregationOutput DatabaseConnect(int userId){


        DBCollection neuralNetDataColl = db.getCollection("neuralNetData");

        DBObject match = new BasicDBObject("$match", new BasicDBObject("_Userid", userId));

        AggregationOutput output =  neuralNetDataColl.aggregate(match);
        return output;
    }
    public static ArrayList<double[][]> dataComp(AggregationOutput output){
        ArrayList<double[][]> temp = new ArrayList<double[][]>();
        for(DBObject d: output.results()) {//structure { { activity-length, aqi, temperature }, {heart-rate} }
            double[][] master = new double[2][];
            double [] result = new double[1];
            double[] array = new double[4];
            result[0] = (double)(Double.parseDouble(d.get("heart-rate").toString()));
System.out.println(d.toString());
            array[0] = (double)(Double.parseDouble(d.get("activity-length").toString()));
            array[1] = (double)(Double.parseDouble(((DBObject)(d.get("location-info"))).get("aqi").toString()));
            array[2] = (Double.parseDouble(((DBObject)(d.get("location-info"))).get("temperature").toString()));

            array[3] = Double.parseDouble(d.get("heart-rate").toString());
            master[0] = array;
            master[1] = result;
            temp.add(master);
        }
        System.out.println(temp.size());
        return temp;
    }
    public static double[][] inputNN(ArrayList<double[][]> dataComp){
        double[][] inputs = new double[dataComp.size()][];
        for(int x = 0; x <= dataComp.size() - 1; x++){
            inputs[x] = dataComp.get(x)[0];
        }
        System.out.println(inputs.length);
        return inputs;
    }
    public static double[][] idealNN(ArrayList<double[][]> dataComp){
        double[][] inputs = new double[dataComp.size()][];
        for(int x = 0; x <= dataComp.size() - 1; x++){
            inputs[x] = dataComp.get(x)[1];
        }
        return inputs;
    }
    public static double[] outputNN(ArrayList<double[][]> dataComp){
        double[] inputs = new double[dataComp.size()];
        for(int x = 0; x <= dataComp.size() - 1; x++){
            inputs[x] = dataComp.get(x)[1][0];
        }
        return inputs;
    }

    public static double[][] normalized(double[][] inputs){
        double NormFactor = 0;
        double[][] normal = new double[inputs.length][inputs[0].length];
        for(int x = 0; x <= inputs[0].length - 1; x++){
            double sum = 0;
            for(int a = 0; a <= inputs.length - 1; a++){
                sum += Math.pow(inputs[a][x], 2);
            }
            if(sum == 0){
                NormFactor = 0;
            }
            else {
                NormFactor = 1 / Math.sqrt(sum);

            }
            for(int a = 0; a <= inputs.length - 1; a++){

                normal[a][x] = inputs[a][x] * NormFactor;
            }
        }
        return normal;
    }
    public static double[][] denormalized(double[][] inputs, double[][] normal){
        double[][] DENORM = new double[normal.length][normal[0].length];
        for(int x = 0; x <= inputs[0].length - 1; x++){
            double sum = 0;
            for(int a = 0; a <= inputs.length - 1; a++){
                sum += Math.pow(inputs[a][x], 2);
            }
            double NormFactor = 1 / Math.sqrt(sum);
            for(int a = 0; a <= inputs.length - 1; a++){
                DENORM[a][x] = normal[a][x] / NormFactor;
            }
        }
        return DENORM;
    }
    public static double normalFactor(double[][] inputs, int x){
        double sum = 0;
        for(int a = 0; a <= inputs.length - 1; a++){
            sum += Math.pow(inputs[a][x], 2);
        }
        double NormFactor = 1 / Math.sqrt(sum);
        return NormFactor;
    }
    public static double normalFactor(double[] inputs){
        double sum = 0;
        for(int a = 0; a <= inputs.length - 1; a++){
            sum += Math.pow(inputs[a], 2);
        }
        double NormFactor = 1 / Math.sqrt(sum);
        return NormFactor;
    }
}
