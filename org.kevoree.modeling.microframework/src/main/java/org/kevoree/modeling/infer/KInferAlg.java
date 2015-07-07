package org.kevoree.modeling.infer;

import org.kevoree.modeling.KObject;

public interface KInferAlg {

    void train(double[][] trainingSet, double[] expectedResultSet, KObject origin);

    double[] infer(double[] features, KObject origin);

}
