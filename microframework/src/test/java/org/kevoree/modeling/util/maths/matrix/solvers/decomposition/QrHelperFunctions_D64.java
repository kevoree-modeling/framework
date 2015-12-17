package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.structure.KArray2D;



public class QrHelperFunctions_D64 {

    public static double findMax( double[] u, int startU , int length ) {
        double max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            double val = u[index];
            val = (val < 0.0D) ? -val : val;
            if( val > max )
                max = val;
        }

        return max;
    }

    public static double findMaxArray(KArray2D u, int col, int startU , int length ) {
        double max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            double val = u.get(index,col);
            val = (val < 0.0D) ? -val : val;
            if( val > max )
                max = val;
        }

        return max;
    }

    public static void divideElements4arg(final int j, final int numRows ,
                                      final double[] u, final double u_0 ) {
//        double div_u = 1.0/u_0;
//
//        if( Double.isInfinite(div_u)) {
        for( int i = j; i < numRows; i++ ) {
            u[i] /= u_0;
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                u[i] *= div_u;
//            }
//        }
    }

    public static void divideElements4argArray(final int j, final int numRows ,
                                          KArray2D u, int col, final double u_0 ) {
//        double div_u = 1.0/u_0;
//
//        if( Double.isInfinite(div_u)) {
        for( int i = j; i < numRows; i++ ) {
            u.set(i,col,u.get(i,col)/ u_0);
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                u[i] *= div_u;
//            }
//        }
    }

    public static void divideElements(int j, int numRows , double[] u, int startU , double u_0 ) {
//        double div_u = 1.0/u_0;
//
//        if( Double.isInfinite(div_u)) {
        for( int i = j; i < numRows; i++ ) {
            u[i+startU] /= u_0;
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                u[i+startU] *= div_u;
//            }
//        }
    }

    public static void divideElements_Brow(int j, int numRows , double[] u,
                                           double b[] , int startB ,
                                           double u_0 ) {
//        double div_u = 1.0/u_0;
//
//        if( Double.isInfinite(div_u)) {
        for( int i = j; i < numRows; i++ ) {
            u[i] = b[i+startB] /= u_0;
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                u[i] = b[i+startB] *= div_u;
//            }
//        }
    }

    public static void divideElements_Bcol(int j, int numRows , int numCols ,
                                           double[] u,
                                           double b[] , int startB ,
                                           double u_0 ) {
//        double div_u = 1.0/u_0;
//
//        if( Double.isInfinite(div_u)) {
        int indexB = j*numCols+startB;
        for( int i = j; i < numRows; i++) {
            b[indexB] = u[i] /= u_0;
            indexB += numCols;
        }
//        } else {
//            int indexB = j*getNumCols+startB;
//            for( int i = j; i < getNumRows; i++ , indexB += getNumCols ) {
//                b[indexB] = u[i] *= div_u;
//            }
//        }
    }

    public static double computeTauAndDivide(int j, int numRows , double[] u, int startU , double max) {
        // compute the norm2 of the matrix, with each element
        // normalized by the max value to avoid overflow problems
        double tau = 0;
//        double div_max = 1.0/max;
//        if( Double.isInfinite(div_max)) {
        // more accurate
        for( int i = j; i < numRows; i++ ) {
            double d = u[startU+i] /= max;
            tau += d*d;
        }
//        } else {
//            // faster
//            for( int i = j; i < getNumRows; i++ ) {
//                double d = u[startU+i] *= div_max;
//                tau += d*d;
//            }
//        }
        tau = Math.sqrt(tau);

        if( u[startU+j] < 0 )
            tau = -tau;

        return tau;
    }

    public static double computeTauAndDivide4arg(final int j, final int numRows ,
                                                 final double[] u , final double max) {
        double tau = 0;
//        double div_max = 1.0/max;
//        if( Double.isInfinite(div_max)) {
        for( int i = j; i < numRows; i++ ) {
            double d = u[i] /= max;
            tau += d*d;
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                double d = u[i] *= div_max;
//                tau += d*d;
//            }
//        }
        tau = Math.sqrt(tau);

        if( u[j] < 0 )
            tau = -tau;

        return tau;
    }


    public static double computeTauAndDivide4argArray(final int j, final int numRows ,
                                            KArray2D u , int col, final double max) {
        double tau = 0;
//        double div_max = 1.0/max;
//        if( Double.isInfinite(div_max)) {
        for( int i = j; i < numRows; i++ ) {
            u.set(i,col,u.get(i,col)/max);
            double d = u.get(i,col);
            tau += d*d;
        }
//        } else {
//            for( int i = j; i < getNumRows; i++ ) {
//                double d = u[i] *= div_max;
//                tau += d*d;
//            }
//        }
        tau = Math.sqrt(tau);

        if( u.get(j,col) < 0 )
            tau = -tau;

        return tau;
    }

    public static void rank1UpdateMultR( DenseMatrix64F A , double u[] , double gamma ,
                                         int colA0,
                                         int w0, int w1 ,
                                         double _temp[] )
    {
//        for( int i = colA0; i < A.getNumCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k]*A.data[k*A.getNumCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0]*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            double valU = u[k];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[indexA++];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u[i];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[indexA++] -= valU*_temp[j];
            }
        }
    }

    public static void rank1UpdateMultRArray( DenseMatrix64F A , KArray2D u , int col, double gamma ,
                                         int colA0,
                                         int w0, int w1 ,
                                         double _temp[] )
    {
//        for( int i = colA0; i < A.getNumCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k]*A.data[k*A.getNumCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u.get(w0,col)*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            double valU = u.get(k,col);
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[indexA++];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u.get(i,col);

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[indexA++] -= valU*_temp[j];
            }
        }
    }

    public static void rank1UpdateMultR8param(DenseMatrix64F A,
                                        double u[], int offsetU,
                                        double gamma,
                                        int colA0,
                                        int w0, int w1,
                                        double _temp[])
    {
//        for( int i = colA0; i < A.getNumCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k+offsetU]*A.data[k*A.getNumCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0+offsetU]*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            double valU = u[k+offsetU];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[indexA++];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u[i+offsetU];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[indexA++] -= valU*_temp[j];
            }
        }
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by w with the multiply on the left.<br>
     * <br>
     * A = A(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultL( DenseMatrix64F A , double u[] ,
                                         double gamma ,
                                         int colA0,
                                         int w0 , int w1 )
    {
        for( int i = colA0; i < A.numRows; i++ ) {
            int startIndex = i*A.numCols+w0;
            double sum = 0;
            int rowIndex = startIndex;
            for( int j = w0; j < w1; j++ ) {
                sum += A.data[rowIndex++]*u[j];
            }
            sum = -gamma*sum;

            rowIndex = startIndex;
            for( int j = w0; j < w1; j++ ) {
                A.data[rowIndex++] += sum*u[j];
            }
        }
    }
}
