package org.kevoree.modeling.infer;

/**
 * Created by assaad on 13/02/15.
 */
public class GaussianClassificationKInfer {



    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @param alpha is the learning rate of the linear regression
     */
    private double alpha = 0.05;


    /*
    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        GaussianArrayKInferState currentState = (GaussianArrayKInferState) modifyState();
        int featuresize = trainingSet[0].length;


        double[][] features = new double[trainingSet.length][];
        boolean[] results = new boolean[expectedResultSet.length];

        for (int i = 0; i < trainingSet.length; i++) {
            features[i] = new double[featuresize];
            for (int j = 0; j < featuresize; j++) {
                features[i][j] = (double) trainingSet[i][j];
            }
            results[i] = (boolean) expectedResultSet[i];
            currentState.train(features[i], results[i], alpha);
        }
    }

    @Override
    public Object infer(Object[] features) {
        GaussianArrayKInferState currentState = (GaussianArrayKInferState) readOnlyState();
        double[] ft = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            ft[i] = (double) features[i];
        }
        return currentState.infer(ft);
    }

    @Override
    public Object accuracy(Object[][] testSet, Object[] expectedResultSet) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public KInferState createEmptyState() {
        return null;
    }
    */

}
