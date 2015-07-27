package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class QRxColPivDecompositionHouseholderColumn_D64
        extends QRDecompositionHouseholderColumn_D64

{
    // the ordering of each column, the current column i is the original column pivots[i]
    protected int pivots[];
    // F-norm  squared for each column
    protected double normsCol[];

    // value of the maximum abs element
    protected double maxAbs;


    protected double singularThreshold = CommonOps.EPS;

    // the matrix's rank
    protected int rank;


  /*  public QRColPivDecompositionHouseholderColumn_D64(double singularThreshold) {
        this.singularThreshold = singularThreshold;
    }*/

    public QRxColPivDecompositionHouseholderColumn_D64() {
        super();
    }


    public void setSingularThreshold( double threshold ) {
        this.singularThreshold = threshold;
    }


    public void setExpectedMaxSize( int numRows , int numCols ) {
        super.setExpectedMaxSize(numRows,numCols);

        if( pivots == null || pivots.length < numCols  ) {
            pivots = new int[numCols];
            normsCol = new double[numCols];
        }
    }


    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CommonOps.identity(numRows, minLength);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new RuntimeException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = CommonOps.identity1D(numRows);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new RuntimeException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        }

        for( int j = rank-1; j >= 0; j-- ) {
            double u[] = dataQR[j];

            double vv = u[j];
            u[j] = 1;
            QrHelperFunctions_D64.rank1UpdateMultR(Q, u, gammas[j], j, j, numRows, v);
            u[j] = vv;
        }

        return Q;
    }


    @Override
    public boolean decompose( DenseMatrix64F A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        convertToColumnMajor(A);

        maxAbs = CommonOps.elementMaxAbs(A);
        // initialize pivot variables
        setupPivotInfo();

        // go through each column and perform the decomposition
        for( int j = 0; j < minLength; j++ ) {
            if( j > 0 )
                updateNorms(j);
            swapColumns(j);
            // if its degenerate stop processing
            if( !householderPivot(j) )
                break;
            updateA(j);
            rank = j+1;
        }

        return true;
    }


    private void setupPivotInfo() {
        for( int col = 0; col < numCols; col++ ) {
            pivots[col] = col;
            double c[] = dataQR[col];
            double norm = 0;
            for( int row = 0; row < numRows; row++ ) {
                double element = c[row];
                norm += element*element;
            }
            normsCol[col] = norm;
        }
    }


    private void updateNorms( int j ) {
        boolean foundNegative = false;
        for( int col = j; col < numCols; col++ ) {
            double e = dataQR[col][j-1];
            normsCol[col] -= e*e;

            if( normsCol[col] < 0 ) {
                foundNegative = true;
                break;
            }
        }


        if( foundNegative ) {
            for( int col = j; col < numCols; col++ ) {
                double u[] = dataQR[col];
                double actual = 0;
                for( int i=j; i < numRows; i++ ) {
                    double v = u[i];
                    actual += v*v;
                }
                normsCol[col] = actual;
            }
        }
    }


    private void swapColumns( int j ) {

        // find the column with the largest norm
        int largestIndex = j;
        double largestNorm = normsCol[j];
        for( int col = j+1; col < numCols; col++ ) {
            double n = normsCol[col];
            if( n > largestNorm ) {
                largestNorm = n;
                largestIndex = col;
            }
        }
        // swap the columns
        double []tempC = dataQR[j];
        dataQR[j] = dataQR[largestIndex];
        dataQR[largestIndex] = tempC;
        double tempN = normsCol[j];
        normsCol[j] = normsCol[largestIndex];
        normsCol[largestIndex] = tempN;
        int tempP = pivots[j];
        pivots[j] = pivots[largestIndex];
        pivots[largestIndex] = tempP;
    }


    protected boolean householderPivot(int j)
    {
        final double u[] = dataQR[j];

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        final double max = QrHelperFunctions_D64.findMax(u, j, numRows - j);

        if( max <= 0 ) {
            return false;
        } else {
            // computes tau and normalizes u by max
            tau = QrHelperFunctions_D64.computeTauAndDivide4arg(j, numRows, u, max);

            // divide u by u_0
            double u_0 = u[j] + tau;
            QrHelperFunctions_D64.divideElements4arg(j + 1, numRows, u, u_0);

            gamma = u_0/tau;
            tau *= max;

            u[j] = -tau;

            if( Math.abs(tau) <= singularThreshold ) {
                return false;
            }
        }

        gammas[j] = gamma;

        return true;
    }


    public int getRank() {
        return rank;
    }


    public int[] getPivots() {
        return pivots;
    }

    public DenseMatrix64F getPivotMatrix(DenseMatrix64F P) {
        if( P == null )
            P = new DenseMatrix64F(numCols,numCols);
        else if( P.numRows != numCols )
            throw new RuntimeException("Number of rows must be "+numCols);
        else if( P.numCols != numCols )
            throw new RuntimeException("Number of columns must be "+numCols);
        else {
            P.zero();
        }

        for( int i = 0; i < numCols; i++ ) {
            P.set(pivots[i],i,1);
        }

        return P;
    }
}