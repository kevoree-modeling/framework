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

        for(int i=0; i<matA.nbRows();i++){
            for(int j=0;j<matA.nbColumns();j++){
                result.set(j,i,matA.get(i,j));
            }
        }

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


}
