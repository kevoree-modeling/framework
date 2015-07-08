package org.kevoree.modeling.infer;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaDependencies;

public interface KInferAlg {

    void train(double[][] trainingSet, double[] expectedResultSet, KObject currentInferObject, KMetaDependencies meta);

    double[] infer(double[] features, KObject origin, KMetaDependencies meta);

}
