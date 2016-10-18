import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.freeform.FreeformNetwork;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.pattern.FeedForwardPattern;

/**
 * Adi Sidapara
 * Mr. Truong
 * AP CS
 * 2/19/16
 */
public class NeuralNetTrial {
    public static final boolean useRPROP = false;
    public static final boolean dualHidden = true;
    public static final int ITERATIONS = 1000000;


    public static BasicNetwork basicNetwork;
    public static FreeformNetwork freeformNetwork;

    public static  int inputNum = 4;
    public static int hiddenNeuronNum = 5;
    public static int outputNum = 1;
    public static double XOR_INPUT[][];
    public static double[][] XOR_IDEAL;

public static void main(String[] args){

}

    public static BasicNetwork network = createFeedforwardNetwork();
    public static void compute(){
        int inputNum = 4;
        int hiddenNeuronNum = 5;
        int outputNum = 1;
        network.addLayer(new BasicLayer(null,true,inputNum));
        network.addLayer(new BasicLayer(new ActivationBipolarSteepenedSigmoid(),true,hiddenNeuronNum));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputNum));
        network.getStructure().finalizeStructure();
        network.reset();

        // create training data

        MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);

        // train the neural network
        final Backpropagation train = new Backpropagation(network, trainingSet);

        int epoch = 1;

        do {
            train.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while(train.getError() > 0.01);
        train.finishTraining();

        // test the neural network
//        System.out.println("Neural Network Results:");
//        for(MLDataPair pair: trainingSet ) {
//            final MLData output = network.compute(pair.getInput());
//            System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
//                    + ", actual=" + output.getData(0) /normFactor + ",ideal=" + pair.getIdeal().getData(0) / normFactor);
//        }


    }


    public static double[][] weightsInput(){
        double[][] weightsInputLayer = new double[inputNum][hiddenNeuronNum];
        for(int i = 0; i <= inputNum - 1; i++){
            for(int a = 0; a <= hiddenNeuronNum - 1; a++){
                weightsInputLayer[i][a] = network.getWeight(0, i, a);
            }
        }

        return weightsInputLayer;
    }
    public static double[] weightsOutput(){
        double [] weightOutputLayer = new double[hiddenNeuronNum];
        for(int i = 0; i <= hiddenNeuronNum - 1; i++){

            weightOutputLayer[i] = network.getWeight(1, i, 0);

        }
        return weightOutputLayer;
    }


    static BasicNetwork createFeedforwardNetwork() {
        // construct a feedforward type network
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setActivationFunction(new ActivationSigmoid());
        pattern.setInputNeurons(3);
        pattern.addHiddenLayer(5);
        pattern.setOutputNeurons(1);
        return (BasicNetwork)pattern.generate();
    }
    public static double[][] OutputLayerInput(){
        double[][] weightsInputLayer = new double[inputNum][hiddenNeuronNum];
        double[][] weightOutputLayer = new double[hiddenNeuronNum][outputNum];
        for(int i = 0; i <= inputNum - 1; i++){
            for(int a = 0; a <= hiddenNeuronNum - 1; a++){
                weightsInputLayer[i][a] = network.getWeight(0, i, a);
            }
        }
        for(int i = 0; i <= hiddenNeuronNum - 1; i++){

            weightOutputLayer[i][0] = network.getWeight(1, i, 0);

        }
        return weightsInputLayer;
    }



}

