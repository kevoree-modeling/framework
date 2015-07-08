package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.meta.KMetaDependencies;

//TODO
public class AvgInferAlg implements KInferAlg {

    @Override
    public void train(double[][] trainingSet, double[] expectedResultSet, KObject origin, KMetaDependencies meta) {


    }

    @Override
    public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {
        return null;
    }

}
