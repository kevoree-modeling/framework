package org.kevoree.modeling.util.maths.zgaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class TwoComponentDistribution extends MultipleComponentDistribution {
    private final static int NO_OF_COMPONENTS = 2;


    public TwoComponentDistribution(double[] weights, SimpleMatrix[] means, SimpleMatrix[] covariances, SimpleMatrix bandwidth){
        super();
        super.setValues(weights, means, covariances, bandwidth);
    }



}
