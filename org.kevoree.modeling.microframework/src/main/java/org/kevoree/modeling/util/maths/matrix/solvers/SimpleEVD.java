package org.kevoree.modeling.util.maths.matrix.solvers;

import org.kevoree.modeling.util.maths.matrix.Complex64F;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.decomposition.SwitchingEigenDecomposition;

public class SimpleEVD <T extends SimpleMatrix>
{
    private SwitchingEigenDecomposition eig;

    DenseMatrix64F mat;

    public SimpleEVD( DenseMatrix64F mat )
    {
        this.mat = mat;
       // eig = DecompositionFactory.eig(mat.getNumCols,true);
        eig = new SwitchingEigenDecomposition(mat.numCols,true,1e-8);
        if( !eig.decompose(mat))
            throw new RuntimeException("Eigenvalue Decomposition failed");
    }

    public int getNumberOfEigenvalues() {
        return eig.getNumberOfEigenvalues();
    }

    public Complex64F getEigenvalue( int index ) {
        return eig.getEigenvalue(index);
    }


    public T getEigenVector( int index ) {
        return (T)SimpleMatrix.wrap(eig.getEigenVector(index));
    }


    public SwitchingEigenDecomposition getEVD() {
        return eig;
    }


    public int getIndexMax() {
        int indexMax = 0;
        double max = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m > max ) {
                max = m;
                indexMax = i;
            }
        }

        return indexMax;
    }


    public int getIndexMin() {
        int indexMin = 0;
        double min = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m < min ) {
                min = m;
                indexMin = i;
            }
        }

        return indexMin;
    }
}