package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.TransposeAlgs;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

import java.util.Random;

/**
 * @ignore ts
 */
public class SimpleMatrixText {

    @Test
    public void transposeTest() {
        KBlas java = new JavaBlas();

        NativeArray2D matA = new NativeArray2D(3, 5);

        //test normal transpose
        int k = 1;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, k);
                k++;
            }
        }

        KArray2D matB = MatrixOperations.transpose(matA, java);

        Assert.assertTrue(matA.columns() == matB.rows());
        Assert.assertTrue(matA.rows() == matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertTrue(matA.get(i, j) == matB.get(j, i));
            }
        }

        //test square transpose

        DenseMatrix64F test = new DenseMatrix64F(5, 5);


        matA = new NativeArray2D(5, 5);

        k = 1;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, k);
                test.set(i, j, k);
                k++;
            }
        }

        TransposeAlgs.square(test);

        matB = MatrixOperations.transpose(matA, java);
        Assert.assertTrue(matA.columns() == matB.rows());
        Assert.assertTrue(matA.rows() == matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertTrue(matA.get(i, j) == matB.get(j, i));
            }
        }

        //test BIG transpose
        matA = new NativeArray2D(450, 600);

        k = 1;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, k);
                k++;
            }
        }

        matB = MatrixOperations.transpose(matA, java);
        Assert.assertTrue(matA.columns() == matB.rows());
        Assert.assertTrue(matA.rows() == matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertTrue(matA.get(i, j) == matB.get(j, i));
            }
        }
    }


    private void traditional(KArray2D matA, KArray2D matB, KArray2D matC,double alpha, double beta) {
        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                matC.set(i,j,beta*matC.get(i,j));
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, alpha*matA.get(i, k) * matB.get(k, j));
                }
            }
        }
    }

    private void initMatrice(KArray2D matA, boolean random){
        Random rand = new Random();
        int k=0;
        for (int j = 0; j < matA.columns(); j++) {
            for (int i = 0; i < matA.rows(); i++) {
                if(random){
                    matA.set(i, j, rand.nextDouble());
                }
                else {
                    matA.set(i, j, k);
                }
                k++;
            }
        }
    }

    @Test
    public void multiplyTest() {

        KBlas java = new JavaBlas();

        int r=500;
        int[] dimA = {r, r+5};
        int[] dimB = {r+5, r};
        boolean rand=true;
        double alpha=0.7;
        double beta =0.3;
        double eps=1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        initMatrice(matA,rand);

        NativeArray2D matB = new NativeArray2D(dimB[0], dimB[1]);
        initMatrice(matB, rand);


        NativeArray2D matC = new NativeArray2D(matA.rows(), matB.columns());
        initMatrice(matC, rand);
        KArray2D matresult =matC.clone();


        long timestart, timeend;

        timestart=System.currentTimeMillis();
        traditional(matA, matB, matC, alpha, beta);
        timeend=System.currentTimeMillis();
        System.out.println("For loop " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        MatrixOperations.multiplyAlphaBeta(alpha, matA, matB, beta, matresult, java);
        timeend=System.currentTimeMillis();
        System.out.println("Java blas " + ((double) (timeend - timestart)) / 1000);

        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                Assert.assertEquals(matresult.get(i, j), matC.get(i, j), eps);
            }
        }


    }
}
