package org.kevoree.modeling.util.maths.structure.matrix;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

import java.util.Random;

public class MatrixOperations {

    public static int leadingDimension(KArray2D matA){
        return Math.max(matA.columns(),matA.rows());
    }

    public static KArray2D transpose(KArray2D matA, KBlas blas) {
        KArray2D result = new NativeArray2D(matA.columns(), matA.rows());
        blas.trans(matA, result);
        return result;
    }


    public static KArray2D scaleInPlace(KArray2D matA, double alpha, KBlas blas) {
        blas.dscale(alpha, matA);
        return matA;
    }

    public static KArray2D multiply(KArray2D matA, KArray2D matB, KBlas blas) {
        NativeArray2D matC = new NativeArray2D(matA.rows(), matB.columns());
        //blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, 1, matA, matB, 0, matC);
        blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matC.rows(), matC.columns(), matA.columns(), 1.0, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), 0.0, matC.data(), 0, matC.rows());
        return matC;
    }

    public static void copyMatrix(KArray2D matA, SimpleMatrix ejmlmatA) {
        for(int i=0;i<matA.rows();i++){
            for(int j=0;j<matA.columns();j++){
                ejmlmatA.setValue2D(i,j,matA.get(i,j));
            }
        }
    }

    public static KArray2D multiplyTransposeAlpha(KBlasTransposeType transA, KBlasTransposeType transB,KArray2D matA, KArray2D matB, double alpha, KBlas blas) {
        if (testDimensionsAB(transA, transB, matA, matB)) {
            int k = 0;
            int[] dimC = new int[2];
            if (transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
                k = matA.columns();
                if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                }
            } else {
                k = matA.rows();
                if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.rows();
                }
            }


            NativeArray2D matC = new NativeArray2D(dimC[0], dimC[1]);
            blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), 0, matC.data(), 0, matC.rows());
            return matC;
        }
        else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static void multiplyAlphaBetaResult(double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC, KBlas blas) {
        //  blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, alpha, matA, matB, beta, matC);
        if (testDimensionsABC(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matA, matB, matC)) {
            blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matC.rows(), matC.columns(), matA.columns(), alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
        } else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static void multiplyTransposeAlphaBetaResult(KBlasTransposeType transA, KBlasTransposeType transB, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC, KBlas blas) {
        //  blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, alpha, matA, matB, beta, matC);

        if (testDimensionsABC(transA, transB, matA, matB, matC)) {
            int k;
            if (transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
                k = matA.columns();
            } else {
                k = matA.rows();
            }
            blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
        }
        else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static boolean testDimensionsABC(KBlasTransposeType transA, KBlasTransposeType transB, KArray2D matA, KArray2D matB, KArray2D matC) {

        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.columns()==matB.rows() && matC.rows()==matA.rows() &&matC.columns()==matB.columns());
            }
            else{
                return (matA.columns()==matB.columns() && matC.rows()==matA.rows() &&matC.columns()==matB.rows());
            }
        }
        else {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.rows()==matB.rows() && matC.rows()==matA.columns() &&matC.columns()==matB.columns());
            }
            else{
                return (matA.rows()==matB.columns() && matC.rows()==matA.columns() &&matC.columns()==matB.rows());
            }
        }
    }

    public static boolean testDimensionsAB(KBlasTransposeType transA, KBlasTransposeType transB, KArray2D matA, KArray2D matB) {

        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.columns()==matB.rows());
            }
            else{
                return (matA.columns()==matB.columns());
            }
        }
        else {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.rows()==matB.rows());
            }
            else{
                return (matA.rows()==matB.columns());
            }
        }
    }


    public static void initMatrice(KArray2D matA, boolean random){
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

    public static KArray2D invert( KArray2D mat, KBlas blas ) {
        if(mat.rows()!=mat.columns()){
            return null;
        }

        DenseLU alg = new DenseLU(mat.rows(),mat.columns());
        KArray2D result = new NativeArray2D(mat.rows(),mat.columns());
        NativeArray2D A_temp=new NativeArray2D(mat.rows(),mat.columns());
        System.arraycopy(mat.data(),0,A_temp.data(),0,mat.columns()*mat.rows());

        DenseLU dlu = new DenseLU(A_temp.rows(),A_temp.columns());
        if (dlu.invert(A_temp,blas)){
            result.setData(A_temp.data());
            return result;
        }
        else {
            return null;
        }
    }

    public static KArray2D createIdentity(int width) {
        KArray2D ret = new NativeArray2D(width, width);
        ret.setAll(0);
        for (int i = 0; i < width; i++) {
            ret.set(i, i, 1);
        }
        return ret;
    }

}
