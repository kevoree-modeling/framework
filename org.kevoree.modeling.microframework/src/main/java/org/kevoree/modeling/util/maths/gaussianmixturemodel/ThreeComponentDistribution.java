package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class ThreeComponentDistribution extends MultipleComponentDistribution{
    private final static int NO_OF_COMPONENTS = 3;

    public ThreeComponentDistribution(double[] weights, SimpleMatrix[] means, SimpleMatrix[] covariances, SimpleMatrix bandwidth) {
        super();
        super.setValues(weights, means, covariances, bandwidth);
    }

}
