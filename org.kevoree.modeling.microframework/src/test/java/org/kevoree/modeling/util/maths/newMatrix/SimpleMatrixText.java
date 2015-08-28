package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.TransposeAlgs;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

import java.util.Random;

/**
 * @ignore ts
 */
public class SimpleMatrixText {
    @Test
    public void transposeTest() {
        NativeArray2D matA = new NativeArray2D(3, 5);

        //test normal transpose
        int k = 1;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, k);
                k++;
            }
        }

        KArray2D matB = MatrixOperations.transpose(matA, null);

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

        matB = MatrixOperations.transpose(matA, null);
        Assert.assertTrue(matA.columns() == matB.rows());
        Assert.assertTrue(matA.rows() == matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertTrue(matA.get(i, j) == matB.get(j, i));
            }
        }

        //test BIG transpose
        matA = new NativeArray2D(380, 381);

        k = 1;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                matA.set(i, j, k);
                k++;
            }
        }

        matB = MatrixOperations.transpose(matA, null);
        Assert.assertTrue(matA.columns() == matB.rows());
        Assert.assertTrue(matA.rows() == matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                Assert.assertTrue(matA.get(i, j) == matB.get(j, i));
            }
        }
    }


    private void traditional(KArray2D matA, KArray2D matB, KArray2D matC) {
        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, matA.get(i, k) * matB.get(k, j));
                }
            }
        }
    }

    @Test
    public void multiplyTest() {

        int[] dimA = {30, 100};
        int[] dimB = {100, 50};

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

        NativeArray2D matC = new NativeArray2D(matA.rows(), matB.columns());


        traditional(matA, matB, matC);
        KArray2D matRes = MatrixOperations.multiply(matA, matB, null);


        Assert.assertTrue(matRes.rows() == matC.rows());
        Assert.assertTrue(matRes.columns() == matC.columns());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                Assert.assertTrue(matRes.get(i, j) == matC.get(i, j));
            }
        }


    }
}
