import org.junit.Assert;
import org.kevoree.modeling.blas.JCudaBlas;
import org.kevoree.modeling.blas.NetlibBlas;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

import java.util.Random;

/**
 * Created by assaa_000 on 29/08/2015.
 */
public class TestMatrix {
    private static void traditional(KArray2D matA, KArray2D matB, KArray2D matC) {
        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, matA.get(i, k) * matB.get(k, j));
                }
            }
        }
    }


    public static void main (String[] arg) {
        double eps =1e-7;
        KBlas java = new JavaBlas();
        KBlas netlib = new NetlibBlas();
        KBlas cuda = new JCudaBlas();

        int r=1024*4;
        int[] dimA = {r, r};
        int[] dimB = {r, r};

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);

        Random rand = new Random();
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, rand.nextDouble());
            }
        }

        NativeArray2D matB = new NativeArray2D(dimB[0], dimB[1]);

        for (int i = 0; i < matB.rows(); i++) {
            for (int j = 0; j < matB.columns(); j++) {
                matB.set(i, j, rand.nextDouble());
            }
        }

      //  NativeArray2D matC = new NativeArray2D(matA.rows(), matB.columns());

        System.out.println("Data generated");
        long start, end;

   /*    start = System.nanoTime();
        traditional(matA, matB, matC);
        end = System.nanoTime();
        System.out.println("for java " + ((double) (end - start)) / 1000000000 + " s");

        start = System.nanoTime();
        KArray2D matRes = MatrixOperations.multiply(matA, matB, java);
        end = System.nanoTime();
        System.out.println("ejml java " + ((double) (end - start)) / 1000000000 + " s");

*/

        start = System.nanoTime();
        KArray2D matCuda = MatrixOperations.multiply(matA, matB, cuda);
        end = System.nanoTime();
        System.out.println("Cuda " + ((double) (end - start)) / 1000000000 + " s");

        start = System.nanoTime();
        KArray2D matNetlib = MatrixOperations.multiply(matA, matB, netlib);
        end = System.nanoTime();
        System.out.println("Netlib " + ((double) (end - start)) / 1000000000 + " s");




/*
        Assert.assertTrue(matRes.rows() == matC.rows());
        Assert.assertTrue(matRes.columns() == matC.columns());
        for (int i = 0; i <  matA.rows(); i++) {
            for (int j = 0; j < matB.columns(); j++) {
                Assert.assertEquals(matRes.get(i, j), matC.get(i, j),eps);
                Assert.assertEquals(matCuda.get(i, j), matC.get(i, j),eps);
                Assert.assertEquals(matNetlib.get(i, j), matC.get(i, j),eps);
            }
        }*/

    }
}
