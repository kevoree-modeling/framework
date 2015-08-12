package org.kevoree.modeling.infer;

import org.kevoree.modeling.infer.impl.BinaryPerceptronAlg;
import org.kevoree.modeling.infer.impl.GaussianProfiler;
import org.kevoree.modeling.infer.impl.KMeanClusterAlg;
import org.kevoree.modeling.infer.impl.LinearRegressionAlg;
import org.kevoree.modeling.util.PrimitiveHelper;

public class KInferAlgFactory {

    public static final KInferAlg build(String name) {
        if (PrimitiveHelper.equals(name, "BinaryPerceptron")) {
            return new BinaryPerceptronAlg();
        }
        if (PrimitiveHelper.equals(name, "LinearRegression")) {
            return new LinearRegressionAlg();
        }
        if (PrimitiveHelper.equals(name, "KMeanCluster")) {
            return new KMeanClusterAlg();
        }
        if (PrimitiveHelper.equals(name, "GaussianProfiler")) {
            return new GaussianProfiler();
        }
        if (PrimitiveHelper.equals(name, "GaussianClassifier")) {
            return new GaussianProfiler();
        }
        return null;
    }

}
