package org.kevoree.modeling.infer;

/**
 * This class create a live learner of a linear regression.
 * The learning hypothesis is that the result y is in a linear correlation with the features xi.
 * H(y)= a1*x1 + a2*x2 + ... + an*xn + c
 * The state is an array of doubles containing the ai and at the end the constant.
 * There are two parameters to defined for this class: the learning rate alpha, and the number of iterations.
 * Created by assaad on 10/02/15.
 */
public class LinearRegressionKInfer {


    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * @param alpha is the learning rate of the linear regression
     */
    private double alpha = 0.0001;

    /**
     * @param iterations is the number of passes of the live learning on the training set
     */
    private int iterations = 100;

    private double calculate(double[] weights, double[] features) {
        double result = 0;
        for (int i = 0; i < features.length; i++) {
            result += weights[i] * features[i];
        }
        result += weights[features.length];
        return result;
    }

    /*

    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) modifyState();
        double[] weights = currentState.getWeights();
        int featuresize = trainingSet[0].length;
        if (weights == null) {
            weights = new double[featuresize + 1];
            Random random = new Random();
            for(int i=0; i<size+1;i++){
                weights[i]=random.nextDouble();
            }
        }
        double[][] features = new double[trainingSet.length][];
        double[] results = new double[expectedResultSet.length];
        for (int i = 0; i < trainingSet.length; i++) {
            features[i] = new double[featuresize];
            for (int j = 0; j < featuresize; j++) {
                features[i][j] = (double) trainingSet[i][j];
            }
            results[i] = (double) expectedResultSet[i];
        }
        for (int j = 0; j < iterations; j++) {
            for (int i = 0; i < trainingSet.length; i++) {
                double h = calculate(weights, features[i]);
                double err = -alpha * (h - results[i]);
                for (int k = 0; k < featuresize; k++) {
                    weights[k] = weights[k] + err * features[i][k];
                }
                weights[featuresize] = weights[featuresize] + err;
            }

        }
        currentState.setWeights(weights);
    }

    @Override
    public Object infer(Object[] features) {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) readOnlyState();
        double[] weights = currentState.getWeights();
        double[] ft = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            ft[i] = (double) features[i];
        }
        return calculate(weights, ft);
    }

    @Override
    public Object accuracy(Object[][] testSet, Object[] expectedResultSet) {
        return null;
    }

    @Override
    public void clear() {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) modifyState();
        currentState.setWeights(null);
    }

    @Override
    public KInferState createEmptyState() {
        return new DoubleArrayKInferState();
    }*/
}
