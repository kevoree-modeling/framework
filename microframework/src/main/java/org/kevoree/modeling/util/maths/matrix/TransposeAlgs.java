package org.kevoree.modeling.util.maths.matrix;

public class TransposeAlgs {

    public static void square( DenseMatrix64F mat )
    {
        int index = 1;
        int indexEnd = mat.numCols;
        for( int i = 0; i < mat.numRows; i++ ) {
            int indexOther = (i+1)*mat.numCols + i;
            for( ; index < indexEnd; index++) {
                double val = mat.data[ index ];
                mat.data[ index ] = mat.data[ indexOther ];
                mat.data[indexOther] = val;
                indexOther += mat.numCols;
            }
            index += i+2;
            indexEnd += mat.numCols;
        }
    }

    public static void block( DenseMatrix64F A , DenseMatrix64F A_tran ,
                              final int blockLength )
    {
        for( int i = 0; i < A.numRows; i += blockLength ) {
            int blockHeight = Math.min( blockLength , A.numRows - i);

            int indexSrc = i*A.numCols;
            int indexDst = i;

            for( int j = 0; j < A.numCols; j += blockLength ) {
                int blockWidth = Math.min( blockLength , A.numCols - j);
                int indexSrcEnd = indexSrc + blockWidth;
                for( ; indexSrc < indexSrcEnd;  indexSrc++ ) {
                    int rowSrc = indexSrc;
                    int rowDst = indexDst;
                    int end = rowDst + blockHeight;
                    for( ; rowDst < end; rowSrc += A.numCols ) {
                        A_tran.data[ rowDst++ ] = A.data[ rowSrc ];
                    }
                    indexDst += A_tran.numCols;
                }
            }
        }
    }

    public static void standard( DenseMatrix64F A, DenseMatrix64F A_tran)
    {
        int index = 0;
        for( int i = 0; i < A_tran.numRows; i++ ) {
            int index2 = i;

            int end = index + A_tran.numCols;
            while( index < end ) {
                A_tran.data[index++ ] = A.data[ index2 ];
                index2 += A.numCols;
            }
        }
    }
}
