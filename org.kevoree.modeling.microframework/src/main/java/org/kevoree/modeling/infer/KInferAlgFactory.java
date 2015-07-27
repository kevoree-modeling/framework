package org.kevoree.modeling.infer;

import org.kevoree.modeling.infer.impl.BinaryPerceptronAlg;
import org.kevoree.modeling.infer.impl.GaussianProfiler;
import org.kevoree.modeling.infer.impl.KMeanClusterAlg;
import org.kevoree.modeling.infer.impl.LinearRegressionAlg;

public class KInferAlgFactory {

    public static final KInferAlg build(String name) {
        if (name.equals("BinaryPerceptron")) {
            return new BinaryPerceptronAlg();
        }
        if (name.equals("LinearRegression")) {
            return new LinearRegressionAlg();
        }
        if (name.equals("KMeanCluster")) {
            return new KMeanClusterAlg();
        }
        if (name.equals("GaussianProfiler")) {
            return new GaussianProfiler();
        }
        if (name.equals("GaussianClassifier")) {
            return new GaussianProfiler();
        }
        return null;
    }

}
