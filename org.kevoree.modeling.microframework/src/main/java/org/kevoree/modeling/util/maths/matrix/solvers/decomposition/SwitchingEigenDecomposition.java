package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.Complex64F;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.MatrixFeatures;

public class SwitchingEigenDecomposition{
    private double tol;

    SymmetricQRAlgorithmDecomposition_D64 symmetricAlg;
    WatchedDoubleStepQRDecomposition_D64 generalAlg;

    boolean symmetric;
    // should it compute eigenvectors or just eigenvalues?
    boolean computeVectors;

    DenseMatrix64F A = new DenseMatrix64F(1,1);


    public SwitchingEigenDecomposition( int matrixSize , boolean computeVectors , double tol ) {

        TridiagonalDecompositionHouseholder_D64 decomp = new TridiagonalDecompositionHouseholder_D64();
        symmetricAlg= new SymmetricQRAlgorithmDecomposition_D64(decomp,computeVectors);


        generalAlg = new WatchedDoubleStepQRDecomposition_D64(computeVectors);
        this.computeVectors = computeVectors;
        this.tol = tol;
    }

   /* public SwitchingEigenDecomposition( int matrixSize ) {
        this(matrixSize,true,1e-8);
    }*/


    public int getNumberOfEigenvalues() {
        return symmetric ? symmetricAlg.getNumberOfEigenvalues() :
                generalAlg.getNumberOfEigenvalues();
    }


    public Complex64F getEigenvalue(int index) {
        return symmetric ? symmetricAlg.getEigenvalue(index) :
                generalAlg.getEigenvalue(index);
    }


    public DenseMatrix64F getEigenVector(int index) {
        if( !computeVectors )
            throw new RuntimeException("Configured to not compute eignevectors");

        return symmetric ? symmetricAlg.getEigenVector(index) :
                generalAlg.getEigenVector(index);
    }


    public boolean decompose(DenseMatrix64F orig) {
        A.setMatrix(orig);

        symmetric = MatrixFeatures.isSymmetricDouble(A, tol);

        return symmetric ?
                symmetricAlg.decompose(A) :
                generalAlg.decompose(A);

    }


    public boolean inputModified() {
        // since it doesn't know which algorithm will be used until a matrix is provided make a copy
        // of all inputs
        return false;
    }
}