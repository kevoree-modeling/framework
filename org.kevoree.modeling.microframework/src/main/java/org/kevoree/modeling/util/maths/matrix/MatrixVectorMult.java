package org.kevoree.modeling.util.maths.matrix;

public class MatrixVectorMult {


    public static void mult( DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C)
    {
        if( A.numCols == 0 ) {
            CommonOps.fill(C,0);
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        double b0 = B.getValueAtIndex(0);
        for( int i = 0; i < A.numRows; i++ ) {
            double total = A.getValueAtIndex(indexA++) * b0;

            for( int j = 1; j < A.numCols; j++ ) {
                total += A.getValueAtIndex(indexA++) * B.getValueAtIndex(j);
            }

            C.setValueAtIndex(cIndex++, total);
        }
    }


    /*
    public static void multAdd( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {

        if( A.numCols == 0 ) {
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        for( int i = 0; i < A.numRows; i++ ) {
            double total = A.getValueAtIndex(indexA++) * B.getValueAtIndex(0);

            for( int j = 1; j < A.numCols; j++ ) {
                total += A.getValueAtIndex(indexA++) * B.getValueAtIndex(j);
            }

            C.plus(cIndex++ , total );
        }
    }*/


    public static void multTransA_small( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {

        int cIndex = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            double total = 0;

            int indexA = i;
            for( int j = 0; j < A.numRows; j++ ) {
                total += A.getValueAtIndex(indexA) * B.getValueAtIndex(j);
                indexA += A.numCols;
            }

            C.setValueAtIndex(cIndex++ , total);
        }
    }


    public static void multTransA_reorder( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {

        if( A.numRows == 0 ) {
            CommonOps.fill(C,0);
            return;
        }

        double B_val = B.getValueAtIndex(0);
        for( int i = 0; i < A.numCols; i++ ) {
            C.setValueAtIndex(i, A.getValueAtIndex(i) * B_val );
        }

        int indexA = A.numCols;
        for( int i = 1; i < A.numRows; i++ ) {
            B_val = B.getValueAtIndex(i);
            for( int j = 0; j < A.numCols; j++ ) {
                C.plus(  j , A.getValueAtIndex(indexA++) * B_val );
            }
        }
    }

/*
    public static void multAddTransA_small( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {
        int cIndex = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            double total = 0;

            int indexA = i;
            for( int j = 0; j < A.numRows; j++ ) {
                total += A.getValueAtIndex(indexA) * B.getValueAtIndex(j);
                indexA += A.numCols;
            }

            C.plus( cIndex++ , total );
        }
    }


    public static void multAddTransA_reorder( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {

        if( A.numRows == 0 ) {
            return;
        }

        int indexA = 0;
        for( int j = 0; j < A.numRows; j++ ) {
            double B_val = B.getValueAtIndex(j);
            for( int i = 0; i < A.numCols; i++ ) {
                C.plus( i , A.getValueAtIndex(indexA++) * B_val );
            }
        }
    }*/


}
