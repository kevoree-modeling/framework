package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class BidiagonalDecompositionTall_D64
        implements BidiagonalDecomposition<DenseMatrix64F>
{
    QRxColPivDecompositionHouseholderColumn_D64 decompQRP = new QRxColPivDecompositionHouseholderColumn_D64();
    BidiagonalDecomposition<DenseMatrix64F> decompBi = new BidiagonalDecompositionRow_D64(1);

    DenseMatrix64F B = new DenseMatrix64F(1,1);

    // number of rows
    int m;
    // number of column
    int n;
    // min(m,n)
    int min;

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        diag[0] = B.getValueAtIndex(0);
        for( int i = 1; i < n; i++ ) {
            diag[i] = B.get(i, i);
            off[i-1] = B.get(i - 1, i);
        }
    }

    @Override
    public DenseMatrix64F getB(DenseMatrix64F B, boolean compact) {
        B = BidiagonalDecompositionRow_D64.handleB(B, compact, m, n, min);

        B.set(0,0,this.B.get(0,0));
        for( int i = 1; i < min; i++ ) {
            B.set(i,i, this.B.get(i,i));
            B.set(i-1,i, this.B.get(i-1,i));
        }
        if( n > m)
            B.set(min-1,min,this.B.get(min-1,min));

        return B;
    }

    @Override
    public DenseMatrix64F getU(DenseMatrix64F U, boolean transpose, boolean compact) {
        U = BidiagonalDecompositionRow_D64.handleU(U, false, compact, m, n, min);

        if( compact ) {
            // U = Q*U1
            DenseMatrix64F Q1 = decompQRP.getQ(null,true);
            DenseMatrix64F U1 = decompBi.getU(null,false,true);
            CommonOps.mult(Q1, U1, U);
        } else {
            // U = [Q1*U1 Q2]
            DenseMatrix64F Q = decompQRP.getQ(U,false);
            DenseMatrix64F U1 = decompBi.getU(null,false,true);
            DenseMatrix64F Q1 = CommonOps.extract4Int(Q, 0, Q.numRows, 0,min);
            DenseMatrix64F tmp = new DenseMatrix64F(Q1.numRows,U1.numCols);
            CommonOps.mult(Q1,U1,tmp);
            CommonOps.insert(tmp,Q,0,0);
        }

        if( transpose )
            CommonOps.transpose(U);

        return U;
    }

    @Override
    public DenseMatrix64F getV(DenseMatrix64F V, boolean transpose, boolean compact) {
        return decompBi.getV(V,transpose,compact);
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {

        if( !decompQRP.decompose(orig) ) {
            return false;
        }

        m = orig.numRows;
        n = orig.numCols;
        min = Math.min(m, n);
        B.reshapeBoolean(min, n, false);

        decompQRP.getR(B,true);

        // apply the column pivots.
        // TODO this is horribly inefficient
        DenseMatrix64F result = new DenseMatrix64F(min,n);
        DenseMatrix64F P = decompQRP.getPivotMatrix(null);
        CommonOps.multTransB(B, P, result);
        B.setMatrix(result);

        return decompBi.decompose(B);
    }

    @Override
    public boolean inputModified() {
        return decompQRP.inputModified();
    }
}