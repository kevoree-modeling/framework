package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
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
        int r=20;
        int[] dimA = {r, r};
        boolean rand=true;
        double eps=1e-5;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        JavaBlas java = new JavaBlas();

        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        MatrixOperations.copyMatrix(matA, ejmlmatA);

        // long timestart,timeend;

        //   timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,java);
        //   timeend=System.currentTimeMillis();
        //   System.out.println("java blas invert " + ((double) (timeend - timestart)) / 1000);

        //   timestart=System.currentTimeMillis();
        SimpleMatrix resEjml= ejmlmatA.invert();
        //   timeend=System.currentTimeMillis();
        //   System.out.println("java ejml invert " + ((double) (timeend - timestart)) / 1000);

        boolean test=true;
        assert res != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                if(Math.abs(resEjml.getValue2D(i, j)-res.get(i, j))> eps){
                    test =false;
                }
            }
        }
       // Assert.assertTrue(test);
    }
}
