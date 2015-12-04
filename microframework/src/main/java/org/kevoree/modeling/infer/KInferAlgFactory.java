package org.kevoree.modeling.infer;

import org.kevoree.modeling.infer.impl.*;
import org.kevoree.modeling.util.PrimitiveHelper;

public class KInferAlgFactory {

    public static final KInferAlg build(String name) {
        if (PrimitiveHelper.equals(name, "BinaryPerceptron")) {
            return new BinaryPerceptronAlg();
        }
        else if (PrimitiveHelper.equals(name, "LinearRegression")) {
            return new LinearRegressionAlg();
        }
        else if (PrimitiveHelper.equals(name, "KMeanCluster")) {
            return new KMeanClusterAlg();
        }
        else if (PrimitiveHelper.equals(name, "GaussianProfiler")) {
            return new GaussianProfiler();
        }
        else if (PrimitiveHelper.equals(name, "GaussianClassifier")) {
            return new GaussianClassifierAlg();
        }
        else if (PrimitiveHelper.equals(name, "GaussianAnomalyDetection")) {
            return new GaussianAnomalyDetectionAlg();
        }
        else if (PrimitiveHelper.equals(name, "Winnow")) {
            return new WinnowAlg();
        }
        else if (PrimitiveHelper.equals(name, "EmptyInfer")) {
            return new EmptyInfer();
        }
        return null;
    }

}
