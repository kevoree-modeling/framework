package org.kevoree.modeling.infer;

/**
 * Created by assaad on 11/02/15.
 */

//TODO needs update to live adapt - this is currently a copy paste of offline
public class PolynomialOnlineKInfer {

    public int maxDegree = 20;

    public double getToleratedErr() {
        return toleratedErr;
    }

    public void setToleratedErr(double toleratedErr) {
        this.toleratedErr = toleratedErr;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public double toleratedErr = 0.01;

    private double calculateLong(long time, double[] weights, long timeOrigin, long unit) {
        double t = ((double) (time - timeOrigin)) / unit;
        return calculate(weights, t);
    }

    private double calculate(double[] weights, double t) {
        double result = 0;
        double power = 1;
        for (int j = 0; j < weights.length; j++) {
            result += weights[j] * power;
            power = power * t;
        }
        return result;
    }

/*
    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        PolynomialKInferState currentState = (PolynomialKInferState) modifyState();

        double[] weights;
        int featuresize = trainingSet[0].length;


        long[] times = new long[trainingSet.length];
        double[] results = new double[expectedResultSet.length];

        for (int i = 0; i < trainingSet.length; i++) {
            times[i] = (Long) trainingSet[i][0];
            results[i] = (double) expectedResultSet[i];
        }

        if (times.length == 0) {
            return;
        }

        if (times.length == 1) {
            weights = new double[1];
            weights[0] = results[0];
            currentState.setWeights(weights);
            return;
        }

        int maxcurdeg = Math.min(times.length, maxDegree);
        Long timeOrigin = times[0];
        Long unit = times[1] - times[0];

        double[] normalizedTimes = new double[times.length];
        for (int i = 0; i < times.length; i++) {
            normalizedTimes[i] = ((double) (times[i] - times[0])) / unit;
        }

        for (int deg = 0; deg < maxcurdeg; deg++) {
            PolynomialFitEjml pf = new PolynomialFitEjml(deg);
            pf.fit(normalizedTimes, results);
            if (PolynomialKInferState.maxError(pf.getCoef(), normalizedTimes, results) <= toleratedErr) {
                currentState.setUnit(unit);
                currentState.setTimeOrigin(timeOrigin);
                currentState.setWeights(pf.getCoef());
                return;
            }
        }

        //TODO If reached here, there should be split polynomial somewhere

    }


    @Override
    public Object infer(Object[] features) {
        PolynomialKInferState currentState = (PolynomialKInferState) readOnlyState();

        long time = (Long) features[0];
        return currentState.infer(time);


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