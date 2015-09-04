package org.kevoree.modeling.blas;

import com.github.fommil.netlib.LAPACK;
import org.kevoree.modeling.util.maths.structure.blas.*;
import com.github.fommil.netlib.BLAS;
import org.netlib.util.intW;

public class NetlibBlas implements KBlas {
    private BLAS blas;
    private LAPACK lapack;


    public NetlibBlas() {
        blas = BLAS.getInstance();
        lapack=LAPACK.getInstance();
    }

    @Override
    public void dgemm(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, double[] paramArrayOfDouble3, int paramInt8, int paramInt9) {
        blas.dgemm(transTypeToChar(paramString1),transTypeToChar(paramString2), paramInt1,  paramInt2,  paramInt3,  paramDouble1, paramArrayOfDouble1, paramInt4, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, paramDouble2, paramArrayOfDouble3, paramInt8, paramInt9);
        }

    @Override
    public void dgetrs(KBlasTransposeType paramString, int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetrs(transTypeToChar(paramString), paramInt1, paramInt2, paramArrayOfDouble1, paramInt3, paramInt4, paramArrayOfInt, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, newint);
        paramintW[0] = newint.val;
    }

    @Override
    public void dgetri(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, int[] paramArrayOfInt, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetri(paramInt1, paramArrayOfDouble1,paramInt2, paramInt3, paramArrayOfInt, paramInt4, paramArrayOfDouble2, paramInt5, paramInt6, newint);
        paramintW[0] = newint.val;

    }


    @Override
    public void dgetrf(int paramInt1, int paramInt2, double[] paramArrayOfDouble, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetrf(paramInt1, paramInt2, paramArrayOfDouble, paramInt3, paramInt4, paramArrayOfInt, paramInt5, newint);
        paramintW[0] = newint.val;
    }

    @Override
    public void dorgqr(int paramInt1, int paramInt2, int paramInt3, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, double[] paramArrayOfDouble3, int paramInt7, int paramInt8, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dorgqr(paramInt1, paramInt2, paramInt3, paramArrayOfDouble1, paramInt4, paramInt5, paramArrayOfDouble2, paramInt6, paramArrayOfDouble3, paramInt7, paramInt8, newint);
        paramintW[0]=newint.val;
    }

    @Override
    public void dgeqrf(int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, double[] paramArrayOfDouble3, int paramInt6, int paramInt7, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgeqrf(paramInt1, paramInt2, paramArrayOfDouble1, paramInt3, paramInt4, paramArrayOfDouble2, paramInt5, paramArrayOfDouble3, paramInt6, paramInt7, newint);
        paramintW[0]=newint.val;
    }

    @Override
    public void shutdown() {
        blas = null;
    }

    private static final String TRANSPOSE_TYPE_CONJUCATE = "c";

    private static final String TRANSPOSE_TYPE_NOTRANSPOSE = "n";

    private static final String TRANSPOSE_TYPE_TRANSPOSE = "t";

    private static String transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return null;
    }

}
