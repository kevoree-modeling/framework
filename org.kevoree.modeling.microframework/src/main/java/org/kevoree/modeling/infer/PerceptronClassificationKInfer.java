package org.kevoree.modeling.infer;

/**
 * This class create a live learner classifier of perceptron algorithm.
 * The learning hypothesis is that the result y is in a linear correlation with the features xi.
 * It classifies to class 0 if the sum is negative;
 * It classifies to class 1 if the sum is positive;
 * The state is an array of doubles containing the ai and at the end the constant.
 * There are two parameters to defined for this class: the learning rate alpha, and the number of iterations.
 * Created by assaad on 10/02/15.
 */


public class PerceptronClassificationKInfer  {

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
    private double alpha=0.001;

    /**
     * @param iterations is the number of passes of the live learning on the training set
     */
    private int iterations=100;

    private double calculate(double[] weights, double[] features) {
        double res=0;
        for(int i=0; i<features.length;i++){
            res = res + weights[i]*(features[i]);
        }
        //The bias variable is encoded as the last weight.
        res = res + weights[features.length];
        if(res>=0){
            return 1; //Class 1
        }
        else {
            return 0; //Class 0
        }
    }

    /*
    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) modifyState();
        double[] weights=currentState.getWeights();

        int featuresize=trainingSet[0].length;

        if(weights==null){
            weights=new double[featuresize+1];
        }

        double[][] features=new double[trainingSet.length][];
        double[] results = new double[expectedResultSet.length];

        for(int i=0;i<trainingSet.length;i++){
            features[i] = new double[featuresize];
            for(int j=0;j<featuresize;j++){
                features[i][j]=(double) trainingSet[i][j];
            }
            results[i]=(double) expectedResultSet[i];
            if(results[i]==0){
                results[i]=-1;
            }
        }

     for(int j=0; j<iterations;j++) {
            for(int i=0;i<trainingSet.length;i++){
                double h = calculate(weights, features[i]);
                if(h==0){
                    h=-1;
                }

                if(h*results[i]<=0){
                    for(int k=0; k<featuresize;k++){
                        weights[k]=weights[k]+alpha*(results[i]*features[i][k]);
                    }
                    //Updating the bias
                    weights[featuresize]=weights[featuresize]+alpha*(results[i]);
                }
            }

        }

        currentState.setWeights(weights);
    }

    @Override
    public Object infer(Object[] features) {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) readOnlyState();
        double[] weights=currentState.getWeights();
        double[] ft=new double[features.length];
        for(int i=0;i<features.length;i++){
            ft[i]=(double)features[i];
        }
        return calculate(weights,ft);


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
    }
    */
}
