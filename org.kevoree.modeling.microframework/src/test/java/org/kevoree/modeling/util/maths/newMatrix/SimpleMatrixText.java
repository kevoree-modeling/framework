package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.TransposeAlgs;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

import java.util.Random;


/**
 * Created by assaad on 27/08/15.
 */
/** @ignore ts */
public class SimpleMatrixText {
    @Test
    public void transposeTest() {
        NativeArray2D matA= new NativeArray2D(3,5);

        //test normal transpose
        int k=1;
        for(int i=0;i<matA.nbRows();i++){
            for(int j=0;j<matA.nbColumns();j++){
                matA.set(i,j,k);
                k++;
            }
        }

        KArray2D matB= SimpleMatrix.transpose(matA);

        Assert.assertTrue(matA.nbColumns()==matB.nbRows());
        Assert.assertTrue(matA.nbRows()==matB.nbColumns());

        for(int i=0;i<matA.nbRows();i++) {
            for (int j = 0; j < matA.nbColumns(); j++) {
                Assert.assertTrue(matA.get(i,j)==matB.get(j,i));
            }
        }

        //test square transpose

        DenseMatrix64F test = new DenseMatrix64F(5,5);


        matA= new NativeArray2D(5,5);

        k=1;
        for(int i=0;i<matA.nbRows();i++){
            for(int j=0;j<matA.nbColumns();j++){
                matA.set(i,j,k);
                test.set(i,j,k);
                k++;
            }
        }

        TransposeAlgs.square(test);

        matB= SimpleMatrix.transpose(matA);
        Assert.assertTrue(matA.nbColumns()==matB.nbRows());
        Assert.assertTrue(matA.nbRows()==matB.nbColumns());

        for(int i=0;i<matA.nbRows();i++) {
            for (int j = 0; j < matA.nbColumns(); j++) {
                Assert.assertTrue(matA.get(i,j)==matB.get(j,i));
            }
        }

        //test BIG transpose
        matA= new NativeArray2D(380,381);

        k=1;
        for(int i=0;i<matA.nbRows();i++){
            for(int j=0;j<matA.nbColumns();j++){
                matA.set(i,j,k);
                k++;
            }
        }

        matB= SimpleMatrix.transpose(matA);
        Assert.assertTrue(matA.nbColumns()==matB.nbRows());
        Assert.assertTrue(matA.nbRows()==matB.nbColumns());

        for(int i=0;i<matA.nbRows();i++) {
            for (int j = 0; j < matA.nbColumns(); j++) {
                Assert.assertTrue(matA.get(i,j)==matB.get(j,i));
            }
        }
    }


    private void traditional(KArray2D matA, KArray2D matB, KArray2D matC){
        for (int i=0;i<matC.nbRows();i++){
            for(int j=0;j<matC.nbColumns();j++){
                for(int k=0;k<matA.nbColumns();k++){
                    matC.add(i,j,matA.get(i,k)*matB.get(k,j));
                }
            }
        }
    }

    @Test
    public void multiplyTest() {

        int[] dimA={30,100};
        int[] dimB={100,50};

        NativeArray2D matA= new NativeArray2D(dimA[0],dimA[1]);

        Random rand=new Random();
        for(int i=0;i<matA.nbRows();i++){
            for(int j=0;j<matA.nbColumns();j++){
                matA.set(i,j,rand.nextDouble());
            }
        }

        NativeArray2D matB= new NativeArray2D(dimB[0],dimB[1]);

        for(int i=0;i<matB.nbRows();i++){
            for(int j=0;j<matB.nbColumns();j++){
                matB.set(i,j,rand.nextDouble());
            }
        }

        NativeArray2D matC= new NativeArray2D(matA.nbRows(),matB.nbColumns());


        traditional(matA, matB,matC);
        KArray2D matRes = SimpleMatrix.multiply(matA, matB);
        

        Assert.assertTrue(matRes.nbRows()==matC.nbRows());
        Assert.assertTrue(matRes.nbColumns()==matC.nbColumns());

        for(int i=0;i<3;i++) {
            for (int j = 0; j < 2; j++) {
                Assert.assertTrue(matRes.get(i,j)==matC.get(i,j));
            }
        }



    }
}
