package org.kevoree.modeling.util.maths.matrix;

public class MatrixMultProduct {

    public static void outer(DenseMatrix64F a, DenseMatrix64F c) {
        for( int i = 0; i < a.numRows; i++ ) {
            int indexC1 = i*c.numCols+i;
            int indexC2 = indexC1;
            for( int j = i; j < a.numRows; j++ , indexC2 += c.numCols) {
                int indexA = i*a.numCols;
                int indexB = j*a.numCols;
                double sum = 0;
                int end = indexA + a.numCols;
                for( ; indexA < end; indexA++,indexB++ ) {
                    sum += a.data[indexA]*a.data[indexB];
                }
                c.data[indexC2] = c.data[indexC1++] = sum;
            }
        }
    }

    public static void inner_small(DenseMatrix64F a, DenseMatrix64F c) {

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = i; j < a.numCols; j++ ) {
                int indexC1 = i*c.numCols+j;
                int indexC2 = j*c.numCols+i;
                int indexA = i;
                int indexB = j;
                double sum = 0;
                int end = indexA + a.numRows*a.numCols;
                for( ; indexA < end; indexA += a.numCols, indexB += a.numCols ) {
                    sum += a.data[indexA]*a.data[indexB];
                }
                c.data[indexC1] = c.data[indexC2] = sum;
            }
        }
    }

    public static void inner_reorder(DenseMatrix64F a, DenseMatrix64F c) {

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC = i*c.numCols+i;
            double valAi = a.data[i];
            for( int j = i; j < a.numCols; j++ ) {
                c.data[indexC++] =  valAi*a.data[j];
            }

            for( int k = 1; k < a.numRows; k++ ) {
                indexC = i*c.numCols+i;
                int indexB = k*a.numCols+i;
                valAi = a.data[indexB];
                for( int j = i; j < a.numCols; j++ ) {
                    c.data[indexC++] +=  valAi*a.data[indexB++];
                }
            }

            indexC = i*c.numCols+i;
            int indexC2 = indexC;
            for( int j = i; j < a.numCols; j++ , indexC2 += c.numCols) {
                c.data[indexC2] = c.data[indexC++];
            }
        }

    }



    public static void inner_reorder_upper(DenseMatrix64F a, DenseMatrix64F c) {
        for( int i = 0; i < a.numCols; i++ ) {
            int indexC = i*c.numCols+i;
            double valAi = a.data[i];
            for( int j = i; j < a.numCols; j++ ) {
                c.data[indexC++] =  valAi*a.data[j];
            }

            for( int k = 1; k < a.numRows; k++ ) {
                indexC = i*c.numCols+i;
                int indexB = k*a.numCols+i;
                valAi = a.data[indexB];
                for( int j = i; j < a.numCols; j++ ) {
                    c.data[indexC++] +=  valAi*a.data[indexB++];
                }
            }
        }
    }
}
