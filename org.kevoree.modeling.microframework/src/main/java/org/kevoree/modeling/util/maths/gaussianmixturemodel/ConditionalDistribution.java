package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

import java.util.ArrayList;

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
