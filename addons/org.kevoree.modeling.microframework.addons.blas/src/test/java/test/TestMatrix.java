package test;

import org.junit.Assert;
import org.junit.Test;
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

    private void traditional(KArray2D matA, KArray2D matB, KArray2D matC, double alpha, double beta) {
        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                matC.set(i, j, beta * matC.get(i, j));
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, alpha * matA.get(i, k) * matB.get(k, j));
                }
            }
        }
    }

    private void initMatrice(KArray2D matA, boolean random) {
        Random rand = new Random();
        int k = 0;
        for (int j = 0; j < matA.columns(); j++) {
            for (int i = 0; i < matA.rows(); i++) {
                if (random) {
                    matA.set(i, j, rand.nextDouble());
                } else {
                    matA.set(i, j, k);
                }
                k++;
            }
        }
    }

    @Test
    public void multiplyTest() {

        KBlas javaBlas = new JavaBlas();
        KBlas netlibBlas = new NetlibBlas();
        KBlas jcuda = new JCudaBlas();
   //     KBlas jCudaBlas = new JCudaBlas();

        int r = 100;
        int[] dimA = {r, r + 1};
        int[] dimB = {r + 1, r};
        boolean rand = true;
        double alpha = 0.7;
        double beta = 0.3;
        double eps = 1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        initMatrice(matA, rand);

        NativeArray2D matB = new NativeArray2D(dimB[0], dimB[1]);
        initMatrice(matB, rand);


        NativeArray2D matOriginal = new NativeArray2D(matA.rows(), matB.columns());
        initMatrice(matOriginal, rand);

        KArray2D matTrad = matOriginal.clone();
        KArray2D matNetlib = matOriginal.clone();
        KArray2D matJava = matOriginal.clone();
        KArray2D matCuda = matOriginal.clone();
     //   KArray2D matCuda = matOriginal.clone();

        System.out.println("Data generated");
        long timestart, timeend;

        timestart=System.currentTimeMillis();
        traditional(matA, matB, matTrad, alpha, beta);
        timeend=System.currentTimeMillis();
        System.out.println("For loop " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        MatrixOperations.multiplyAlphaBetaResult(alpha, matA, matB, beta, matJava, javaBlas);
        timeend=System.currentTimeMillis();
        System.out.println("Java blas " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        MatrixOperations.multiplyAlphaBetaResult(alpha, matA, matB, beta, matNetlib, netlibBlas);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib Blas " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        MatrixOperations.multiplyAlphaBetaResult(alpha, matA, matB, beta, matCuda, jcuda);
        timeend=System.currentTimeMillis();
        System.out.println("Cuda Blas " + ((double) (timeend - timestart)) / 1000);



        for (int i = 0; i < matOriginal.rows(); i++) {
            for (int j = 0; j < matOriginal.columns(); j++) {
                Assert.assertEquals(matTrad.get(i, j), matJava.get(i, j), eps);
                Assert.assertEquals(matTrad.get(i, j), matNetlib.get(i, j), eps);
                Assert.assertEquals(matTrad.get(i, j), matCuda.get(i, j), eps);
            }
        }

        javaBlas.shutdown();
        netlibBlas.shutdown();
        jcuda.shutdown();
        System.out.println("Test succeeded");
    }
}
