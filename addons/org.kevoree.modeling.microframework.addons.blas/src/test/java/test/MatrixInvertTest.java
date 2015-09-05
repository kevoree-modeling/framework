package test;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.blas.JCudaBlas;
import org.kevoree.modeling.blas.NetlibBlas;
import org.kevoree.modeling.blas.F2JBlas;
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
    public void invertMatrix() {
        int r = 2000;
        int times=1;
        int[] dimA = {r, r};
        boolean rand = true;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);

        JavaBlas javablas = new JavaBlas();
        NetlibBlas nativeblas = new NetlibBlas();
        F2JBlas f2JBlas = new F2JBlas();
        JCudaBlas jCudaBlas = new JCudaBlas();


        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0],dimA[1]);
        CommonOps.copyMatrix(matA, ejmlmatA);


        long timestart, timeend;
        KArray2D res,resJ,resnJ,rescu;
        res=new NativeArray2D(1,1);
        resJ=new NativeArray2D(1,1);
        resnJ=new NativeArray2D(1,1);
        rescu=new NativeArray2D(1,1);
        SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart=System.currentTimeMillis();
        for(int k=0;k<times;k++) {
            res = MatrixOperations.invert(matA, nativeblas);
        }
        timeend=System.currentTimeMillis();
        System.out.println("Netlib blas invert " + ((double) (timeend - timestart)) / (1000*times)+" s");

        timestart = System.currentTimeMillis();
        for(int k=0;k<times;k++) {
            resJ = MatrixOperations.invert(matA, javablas);
        }
        timeend = System.currentTimeMillis();
        System.out.println("Java Src blas invert " + ((double) (timeend - timestart)) / (1000*times)+" s");

        timestart = System.currentTimeMillis();
        for(int k=0;k<times;k++) {
            resnJ = MatrixOperations.invert(matA, f2JBlas);
        }
        timeend = System.currentTimeMillis();
        System.out.println("Netlib JavaClass blas invert " + ((double) (timeend - timestart)) / (1000*times)+" s");

        timestart = System.currentTimeMillis();
        for(int k=0;k<times;k++) {
            rescu = MatrixOperations.invert(matA, jCudaBlas);
        }
        timeend = System.currentTimeMillis();
        System.out.println("Cuda blas invert " + ((double) (timeend - timestart)) / (1000*times)+" s");

        timestart=System.currentTimeMillis();
        for(int k=0;k<times;k++) {
            resEjml = ejmlmatA.invert();
        }
        timeend=System.currentTimeMillis();
        System.out.println("Ejml invert " + ((double) (timeend - timestart)) / (1000*times)+" s");

        assert res != null;
        assert resJ != null;
        assert resnJ != null;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertEquals(res.get(i, j), resJ.get(i, j), eps);
                Assert.assertEquals(res.get(i, j), resnJ.get(i, j), eps);
                Assert.assertEquals(res.get(i, j), resEjml.getValue2D(i, j), eps);
                Assert.assertEquals(res.get(i, j), rescu.get(i, j), eps);
            }
        }

    }
}
