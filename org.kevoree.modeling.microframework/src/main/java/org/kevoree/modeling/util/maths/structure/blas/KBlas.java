package org.kevoree.modeling.util.maths.structure.blas;

import org.kevoree.modeling.util.maths.structure.KArray2D;

public interface KBlas {

    //For Matrix Multiplications
    void dgemm(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, double[] paramArrayOfDouble3, int paramInt8, int paramInt9);

    //To solve AX=B
    void dgetrs(KBlasTransposeType paramString, int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, int[] paramintW);

    //To invert a matrix
    void dgetri(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, int[] paramArrayOfInt, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, int[] paramintW);

    //To factorize LU matrix
    void dgetrf(int paramInt1, int paramInt2, double[] paramArrayOfDouble, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, int[] paramintW);

    //To transpose matrix
    void trans(KArray2D matA, KArray2D result);

    void shutdown();

    //Level 1 Blas
    //To scale matrix
    void dscale(double alpha, KArray2D matA);


    //For matrix-matrix multiplication

    //matA := alpha* matA
   /* void dscal(double alpha,KArray2D matA);

    //Level 3 Blas
    //For matrix-matrix multiplication

    void dgetrf(int paramInt1, int paramInt2, double[] paramArrayOfDouble, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, int[] paramintW);


    //void dgetrs(KBlasTransposeType paramString, int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, int[] paramintW);

    //matC := alpha*op(matA)*op(matB) + beta*matC
    //trans: 'n': matA -> matA (normal)
    //trans: 't': matA -> transpose(matA)
    //trans: 'c': matA -> conjugateTransp(matA)
    void dgemm(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta,  KArray2D matC);

    void trans(KArray2D matA, KArray2D result);

    void shutdown();

    void dgetrs(KBlasTransposeType trans, int rows, int columns, double[] data, int rows1, int[] piv, double[] data1, int rows2, int[] info);
*/
}
