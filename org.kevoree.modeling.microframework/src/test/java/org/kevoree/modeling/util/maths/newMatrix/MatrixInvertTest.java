package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

/**
 * Created by assaad on 03/09/15.
 */
public class MatrixInvertTest {
    @Test
    public void invertMatrix(){
        invert(5);
        invert(100);


    }

    public void invert(int r){
        int[] dimA = {r, r};
        boolean rand=true;
        double eps=1e-5;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        matA.set(0,0,5);

        JavaBlas java = new JavaBlas();
        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        CommonOps.copyMatrix(matA, ejmlmatA);

        // long timestart,timeend;

        //   timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,java);
        //   timeend=System.currentTimeMillis();
        //   System.out.println("java blas invert " + ((double) (timeend - timestart)) / 1000);

        //   timestart=System.currentTimeMillis();
        SimpleMatrix resEjml= ejmlmatA.invert();
        //   timeend=System.currentTimeMillis();
        //   System.out.println("java ejml invert " + ((double) (timeend - timestart)) / 1000);


      /*  System.out.println("java blas:");
        for(int i=0;i<res.rows();i++){
            for (int j = 0; j < res.columns(); j++) {
                System.out.print(res.get(i,j)+" ");
            }
            System.out.println();
        }

        System.out.println();

        System.out.println("EJML:");
        for(int i=0;i<res.rows();i++){
            for (int j = 0; j < res.columns(); j++) {
                System.out.print(resEjml.getValue2D(i, j)+" ");
            }
            System.out.println();
        }
        System.out.println();*/

        boolean test=true;
        int count=0;
        assert res != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                if(Math.abs(resEjml.getValue2D(i, j)-res.get(i, j))> eps){
                    test =false;
                    count++;
                }

            }
        }
       // System.out.println("Error counts: "+count);
        Assert.assertTrue(test);
    }
}
