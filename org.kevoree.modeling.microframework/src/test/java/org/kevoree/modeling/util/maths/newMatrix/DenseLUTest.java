package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.LUDecompositionAlt_D64;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.DenseLU;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

/**
 * Created by assaad on 02/09/15.
 */
public class DenseLUTest {
    //@Test
    public void testLUFactorize(){
        int r=2;
        int[] dimA = {r, r};

        KBlas java = new JavaBlas();

        NativeArray2D A=new NativeArray2D(dimA[0],dimA[1]);
        A.set(0, 0, 3);
        A.set(0, 1, 1);
        A.set(1, 0, -6);
        A.set(1, 1, -4);

        DenseLU dlu = new DenseLU(dimA[0],dimA[1]);
        dlu.factor(A, java);

        KArray2D res= dlu.getLU();

        res=dlu.getLower();
        res=dlu.getUpper();
        System.out.println("done");

        DenseMatrix64F ej=new DenseMatrix64F(dimA[0],dimA[1]);
        ej.set(0,0,3);
        ej.set(0,1,1);
        ej.set(1, 0, -6);
        ej.set(1, 1, -4);

        LUDecompositionAlt_D64 ludec = new LUDecompositionAlt_D64();
        ludec.decompose(ej);
        DenseMatrix64F luejml = ludec.getLU();

        System.out.println("done");
    }

    @Test
    public void invertMatrix(){
        int r=3;
        int[] dimA = {r, r};
        boolean rand=true;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        JavaBlas java = new JavaBlas();

        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        MatrixOperations.copyMatrix(matA, ejmlmatA);

        long timestart,timeend;

        timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,java);
        timeend=System.currentTimeMillis();
        System.out.println("java blas invert " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        SimpleMatrix resEjml= ejmlmatA.invert();
        timeend=System.currentTimeMillis();
        System.out.println("java ejml invert " + ((double) (timeend - timestart)) / 1000);

        assert res != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertEquals(resEjml.getValue2D(i, j), res.get(i, j), eps);
            }
        }



    }
}
