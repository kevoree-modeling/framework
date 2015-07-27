package org.kevoree.modeling.util.maths.matrix.solvers;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class LUDecompositionAlt_D64{
    protected DenseMatrix64F LU;
    protected int maxWidth=-1;

    protected int m,n;
    protected double dataLU[];
    protected double vv[];
    protected int indx[];
    protected int pivot[];

    // used by determinant
    protected double pivsign;


    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new DenseMatrix64F(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new double[ maxWidth ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public DenseMatrix64F getLU() {
        return LU;
    }

    public int[] getIndx() {
        return indx;
    }

    public int[] getPivot() {
        return pivot;
    }

    public DenseMatrix64F getLower( DenseMatrix64F lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        if( lower == null ) {
            lower = new DenseMatrix64F(numRows,numCols);
        } else {
            CommonOps.fill(lower, 0);
        }

        for( int i = 0; i < numCols; i++ ) {
            lower.set(i,i,1.0);

            for( int j = 0; j < i; j++ ) {
                lower.set(i,j, LU.get(i,j));
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    lower.set(i,j, LU.get(i,j));
                }
            }
        }
        return lower;
    }


    public DenseMatrix64F getUpper( DenseMatrix64F upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        if( upper == null ) {
            upper = new DenseMatrix64F(numRows, numCols);
        } else {
            CommonOps.fill(upper, 0);
        }

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                upper.set(i,j, LU.get(i,j));
            }
        }

        return upper;
    }



    protected void decomposeCommonInit(DenseMatrix64F a) {
        if( a.numRows > maxWidth || a.numCols > maxWidth ) {
            setExpectedMaxSize(a.numRows,a.numCols);
        }

        m = a.numRows;
        n = a.numCols;

        LU.setMatrix(a);
        for (int i = 0; i < m; i++) {
            pivot[i] = i;
        }
        pivsign = 1;
    }





    public void _solveVectorInternal( double []vv )
    {
        // Solve L*Y = B
        int ii = 0;

        for( int i = 0; i < n; i++ ) {
            int ip = indx[i];
            double sum = vv[ip];
            vv[ip] = vv[i];
            if( ii != 0 ) {
//                for( int j = ii-1; j < i; j++ )
//                    sum -= dataLU[i* n +j]*vv[j];
                int index = i*n + ii-1;
                for( int j = ii-1; j < i; j++ )
                    sum -= dataLU[index++]*vv[j];
            } else if( sum != 0.0 ) {
                ii=i+1;
            }
            vv[i] = sum;
        }

        // Solve U*X = Y;
        TriangularSolver.solveU2arr(dataLU, vv, n);
    }

    public double[] _getVV() {
        return vv;
    }

    public double computeDeterminant() {
        double ret = pivsign;

        int total = m*n;
        for( int i = 0; i < total; i += n + 1 ) {
            ret *= dataLU[i];
        }
        return ret;
    }


    public boolean decompose( DenseMatrix64F a )
    {
        decomposeCommonInit(a);

        double LUcolj[] = vv;

        for( int j = 0; j < n; j++ ) {

            // make a copy of the column to avoid cache jumping issues
            for( int i = 0; i < m; i++) {
                LUcolj[i] = dataLU[i*n + j];
            }

            // Apply previous transformations.
            for( int i = 0; i < m; i++ ) {
                int rowIndex = i*n;

                // Most of the time is spent in the following dot product.
                int kmax = i < j ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += dataLU[rowIndex+k]*LUcolj[k];
                }

                dataLU[rowIndex+j] = LUcolj[i] -= s;
            }

            // Find pivot and exchange if necessary.
            int p = j;
            double max = Math.abs(LUcolj[p]);
            for (int i = j+1; i < m; i++) {
                double v = Math.abs(LUcolj[i]);
                if ( v > max) {
                    p = i;
                    max = v;
                }
            }

            if (p != j) {
                // swap the rows
//                for (int k = 0; k < n; k++) {
//                    double t = dataLU[p*n + k];
//                    dataLU[p*n + k] = dataLU[j*n + k];
//                    dataLU[j*n + k] = t;
//                }
                int rowP = p*n;
                int rowJ = j*n;
                int endP = rowP+n;
                for (;rowP < endP; rowP++,rowJ++) {
                    double t = dataLU[rowP];
                    dataLU[rowP] = dataLU[rowJ];
                    dataLU[rowJ] = t;
                }
                int k = pivot[p]; pivot[p] = pivot[j]; pivot[j] = k;
                pivsign = -pivsign;
            }
            indx[j] = p;

            // Compute multipliers.
            if (j < m ) {
                double lujj = dataLU[j*n+j];
                if( lujj != 0 ) {
                    for (int i = j+1; i < m; i++) {
                        dataLU[i*n+j] /= lujj;
                    }
                }
            }
        }

        return true;
    }
}
