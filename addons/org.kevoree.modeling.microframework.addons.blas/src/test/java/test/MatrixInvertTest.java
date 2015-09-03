package test;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.blas.NetlibBlas;
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


        long timestart, timeend;

        timestart=System.currentTimeMillis();
        KArray2D res= MatrixOperations.invert(matA,nativeblas);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib blas invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart = System.currentTimeMillis();
        KArray2D resJ = MatrixOperations.invert(matA, javablas);
        timeend = System.currentTimeMillis();
        System.out.println("Java blas invert " + ((double) (timeend - timestart)) / 1000+" s");

        assert res != null;
        assert resJ != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertEquals(res.get(i, j), resJ.get(i, j), eps);
            }
        }

    }
}
