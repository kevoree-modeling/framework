package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;


/**
 * Created by assaad on 27/08/15.
 */
public class SimpleMatrixText {
    @Test
    public void transposeTest() {
        NativeArray2D matA= new NativeArray2D(3,5);

        int k=1;
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                matA.set(i,j,k);
                k++;
            }
        }

        KArray2D matB= SimpleMatrix.transpose(matA);

        Assert.assertTrue(matA.nbColumns()==matB.nbRows());
        Assert.assertTrue(matA.nbRows()==matB.nbColumns());

        for(int i=0;i<3;i++) {
            for (int j = 0; j < 5; j++) {
                Assert.assertTrue(matA.get(i,j)==matB.get(j,i));
            }
        }
    }

    @Test
    public void multiplyTest() {
        NativeArray2D matA= new NativeArray2D(3,5);

        int k=1;
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                matA.set(i,j,k);
                k++;
            }
        }

        NativeArray2D matB= new NativeArray2D(5,2);

        k=1;
        for(int i=0;i<5;i++){
            for(int j=0;j<2;j++){
                matB.set(i,j,k);
                k++;
            }
        }

        NativeArray2D matC= new NativeArray2D(3,2);
        matC.set(0,0,5*19);
        matC.set(0,1,5*22);
        matC.set(1,0,5*44);
        matC.set(1,1,5*52);
        matC.set(2,0,5*69);
        matC.set(2,1,5*82);


        KArray2D matRes = SimpleMatrix.multiply(matA,matB);

        Assert.assertTrue(matRes.nbRows()==matC.nbRows());
        Assert.assertTrue(matRes.nbColumns()==matC.nbColumns());

        for(int i=0;i<3;i++) {
            for (int j = 0; j < 2; j++) {
                Assert.assertTrue(matRes.get(i,j)==matC.get(i,j));
            }
        }



    }
}
