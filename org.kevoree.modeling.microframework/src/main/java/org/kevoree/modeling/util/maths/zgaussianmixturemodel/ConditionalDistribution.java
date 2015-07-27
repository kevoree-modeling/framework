package org.kevoree.modeling.util.maths.zgaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class ConditionalDistribution {

    public SimpleMatrix[] conditionalMeans;

    public SimpleMatrix[] conditionalCovs;

    public double[] conditionalWeights;

    public ConditionalDistribution(SimpleMatrix[] conditionalMeans, SimpleMatrix[] conditionalCovs, double[] conditionalWeights) {
        this.conditionalMeans = conditionalMeans;
        this.conditionalCovs = conditionalCovs;
        this.conditionalWeights = conditionalWeights;
    }



}
