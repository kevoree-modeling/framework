package org.kevoree.modeling.util.maths.matrix.solvers;

import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.MatrixMatrixMult;
import org.kevoree.modeling.util.maths.matrix.solvers.decomposition.QRDecompositionHouseholderColumn_D64;

public class AdjLinearSolverQr {

    protected int numRows;
    protected int numCols;
    private QRDecompositionHouseholderColumn_D64 decomposer;
    protected int maxRows = -1;
    protected int maxCols = -1;
    protected DenseMatrix64F Q;
    protected DenseMatrix64F R;
    private DenseMatrix64F Y, Z;

    public boolean setA(DenseMatrix64F A) {
        if (A.numRows > maxRows || A.numCols > maxCols) {
            setMaxSize(A.numRows, A.numCols);
        }
        this.numRows = A.numRows;
        this.numCols = A.numCols;
        if (!decomposer.decompose(A)) {
            return false;
        }
        Q.reshapeBoolean(numRows, numRows, false);
        R.reshapeBoolean(numRows, numCols, false);
        decomposer.getQ(Q, false);
        decomposer.getR(R, false);
        return true;
    }

    private void solveU(double U[], double[] b, int n) {
        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            int indexU = i * n + i + 1;
            for (int j = i + 1; j < n; j++) {
                sum -= U[indexU++] * b[j];
            }
            b[i] = sum / U[i * n + i];
        }
    }


    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        int BnumCols = B.numCols;
        Y.reshapeBoolean(numRows, 1, false);
        Z.reshapeBoolean(numRows, 1, false);
        // solve each column one by one
        for (int colB = 0; colB < BnumCols; colB++) {
            // make a copy of this column in the vector
            for (int i = 0; i < numRows; i++) {
                Y.data[i] = B.get(i, colB);
            }
            // Solve Qa=b
            // a = Q'b
            MatrixMatrixMult.multTransA(Q, Y, Z);
            // solve for Rx = b using the standard upper triangular solver
            solveU(R.data, Z.data, numCols);
            // save the results
            for (int i = 0; i < numCols; i++) {
                X.cset(i, colB, Z.data[i]);
            }
        }
    }

    public AdjLinearSolverQr() {
        this.decomposer = new QRDecompositionHouseholderColumn_D64();
    }

    public void setMaxSize(int maxRows, int maxCols) {
        // allow it some room to grow
        maxRows += 5;
        this.maxRows = maxRows;
        this.maxCols = maxCols;
        Q = new DenseMatrix64F(maxRows, maxRows);
        R = new DenseMatrix64F(maxRows, maxCols);
        Y = new DenseMatrix64F(maxRows, 1);
        Z = new DenseMatrix64F(maxRows, 1);
    }

}
