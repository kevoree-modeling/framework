package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class HashableSimpleMatrix extends SimpleMatrix {

    public HashableSimpleMatrix(SimpleMatrix m) {
        super(m.numRows(),m.numCols());
        this.mat=m.getMatrix();
    }

    @Override
    public boolean equals(Object obj) {
        SimpleMatrix m = (SimpleMatrix) obj;
        if(m.isIdentical(this, 1E-30))
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return (int) this.getValue2D(this.numRows()-1,0);
    }
}
