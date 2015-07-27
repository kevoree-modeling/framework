package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public interface BidiagonalDecomposition <T extends DenseMatrix64F> {
    public T getB( T B , boolean compact );
    public T getU( T U , boolean transpose , boolean compact );
    public T getV( T V ,  boolean transpose , boolean compact );
    public void getDiagonal( double diag[], double off[] );
    public boolean decompose( T orig );
    public boolean inputModified();

}