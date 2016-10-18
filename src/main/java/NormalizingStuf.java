
import java.util.Arrays;

/**
 * Adi Sidapara
 * Mr. Truong
 * AP CS
 * 1/15/16
 */
public class NormalizingStuf {
    public static void main(String[] args){
     double[][] test = {{4.6, 2.3, 5.3} , { 23.52, 3.4, 234.0234234234} , {23423.345323, 2932.111111, 12.3242}};

        System.out.println(Arrays.deepToString(normalized(test)));
        System.out.println(normalized(test)[0][0] / normalFactor(test, 0));

    }
    public static double[][] normalized(double[][] inputs){
        for(int x = 0; x <= inputs[0].length - 1; x++){
            double sum = 0;
            for(int a = 0; a <= inputs.length - 1; a++){
                sum += Math.pow(inputs[a][x], 2);
            }
            double NormFactor = 1 / Math.sqrt(sum);
            for(int a = 0; a <= inputs.length - 1; a++){
                inputs[a][x] *= NormFactor;
            }
        }
        return inputs;
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

}
