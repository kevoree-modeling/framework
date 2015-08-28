package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class QRDecompositionHouseholderColumn_D64 {

    protected KArray2D dataQR; // [ column][ row ]
    // used internally to store temporary data
    protected double v[];
    // universe of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;
    // the computed gamma for Q_k matrix
    protected double gammas[];
    // local variables
    protected double gamma;
    protected double tau;
    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize(int numRows, int numCols) {
        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numCols, numRows);
        int maxLength = Math.max(numCols, numRows);
        if (dataQR == null || dataQR.rows() < numRows || dataQR.columns()<numCols) {
            dataQR = new NativeArray2D(numRows,numCols);
            v = new double[maxLength];
            gammas = new double[minLength];
        }
        if (v.length < maxLength) {
            v = new double[maxLength];
        }
        if (gammas.length < minLength) {
            gammas = new double[minLength];
        }
    }

    public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact) {
        if (compact) {
            if (Q == null) {
                Q = DenseMatrix64F.identity(numRows, minLength);
            } else {
                DenseMatrix64F.setIdentity(Q);
            }
        } else {
            if (Q == null) {
                Q = DenseMatrix64F.widentity(numRows);
            } else {
                DenseMatrix64F.setIdentity(Q);
            }
        }
        for (int j = minLength - 1; j >= 0; j--) {
            //double u[] = dataQR[j];
            double vv = dataQR.get(j,j);
            dataQR.set(j,j,1);
            rank1UpdateMultR(Q, dataQR,j, gammas[j], j, j, numRows, v);
            dataQR.set(j,j,vv);
        }
        return Q;
    }

    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
        if (R == null) {
            if (compact) {
                R = new DenseMatrix64F(minLength, numCols);
            } else
                R = new DenseMatrix64F(numRows, numCols);
        } else {
            for (int i = 0; i < R.numRows; i++) {
                int min = Math.min(i, R.numCols);
                for (int j = 0; j < min; j++) {
                    R.cset(i, j, 0);
                }
            }
        }
        for (int j = 0; j < numCols; j++) {
           // double colR[] = dataQR[j];
            int l = Math.min(j, numRows - 1);
            for (int i = 0; i <= l; i++) {
                double val = dataQR.get(i,j);
                R.cset(i, j, val);
            }
        }
        return R;
    }

    public boolean decompose(DenseMatrix64F A) {
        setExpectedMaxSize(A.numRows, A.numCols);
        convertToColumnMajor(A);
        error = false;
        for (int j = 0; j < minLength; j++) {
            householder(j);
            updateA(j);
        }
        return !error;
    }

    protected void convertToColumnMajor(DenseMatrix64F A) {
        for (int x = 0; x < numCols; x++) {
           // double colQ[] = dataQR[x];
            for (int y = 0; y < numRows; y++) {
                dataQR.set(y,x, A.data[y * numCols + x]);
            }
        }
    }

    protected void householder(int j) {
       // final double u[] = dataQR[j];
        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        final double max = findMax(dataQR,j, j, numRows - j);
        if (max == 0.0) {
            gamma = 0;
            error = true;
        } else {
            // computes tau and normalizes u by max
            tau = computeTauAndDivide(j, numRows, dataQR,j, max);
            // divide u by u_0
            double u_0 = dataQR.get(j,j) + tau;
            divideElements(j + 1, numRows, dataQR,j, u_0);
            gamma = u_0 / tau;
            tau *= max;
           dataQR.set(j,j, -tau);
        }
        gammas[j] = gamma;
    }

    protected void updateA(int w) {
       // final double u[] = dataQR[w];
        for (int j = w + 1; j < numCols; j++) {
           //final double colQ[] = dataQR[j];
            double val = dataQR.get(w,j);
            for (int k = w + 1; k < numRows; k++) {
                val += dataQR.get(k,w) * dataQR.get(k,j);
            }
            val *= gamma;
            dataQR.add(w,j,-val);
            for (int i = w + 1; i < numRows; i++) {
                dataQR.add(i,j, -dataQR.get(i,w) * val);
            }
        }
    }

    public static double findMax(KArray2D u, int col, int startU, int length) {
        double max = -1;
        int index = startU;
        int stopIndex = startU + length;
        for (; index < stopIndex; index++) {
            double val = u.get(index,col);
            val = (val < 0.0) ? -val : val;
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    public static void divideElements(final int j, final int numRows, KArray2D u, int col, final double u_0) {
        for (int i = j; i < numRows; i++) {
            u.set(i,col,u.get(i,col)/ u_0);
        }
    }

    public static double computeTauAndDivide(final int j, final int numRows, KArray2D u, int col, final double max) {
        double tau = 0;
        for (int i = j; i < numRows; i++) {
            u.set(i,col,u.get(i,col)/max);
            double d = u.get(i,col);
            tau += d * d;
        }
        tau = Math.sqrt(tau);
        if (u.get(j,col) < 0) {
            tau = -tau;
        }
        return tau;
    }


    public boolean inputModified() {
        return false;
    }

    public static void rank1UpdateMultR(DenseMatrix64F A, KArray2D u,int col, double gamma, int colA0, int w0, int w1, double _temp[]) {
        for (int i = colA0; i < A.numCols; i++) {
            _temp[i] = u.get(w0,col) * A.data[w0 * A.numCols + i];
        }
        for (int k = w0 + 1; k < w1; k++) {
            int indexA = k * A.numCols + colA0;
            double valU = u.get(k,col);
            for (int i = colA0; i < A.numCols; i++) {
                _temp[i] += valU * A.data[indexA++];
            }
        }
        for (int i = colA0; i < A.numCols; i++) {
            _temp[i] *= gamma;
        }
        for (int i = w0; i < w1; i++) {
            double valU = u.get(i,col);
            int indexA = i * A.numCols + colA0;
            for (int j = colA0; j < A.numCols; j++) {
                A.data[indexA++] -= valU * _temp[j];
            }
        }
    }


}