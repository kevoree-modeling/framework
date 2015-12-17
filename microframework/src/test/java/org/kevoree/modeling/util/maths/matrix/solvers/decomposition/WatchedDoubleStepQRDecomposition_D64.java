package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.Complex64F;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class WatchedDoubleStepQRDecomposition_D64 {

    HessenbergSimilarDecomposition_D64 hessenberg;
    WatchedDoubleStepQREigenvalue algValue;
    WatchedDoubleStepQREigenvector algVector;

    DenseMatrix64F H;

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public WatchedDoubleStepQRDecomposition_D64(boolean computeVectors) {
        hessenberg = new HessenbergSimilarDecomposition_D64(10);
        algValue = new WatchedDoubleStepQREigenvalue();
        algVector = new WatchedDoubleStepQREigenvector();

        this.computeVectors = computeVectors;
    }


    public boolean decompose(DenseMatrix64F A) {

        if( !hessenberg.decompose(A) )
            return false;

        H = hessenberg.getH(null);

        algValue.getImplicitQR().createR = false;
//        algValue.getImplicitQR().setChecks(true,true,true);

        if( !algValue.process(H) )
            return false;

//        for( int i = 0; i < A.getNumRows; i++ ) {
//            System.out.println(algValue.getEigenvalues()[i]);
//        }

        algValue.getImplicitQR().createR = true;

        if( computeVectors )
            return algVector.process(algValue.getImplicitQR(), H, hessenberg.getQ(null));
        else
            return true;
    }


    public boolean inputModified() {
        return hessenberg.inputModified();
    }


    public int getNumberOfEigenvalues() {
        return algValue.getEigenvalues().length;
    }


    public Complex64F getEigenvalue(int index) {
        return algValue.getEigenvalues()[index];
    }

    public DenseMatrix64F getEigenVector(int index) {
        return algVector.getEigenvectors()[index];
    }
}