package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.TransposeAlgs;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
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


    private void traditional(KBlasTransposeType transA, KBlasTransposeType transB, KArray2D matA, KArray2D matB, KArray2D matC,double alpha, double beta) {
       int dim=0;
        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)){
            dim=matA.columns();
        }
        else {
            dim=matA.rows();
        }

        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                matC.set(i,j,beta*matC.get(i,j));
                for (int k = 0; k < dim; k++) {
                    if(transA.equals(KBlasTransposeType.NOTRANSPOSE)&& transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                        matC.add(i, j, alpha * matA.get(i, k) * matB.get(k, j));
                    }
                    else if( transA.equals(KBlasTransposeType.TRANSPOSE) && transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                        matC.add(i, j, alpha * matA.get(k,i) * matB.get(k, j));
                    }
                    else if ( transA.equals(KBlasTransposeType.NOTRANSPOSE) && transB.equals(KBlasTransposeType.TRANSPOSE)){
                        matC.add(i, j, alpha * matA.get(i, k) * matB.get(j, k));
                    }
                    else{
                        matC.add(i, j, alpha * matA.get(k, i) * matB.get(j, k));
                    }
                }
            }
        }
    }



    @Test
    public void multiplyTest() {

        KBlas java = new JavaBlas();

        int r=100;
        int[] dimA = {r, r+3};
        int[] dimB = {r+3, r+5};
        KBlasTransposeType transA=KBlasTransposeType.NOTRANSPOSE;
        KBlasTransposeType transB=KBlasTransposeType.NOTRANSPOSE;
        boolean rand=true;
        double alpha=0.8;
        double beta =0.2;
        double eps=1e-7;



        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);

        NativeArray2D matB = new NativeArray2D(dimB[0], dimB[1]);
        MatrixOperations.initMatrice(matB, rand);

        int[] dimC= new int[2];

        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)&& transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
            dimC[0]=dimA[0];
            dimC[1]=dimB[1];
        }
        else if( transA.equals(KBlasTransposeType.TRANSPOSE) && transB.equals(KBlasTransposeType.NOTRANSPOSE)){
            dimC[0]=dimA[1];
            dimC[1]=dimB[1];
        }
        else if ( transA.equals(KBlasTransposeType.NOTRANSPOSE) && transB.equals(KBlasTransposeType.TRANSPOSE)){
            dimC[0]=dimA[0];
            dimC[1]=dimB[0];
        }
        else{
            dimC[0]=dimA[1];
            dimC[1]=dimB[0];
        }


        NativeArray2D matC = new NativeArray2D(dimC[0],dimC[1]);
        MatrixOperations.initMatrice(matC, rand);
        KArray2D matresult =matC.clone();

        SimpleMatrix ejmlmatA= new SimpleMatrix(dimA[0], dimA[1]);
        SimpleMatrix ejmlmatB= new SimpleMatrix(dimB[0], dimB[1]);
        SimpleMatrix ejmlmatC= new SimpleMatrix(dimC[0], dimC[1]);

        MatrixOperations.copyMatrix(matA, ejmlmatA);
        MatrixOperations.copyMatrix(matB, ejmlmatB);
        MatrixOperations.copyMatrix(matC, ejmlmatC);

        System.out.println("Init done");

        long timestart, timeend;

        timestart=System.currentTimeMillis();
        traditional(transA, transB, matA, matB, matC, alpha, beta);
        timeend=System.currentTimeMillis();
        System.out.println("For loop " + ((double) (timeend - timestart)) / 1000);

        timestart=System.currentTimeMillis();
        MatrixOperations.multiplyTransposeAlphaBetaResult(transA, transB, alpha, matA, matB, beta, matresult, java);
        timeend=System.currentTimeMillis();
        System.out.println("Java blas " + ((double) (timeend - timestart)) / 1000);


        timestart=System.currentTimeMillis();
        CommonOps.multAlphaBeta(alpha, ejmlmatA.getMatrix(), ejmlmatB.getMatrix(), ejmlmatC.getMatrix(), beta);
        timeend=System.currentTimeMillis();
        System.out.println("EJML " + ((double) (timeend - timestart)) / 1000);


        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                Assert.assertEquals(matresult.get(i, j), matC.get(i, j), eps);
                Assert.assertEquals(ejmlmatC.getValue2D(i, j), matC.get(i, j), eps);
            }
        }


    }


}
