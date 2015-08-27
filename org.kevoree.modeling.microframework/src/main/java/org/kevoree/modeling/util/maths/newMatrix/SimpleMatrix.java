package org.kevoree.modeling.util.maths.newMatrix;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

/**
 * Created by assaad on 27/08/15.
 */
public class SimpleMatrix {
    public static int BLOCK_WIDTH = 60;
    public static int TRANSPOSE_SWITCH = 375;
    public static int MULT_COLUMN_SWITCH = 15;
    
    
    public static KArray2D transpose (KArray2D matA){
        NativeArray2D result= new NativeArray2D(matA.nbColumns(),matA.nbRows());

        if(matA.nbColumns()==matA.nbRows()){
            transposeSquare(matA, result);
        }
        else if( matA.nbColumns() > TRANSPOSE_SWITCH && matA.nbRows() > TRANSPOSE_SWITCH )
            transposeBlock(matA, result);
        else
            transposeStandard(matA, result);
        
        return result;
    }

    public static KArray2D multiply (KArray2D matA, KArray2D matB){

        NativeArray2D matC= new NativeArray2D(matA.nbRows(),matB.nbColumns());

        if( matB.nbColumns() == 1 ) {
            matrixVectorMult(matA, matB, matC);
        } else if( matB.nbColumns() >= MULT_COLUMN_SWITCH ) {
            mult_reorder(matA, matB, matC);
        } else {
            mult_small(matA, matB, matC);
        }

        return matC;
    }

    private static void transposeSquare(KArray2D matA, NativeArray2D result) {
        int index = 1;
        int indexEnd = matA.nbColumns();
        for( int i = 0; i < matA.nbRows();
             i++ , index += i+1 , indexEnd += matA.nbColumns() ) {
            int indexOther = (i+1)*matA.nbColumns() + i;
            int n=i*(matA.nbColumns()+1);
            result.setAtIndex(n,matA.getAtIndex(n));
            for( ; index < indexEnd; index++, indexOther += matA.nbColumns()) {
                result.setAtIndex(index,matA.getAtIndex(indexOther));
                result.setAtIndex(indexOther,matA.getAtIndex(index));
            }
        }
    }

    private static void transposeStandard(KArray2D matA, KArray2D result) {
        int index = 0;
        for( int i = 0; i < result.nbRows(); i++ ) {
            int index2 = i;

            int end = index + result.nbColumns();
            while( index < end ) {
                result.setAtIndex(index++,matA.getAtIndex(index2));
                index2 += matA.nbColumns();
            }
        }
    }

    private static void transposeBlock(KArray2D matA, KArray2D result) {
        for( int i = 0; i < matA.nbRows(); i += BLOCK_WIDTH ) {
            int blockHeight = Math.min( BLOCK_WIDTH , matA.nbRows() - i);

            int indexSrc = i*matA.nbColumns();
            int indexDst = i;

            for( int j = 0; j < matA.nbColumns(); j += BLOCK_WIDTH ) {
                int blockWidth = Math.min( BLOCK_WIDTH , matA.nbColumns() - j);
                int indexSrcEnd = indexSrc + blockWidth;
                for( ; indexSrc < indexSrcEnd;  indexSrc++ ) {
                    int rowSrc = indexSrc;
                    int rowDst = indexDst;
                    int end = rowDst + blockHeight;
                    for( ; rowDst < end; rowSrc += matA.nbColumns() ) {
                        result.setAtIndex(rowDst++, matA.getAtIndex(rowSrc));
                    }
                    indexDst += result.nbColumns();
                }
            }
        }
    }



    private static void mult_small(KArray2D matA, KArray2D matB, KArray2D matC) {
        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < matA.nbRows(); i++ ) {
            for( int j = 0; j < matB.nbColumns(); j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + matB.nbRows();
                while( indexA < end ) {
                    total += matA.getAtIndex(indexA++) * matB.getAtIndex(indexB);
                    indexB += matB.nbColumns();
                }

                matC.setAtIndex(cIndex++, total);
            }
            aIndexStart += matA.nbColumns();
        }
    }

    private static void mult_reorder(KArray2D matA, KArray2D matB, KArray2D matC) {
        if (matA.nbColumns() == 0 || matA.nbRows() == 0 ) {
            matC.setAll(0);
            return;
        }
        double valA;
        int indexCbase= 0;
        int endOfKLoop = matB.nbRows()*matB.nbColumns();

        for( int i = 0; i < matA.nbRows(); i++ ) {
            int indexA = i*matA.nbColumns();

            // need to assign matC.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + matB.nbColumns();

            valA = matA.getAtIndex(indexA++);

            while( indexB < end ) {
                matC.setAtIndex(indexC++, valA * matB.getAtIndex(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + matB.nbColumns();

                valA = matA.getAtIndex(indexA++);

                while( indexB < end ) { // j loop
                    matC.addAtIndex(indexC++ , valA * matB.getAtIndex(indexB++));
                }
            }
            indexCbase += matC.nbColumns();
        }
    }

    private static void matrixVectorMult(KArray2D matA, KArray2D matB, KArray2D matC) {
        if( matA.nbColumns() == 0 ) {
            matC.setAll(0);
            return;
        }
        int indexA = 0;
        int cIndex = 0;
        double b0 = matB.getAtIndex(0);
        for( int i = 0; i < matA.nbRows(); i++ ) {
            double total = matA.getAtIndex(indexA++) * b0;
            for( int j = 1; j < matA.nbColumns(); j++ ) {
                total += matA.getAtIndex(indexA++) * matB.getAtIndex(j);
            }
            matC.setAtIndex(cIndex++, total);
        }
    }

    public static KArray2D identity(int width ) {
        KArray2D ret = new NativeArray2D(width,width);
        ret.setAll(0);
        for(int i=0;i<width;i++){
            ret.set(i,i,1);
        }
        return ret;
    }

}
