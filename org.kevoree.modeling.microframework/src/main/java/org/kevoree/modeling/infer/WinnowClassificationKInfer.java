package org.kevoree.modeling.infer;

/**
 * This class create a live learner classifier using the winnow algorithm.
 * The learning hypothesis is that the result y is according to majority of votes of experts xi.
 * It classifies to class 0 if the majority of experts vote 0;
 * It classifies to class 1 if the majority of experts vote 1;
 * The state is an array of doubles containing the voting weights of experts.
 * There are two parameters to defined for this class:
 * Alpha is the reward in term of voting power multiplication for experts who guess right (usually x2).
 * Beta is the penalty in term of voting power division for experts who guess wrong (usually /2).
 * Created by assaad on 11/02/15.
 */
public class WinnowClassificationKInfer {

    /**
     * @param alpha is the reward multiplier when the expert is correct
     */
    private double alpha=2;

    /**
     * @param beta is the penalty divider when the expert is wrong
     */
    private double beta=2;

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    private double calculate(double[] weights, double[] features) {
        double result=0;
        for(int i=0; i<features.length;i++){
            result+= weights[i]*features[i];
        }
        if(result>=features.length){
            return 1.0;
        }
        else{
            return 0.0;
        }
    }

/*
    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        DoubleArrayKInferState currentState = (DoubleArrayKInferState) modifyState();
        double[] weights=currentState.getWeights();

        int featuresize=trainingSet[0].length;

        if(weights==null){
            weights=new double[featuresize];
            for(int i=0; i<weights.length;i++){
                weights[i]=2;
            }
        }


        double[][] features=new double[trainingSet.length][];
        double[] results = new double[expectedResultSet.length];

        for(int i=0;i<trainingSet.length;i++){
            features[i] = new double[featuresize];
            for(int j=0;j<featuresize;j++){
                features[i][j]=(double) trainingSet[i][j];
            }
            results[i]=(double) expectedResultSet[i];
        }

            for(int i=0;i<trainingSet.length;i++){

                //If the learning fit continue.
                if(calculate(weights,features[i])==results[i])
                    continue;

                //Else update the weights

                if(results[i]==0) {
                    for (int j = 0; j < features[i].length; j++) {
                        if(features[i][j]!=0){
                            weights[j]=weights[j]/beta;
                        }

                    }
                }
                else{
                    for (int j = 0; i < features[i].length; j++) {
                        if(features[i][j]!=0){
                            weights[j]=weights[j]*alpha;
                        }

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