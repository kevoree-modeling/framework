package org.kevoree.modeling.util.maths.matrix.solvers;

import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class LinearSolverLu_D64 {

    protected DenseMatrix64F A;
    protected int numRows;
    protected int numCols;

    public DenseMatrix64F getA() {
        return A;
    }

    protected void _setA(DenseMatrix64F A) {
        this.A = A;
        this.numRows = A.numRows;
        this.numCols = A.numCols;
    }



    protected LUDecompositionAlt_D64 decomp;

    public LinearSolverLu_D64(LUDecompositionAlt_D64 decomp) {
        this.decomp = decomp;

    }

    public boolean setA(DenseMatrix64F A) {
        _setA(A);
        return decomp.decompose(A);
    }


    public void invert(DenseMatrix64F A_inv) {
        double []vv = decomp._getVV();
        DenseMatrix64F LU = decomp.getLU();

        int n = A.numCols;

        double dataInv[] = A_inv.data;

        for( int j = 0; j < n; j++ ) {
            // don't need to change inv into an identity matrix before hand
            for( int i = 0; i < n; i++ ) vv[i] = i == j ? 1 : 0;
            decomp._solveVectorInternal(vv);
//            for( int i = 0; i < n; i++ ) dataInv[i* n +j] = vv[i];
            int index = j;
            for( int i = 0; i < n; i++ , index += n) dataInv[ index ] = vv[i];
        }
    }

    public void improveSol( DenseMatrix64F b , DenseMatrix64F x )
    {


        double dataA[] = A.data;
        double dataB[] = b.data;
        double dataX[] = x.data;

        final int nc = b.numCols;
        final int n = b.numCols;

        double []vv = decomp._getVV();

        for( int k = 0; k < nc; k++ ) {
            for( int i = 0; i < n; i++ ) {
                double sdp = -dataB[ i * nc + k];
                for( int j = 0; j < n; j++ ) {
                    sdp += dataA[i* n +j] * dataX[ j * nc + k];
                }
                vv[i] = sdp;
            }
            decomp._solveVectorInternal(vv);
            for( int i = 0; i < n; i++ ) {
                dataX[i*nc + k] -= vv[i];
            }
        }
    }

    public boolean modifiesA() {
        return false;
    }

    public boolean modifiesB() {
        return false;
    }



    boolean doImprove = false;



    public void solve(DenseMatrix64F b, DenseMatrix64F x) {
       int numCols = b.numCols;

        double dataB[] = b.data;
        double dataX[] = x.data;

        double []vv = decomp._getVV();

        for( int j = 0; j < numCols; j++ ) {
            int index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) vv[i] = dataB[index];
            decomp._solveVectorInternal(vv);
            index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) dataX[index] = vv[i];
        }

        if( doImprove ) {
            improveSol(b,x);
        }
    }
}
