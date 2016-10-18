import com.mongodb.*;
import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.pattern.FeedForwardPattern;
import org.mongodb.morphia.Morphia;

/**
 * Adi Sidapara
 * Mr. Truong
 * AP CS
 * 1/3/16
 */
public class ImmediateLayer {
//serves as underlying neural network
    public static Morphia morphia = new Morphia();
    public static int userId;
    public ImmediateLayer(int id){userId = id;}
    public static double computeValue(double [] input){

        int inputNum = 4;
        int hiddenNeuronNum = 5;
        int outputNum = 1;
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(null,true,inputNum));
        network.addLayer(new BasicLayer(new ActivationBipolarSteepenedSigmoid(),true,hiddenNeuronNum));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputNum));
        network.getStructure().finalizeStructure();
        for(int i = 0; i <= inputNum - 1; i++){
            for(int b = 0; b <= hiddenNeuronNum - 1; b++){
               network.setWeight(0, i, b, weightsInput(lastDoc())[i][b]);
            }

        }
        for(int i = 0; i <= hiddenNeuronNum - 1; i++){
                network.setWeight(1, i, 0, weightsOutput(lastDoc())[i]);

        }
        MLData data = new BasicMLData(input);
        return (network.compute(data)).getData(0);
        }
    static BasicNetwork createFeedforwardNetwork() {
        // construct a feedforward type network
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setActivationFunction(new ActivationSigmoid());
        pattern.setInputNeurons(4);
        pattern.addHiddenLayer(5);
        pattern.setOutputNeurons(1);
        return (BasicNetwork)pattern.generate();
    }
    public static DBObject lastDoc(){
        MongoClientURI uri  = new MongoClientURI("mongodb://asidapara:orhynnyoj7@ds041404.mongolab.com:41404/breatheasy");
        MongoClient client= new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());
        DBCollection coll = db.getCollection("weightsCollection");

        BasicDBObject fields = new BasicDBObject();
        BasicDBObject field = new BasicDBObject();
        field.put("_UserId", userId);
        fields.put("_id", -1);
        DBCursor doc = coll.find(field).sort(fields).limit(1);
        DBObject dOC = doc.next();
        return dOC;
    }
    public static double[][] weightsInput(DBObject doc){
        BasicDBList list = (BasicDBList) doc.get("layer1");
        double[][] weights = new double[list.size()][((BasicDBList)list.get(0)).size()];
        for(int x = 0; x < list.size(); x++){
            for(int y = 0; y < ((BasicDBList)list.get(0)).size(); y++){
                weights[x][y] = Double.parseDouble(((BasicDBList)list.get(x)).get(y).toString());
            }
        }


        return weights;
    }
    public static double[] weightsOutput(DBObject doc){
        BasicDBList list = (BasicDBList) doc.get("layer2");
        double[] weights = new double[list.size()];
        for(int x = 0; x < list.size(); x++){

                weights[x] = Double.parseDouble((list.get(x)).toString());

        }


        return weights;
    }
        }

