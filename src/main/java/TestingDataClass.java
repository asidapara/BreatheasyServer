import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mongodb.*;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adi Sidapara
 * Mr. Truong
 * AP CS
 * 3/30/16
 */
public class TestingDataClass {
    public static MongoClientURI uri = new MongoClientURI("mongodb://asidapara:orhynnyoj7@ds041404.mongolab.com:41404/breatheasy");
    public static MongoClient client = new MongoClient(uri);
    public static DB db = client.getDB(uri.getDatabase());

    public static DBObject lastDoc(int userId) {

        DBCollection coll = db.getCollection("userData");

        BasicDBObject fields = new BasicDBObject();
        fields.put("_id", -1);
        BasicDBObject field = new BasicDBObject();
        DBCursor doc = coll.find(field).sort(fields).limit(1);
        DBObject dOC = doc.next();
        return dOC;
    }

    public static double lastHeartRate(DBObject doc) {
        String heartRateOld = doc.get("heartRate").toString();
        return Double.parseDouble(heartRateOld);
    }

    public static int lastActivityLength(DBObject doc) {
        String heartRateOld = doc.get("activityLength").toString();
        return Integer.parseInt(heartRateOld);
    }

    public static double lastTemperature(DBObject doc) {
        String heartRateOld = doc.get("temperature").toString();
        return Double.parseDouble(heartRateOld);
    }

    public static int lastAQI(DBObject doc) {
        String heartRateOld = doc.get("AQI").toString();
        return Integer.parseInt(heartRateOld);
    }

