package test;

import org.junit.Assert;
import org.kevoree.modeling.blas.NetlibBlas;
import org.kevoree.modeling.blas.NetlibBlasDebug;
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
 * Created by assaad on 03/09/15.
 */
public class Debug {
    public static void main(String[] arg){
        int r=3000;
        int[] dimA = {r, r};

        KBlas java = new JavaBlas();
        KBlas netlib = new NetlibBlas();
        KBlas netlibdb = new NetlibBlasDebug();

       // for(int l=0;l<10000;l++) {
            boolean rand = true;
            double eps = 1e-5;

            NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
            MatrixOperations.initMatrice(matA, rand);
            matA.set(0, 0, 5);

            SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0], dimA[1]);
            MatrixOperations.copyMatrix(matA, ejmlmatA);

            long timestart, timeend;
            System.out.println("Init done");

            timestart = System.currentTimeMillis();
            KArray2D res = MatrixOperations.invert(matA, netlib);
            timeend = System.currentTimeMillis();
            System.out.println("Netlib invert " + ((double) (timeend - timestart)) / 1000 + " s");

         /*   timestart = System.currentTimeMillis();
            KArray2D resJ = MatrixOperations.invert(matA, java);
            timeend = System.currentTimeMillis();
            System.out.println("Java invert " + ((double) (timeend - timestart)) / 1000 + " s");*/

        timestart = System.currentTimeMillis();
        KArray2D resJdb = MatrixOperations.invert(matA, netlibdb);
        timeend = System.currentTimeMillis();
        System.out.println("Netlib debug invert " + ((double) (timeend - timestart)) / 1000 + " s");

         /*   timestart = System.currentTimeMillis();
            SimpleMatrix resEjml = ejmlmatA.invert();
            timeend = System.currentTimeMillis();
            System.out.println("Ejml invert " + ((double) (timeend - timestart)) / 1000 + " s");

            System.out.println("done");*/

            assert res != null;
           // assert resJ != null;
        assert resJdb != null;
            for (int i = 0; i < matA.rows(); i++) {
                for (int j = 0; j < matA.columns(); j++) {
                  //  Assert.assertEquals(res.get(i, j), resJ.get(i, j), eps);
                    Assert.assertEquals(res.get(i, j), resJdb.get(i, j), eps);
                 //   Assert.assertEquals(res.get(i, j), resEjml.getValue2D(i, j), eps);

                }
            }
        }

    //}
}
