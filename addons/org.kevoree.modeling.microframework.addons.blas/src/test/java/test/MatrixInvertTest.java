package test;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.blas.NetlibBlas;
import org.kevoree.modeling.blas.NetlibJavaBlas;
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
    public void invertMatrix() {
        int r = 3000;
        int[] dimA = {r, r};
        boolean rand = true;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        JavaBlas javablas = new JavaBlas();
        NetlibBlas nativeblas = new NetlibBlas();
        NetlibJavaBlas netlibJavaBlas = new NetlibJavaBlas();

        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        MatrixOperations.copyMatrix(matA, ejmlmatA);


        long timestart, timeend;

        timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,nativeblas);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib blas invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart = System.currentTimeMillis();
        KArray2D resJ = MatrixOperations.invert(matA, javablas);
        timeend = System.currentTimeMillis();
        System.out.println("Java Src blas invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart = System.currentTimeMillis();
        KArray2D resnJ = MatrixOperations.invert(matA, netlibJavaBlas);
        timeend = System.currentTimeMillis();
        System.out.println("Netlib JavaClass blas invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart=System.currentTimeMillis();
        SimpleMatrix resEjml= ejmlmatA.invert();
        timeend=System.currentTimeMillis();
        System.out.println("Ejml invert " + ((double) (timeend - timestart)) / 1000+" s");

        assert res != null;
        assert resJ != null;
        assert resnJ != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertEquals(res.get(i, j), resJ.get(i, j), eps);
                Assert.assertEquals(res.get(i, j), resnJ.get(i, j), eps);
                Assert.assertEquals(res.get(i, j), resEjml.getValue2D(i, j), eps);
            }
        }

    }
}
