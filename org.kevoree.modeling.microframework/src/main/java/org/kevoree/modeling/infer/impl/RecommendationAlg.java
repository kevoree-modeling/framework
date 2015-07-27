package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;


public class RecommendationAlg implements KInferAlg {
    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject currentInferObject) {

    }

    @Override
    public double[][] infer(double[][] features, KObject currentInferObject) {
        return new double[0][];
    }
}