    public static double getTemperature(int userId, double longitude, double latitude) {
        try {
            com.mashape.unirest.http.HttpResponse<JsonNode> obJ = null;
            try {
                obJ = Unirest.get("http://api.openweathermap.org/data/2.5/weather?lat=33.4930&lon=-111.9689&APPID=7f63027b415cfab7dd4d13c9cac16808").asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            org.json.JSONObject obj2 = obJ.getBody().getObject().getJSONObject("main");
            //the values
            double temp = obj2.getDouble("temp");
            return temp;
        } catch (JSONException e) {
            return lastAQI(lastDoc(userId));
        }
    }

    public static int getAQI(int userId, double longitude, double latitude) {
        try {
            com.mashape.unirest.http.HttpResponse<JsonNode> obj = null;
            try {
                obj = Unirest.get("http://www.airnowapi.org/aq/observation/latLong/current/?format=application/json&latitude=" + latitude + "&longitude=" + longitude + "&distance=100&API_KEY=B73EE5E8-A96B-435C-814B-2369BDA5B24F").asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            org.json.JSONArray array = obj.getBody().getArray();
            org.json.JSONObject obj1 = array.getJSONObject(0);
            int AQI = obj1.getInt("AQI");
            return AQI;
        } catch (JSONException e) {
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

    public static void NeuralNetDataset(int userId, double temperature, int AQI, int activityLength, double heartRateVariability, double presentHeartRate) {

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

    public static AggregationOutput DatabaseConnect(int userId) {


        DBCollection neuralNetDataColl = db.getCollection("neuralNetData");

        DBObject match = new BasicDBObject("$match", new BasicDBObject("_Userid", userId));

        AggregationOutput output = neuralNetDataColl.aggregate(match);
        return output;
    }

    public static ArrayList<double[][]> dataComp(AggregationOutput output) {
        ArrayList<double[][]> temp = new ArrayList<double[][]>();
        for (DBObject d : output.results()) {//structure { { activity-length, aqi, temperature }, {heart-rate} }
            double[][] master = new double[2][];
            double[] result = new double[1];
            double[] array = new double[4];
            result[0] = (double) (Double.parseDouble(d.get("heart-rate").toString()));

            array[0] = (double) (Double.parseDouble(d.get("activity-length").toString()));
            array[1] = (double) (Double.parseDouble(((DBObject) (d.get("location-info"))).get("aqi").toString()));
            array[2] = (Double.parseDouble(((DBObject) (d.get("location-info"))).get("temperature").toString()));

            array[3] = Double.parseDouble(d.get("heart-rate").toString());
            master[0] = array;
            master[1] = result;
            temp.add(master);
        }

        return temp;
    }

    public static double[][] inputNN(ArrayList<double[][]> dataComp) {
        double[][] inputs = new double[dataComp.size()][];
        for (int x = 0; x <= dataComp.size() - 1; x++) {
            inputs[x] = dataComp.get(x)[0];
        }

        return inputs;
    }

    public static double[][] idealNN(ArrayList<double[][]> dataComp) {
        double[][] inputs = new double[dataComp.size()][];
        for (int x = 0; x <= dataComp.size() - 1; x++) {
            inputs[x] = dataComp.get(x)[1];
        }
        return inputs;
    }

    public static double[] outputNN(ArrayList<double[][]> dataComp) {
        double[] inputs = new double[dataComp.size()];
        for (int x = 0; x <= dataComp.size() - 1; x++) {
            inputs[x] = dataComp.get(x)[1][0];
        }
        return inputs;
    }

    public static double[][] normalized(double[][] inputs) {
        double NormFactor = 0;
        double[][] normal = new double[inputs.length][inputs[0].length];
        for (int x = 0; x <= inputs[0].length - 1; x++) {
            double sum = 0;
            for (int a = 0; a <= inputs.length - 1; a++) {
                sum += Math.pow(inputs[a][x], 2);
            }
            if (sum == 0) {
                NormFactor = 0;
            } else {
                NormFactor = 1 / Math.sqrt(sum);

            }
            for (int a = 0; a <= inputs.length - 1; a++) {

                normal[a][x] = inputs[a][x] * NormFactor;
            }
        }
        return normal;
    }

    public static double[][] denormalized(double[][] inputs, double[][] normal) {
        double[][] DENORM = new double[normal.length][normal[0].length];
        for (int x = 0; x <= inputs[0].length - 1; x++) {
            double sum = 0;
            for (int a = 0; a <= inputs.length - 1; a++) {
                sum += Math.pow(inputs[a][x], 2);
            }
            double NormFactor = 1 / Math.sqrt(sum);
            for (int a = 0; a <= inputs.length - 1; a++) {
                DENORM[a][x] = normal[a][x] / NormFactor;
            }
        }
        return DENORM;
    }

    public static double normalFactor(double[][] inputs, int x) {
        double sum = 0;
        for (int a = 0; a <= inputs.length - 1; a++) {
            sum += Math.pow(inputs[a][x], 2);
        }
        double NormFactor = 1 / Math.sqrt(sum);
        return NormFactor;
    }

    public static double normalFactor(double[] inputs) {
        double sum = 0;
        for (int a = 0; a <= inputs.length - 1; a++) {
            sum += Math.pow(inputs[a], 2);
        }
        double NormFactor = 1 / Math.sqrt(sum);
        return NormFactor;
    }
    public static Logger mongoLogger = Logger.getLogger( "com.mongodb");


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        mongoLogger.setLevel(Level.SEVERE);
        PrintWriter writer = new PrintWriter("BreatheasyData2.txt", "UTF-8");



        int activitylength = 77;
        int[] userIds = {6, 7, 8, 10};
        int[] userIds2 = {6, 7, 8, 10};
        double[][][] testData = {{{60.4839, 64.3777}, {43.1035, 46.2963}, {47.9233, 49.6689}, {41.6667, 45.4545}, {45.4545, 49.1803}, {44.7761, 47.3186}, {42.9799, 46.7290}, {44.7761, 48.3871}, {48.7013, 53.1915}, {47.1698, 51.0204}}, {{67.8733, 77.03}, {51.3699, 55.5556}, {62.7615, 68.8073}, {64.1026, 66.0793}, {57.0342, 61.4754}, {61.4754, 63.5593}, {59.7610, 64.1026}, {53.7634, 57.4713}, {54.7445, 60.9756}, {58.1395, 62.2407}}, {{50.6757, 55.5556}, {56.1798, 59.2885}, {57.2519, 61.4754}, {53.9568, 56.1798}, {51.7241, 55.9701}, {50.1672, 56.5038}, {47.6190, 53.1915}, {52.8169, 59.7610}, {55.7621, 66.6667}, {50.8475, 56.6038}}, {{60.4839, 61.4754}, {60.9756, 63.2911}, {60.2410, 62.5000}, {61.4754, 63.8298}, {63.8298, 65.2174}, {61.7284, 66.0793}, {57.6923, 60.9756}, {57.9151, 61.9835}, {58.5937, 60.0000}, {59.5238, 61.4754}}, {{44.3787, 49.6689}, {44.2478, 49.3421}, {45.1807, 52.8169}, {45.4545, 50.0}, {43.4783, 49.0196}, {43.6046, 49.6689}, {42.7350, 47.7707}, {43.2277, 49.6689}, {43.9883, 49.5050}, {42.8571, 49.3421}}, {{71.4286, 73.1707}, {73.5294, 74.6269}, {70.4225, 70.4638}, {70.4225, 71.4286}, {71.4286, 72.4638}, {71.7703, 73.1707}, {69.4444, 70.4225}, {76.9231, 75.7576}, {77.3196, 75.3769}, {77.3196, 76.1421}}, {{71.7703, 75.3769}, {60.9756, 61.2245}, {66.6667, 69.4444}, {66.6667, 73.1707}, {67.2646, 72.4638}, {64.6552, 70.4225}, {62.7615, 66.3717}, {64.6552, 67.2646}, {62.7615, 66.3717}, {63.2911, 67.2646}}};
        int time = 1;
        double temp = 298.0;
        int AQI = 34;

        for (int x = 0; x <= userIds.length - 1; x++) {
            int userId = userIds[x];
            writer.println("\n" + "\n" + "\n" + "M" + userId + "\n");
            writer.println("predicted:" + "\n" + "\n");
            for(int y = 0; y <= testData[x].length - 1;y++){
                double heartRate = testData[x][y][0];
                double[] input = {(temp - lastTemperature(lastDoc(userId))) / time, (AQI - lastAQI(lastDoc(userId))) / time, (activitylength - lastActivityLength(lastDoc(userId))) / time, (heartRate - lastHeartRate(lastDoc(userId))) / time, heartRate};
                ImmediateLayer layer = new ImmediateLayer(userId);
                double[] normalized = {(normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 0) * input[0]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 1) * input[1]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 2) * input[2]), (normalFactor(inputNN(dataComp(DatabaseConnect(userId))), 3) * input[3])};
                double val = (layer.computeValue(normalized) / normalFactor(outputNN(dataComp(DatabaseConnect(userId)))));
                double prediction = heartRate + val;
                if(val > 60){
                    prediction = val;
                }
                writer.println(prediction);

        }
            writer.println("actual:" + "\n" + "\n");
            for(int y = 0; y <= testData[x].length - 1;y++){
                double heartRate = testData[x][y][1];
                writer.println(heartRate);
            }
        }
        writer.close();

    }
}

