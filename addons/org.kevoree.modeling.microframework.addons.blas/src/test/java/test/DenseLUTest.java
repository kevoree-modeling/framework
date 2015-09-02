package test;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.blas.NetlibBlas;
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
    @Test
    public void testLUFactorize(){
        int r=2000;
        int[] dimA = {r, r};

        KBlas java = new JavaBlas();
        KBlas netlib = new NetlibBlas();

        boolean rand=true;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);

        DenseMatrix64F ejmlmatA = new DenseMatrix64F(dimA[0],dimA[1]);
        MatrixOperations.copyMatrixDense(matA, ejmlmatA);



        DenseLU dlu = new DenseLU(dimA[0],dimA[1]);

        System.out.println("Init done");

        long timestart,timeend;

        timestart=System.currentTimeMillis();
        dlu.factor(matA, netlib);
        KArray2D res= dlu.getLU();
        timeend=System.currentTimeMillis();
        System.out.println("Netlib Factorizarion " + ((double) (timeend - timestart)) / 1000+" s");


        LUDecompositionAlt_D64 ludec = new LUDecompositionAlt_D64();
        timestart=System.currentTimeMillis();
        ludec.decompose(ejmlmatA);
        DenseMatrix64F luejml = ludec.getLU();
        timeend=System.currentTimeMillis();
        System.out.println("EJML Factorizarion " + ((double) (timeend - timestart)) / 1000+" s");


        System.out.println("done");
    }

    @Test
    public void invertMatrix(){
        int r=2000;
        int[] dimA = {r, r};
        boolean rand=true;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        JavaBlas java = new JavaBlas();
        KBlas netlib = new NetlibBlas();

        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        MatrixOperations.copyMatrix(matA, ejmlmatA);

        long timestart,timeend;
        System.out.println("Init done");

        timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,netlib);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart=System.currentTimeMillis();
        SimpleMatrix resEjml= ejmlmatA.invert();
        timeend=System.currentTimeMillis();
        System.out.println("Ejml invert " + ((double) (timeend - timestart)) / 1000+" s");

        assert res != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertEquals(resEjml.getValue2D(i, j), res.get(i, j), eps);
            }
        }



    }
}
