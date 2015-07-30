package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;


public class RecommendationAlg implements KInferAlg {
    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject currentInferObject, KInternalDataManager manager) {

    }

    @Override
    public double[][] infer(double[][] features, KObject currentInferObject, KInternalDataManager manager) {
        return new double[0][];
    }
}
