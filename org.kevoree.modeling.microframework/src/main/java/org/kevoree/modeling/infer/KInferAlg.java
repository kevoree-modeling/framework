package org.kevoree.modeling.infer;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaDependencies;

public interface KInferAlg {

    void train(double[][] trainingSet, double[][] expectedResultSet, KObject currentInferObject);

    double[] infer(double[][] features, KObject currentInferObject);

}
