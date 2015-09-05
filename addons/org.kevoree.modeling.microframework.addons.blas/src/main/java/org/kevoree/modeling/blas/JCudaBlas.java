package org.kevoree.modeling.blas;

import static jcuda.jcublas.JCublas2.*;
import static jcuda.jcublas.JCublas2.cublasDestroy;
import static jcuda.jcublas.cublasOperation.CUBLAS_OP_N;
import static jcuda.runtime.JCuda.*;

import jcuda.Sizeof;
import jcuda.jcublas.*;
import jcuda.Pointer;
import jcuda.jcublas.cublasHandle;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;




public class JCudaBlas implements KBlas {

    public JCudaBlas() {

    }

    private static void cublasDestroy(cublasHandle handle) {

    }


    @Override
    public void dgemm(KBlasTransposeType transA, KBlasTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC) {
        // Create a CUBLAS handle
        cublasHandle handle = new cublasHandle();
        cublasCreate(handle);

        // Allocate memory on the device
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        cudaMalloc(d_A, matA.length * Sizeof.DOUBLE);
        cudaMalloc(d_B, matB.length * Sizeof.DOUBLE);
        cudaMalloc(d_C, matC.length * Sizeof.DOUBLE);

        // Copy the memory from the host to the device
        cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        cublasSetVector(matB.length , Sizeof.DOUBLE, Pointer.to(matB), 1, d_B, 1);
        cublasSetVector(matC.length, Sizeof.DOUBLE, Pointer.to(matC), 1, d_C, 1);

        // Execute sgemm
        Pointer pAlpha = Pointer.to(new double[]{alpha});
        Pointer pBeta = Pointer.to(new double[]{beta});
        cublasDgemm(handle, transTypeToInt(transA),  transTypeToInt(transB), m, n, k,
                pAlpha, d_A, ldA, d_B, ldB, pBeta, d_C, ldC);

        // Copy the result from the device to the host
        cublasGetVector(matC.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matC), 1);

        // Clean up
        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);
        cublasDestroy(handle);
    }

    @Override
    public void dgetrs(KBlasTransposeType transA, int dim, int nrhs, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] matB, int offsetB, int ldB, int[] info) {


      //  JCublas2.

    }

    @Override
    public void dgetri(int dim, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] work, int offsetWork, int ldWork, int[] info) {

    }

    @Override
    public void dgetrf(int rows, int columns, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, int[] info) {

    }

    @Override
    public void dorgqr(int m, int n, int k, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetWork, int lWork, int[] info) {

    }

    @Override
    public void dgeqrf(int m, int n, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetwork, int lWork, int[] info) {

    }

    @Override
      public void shutdown() {
    }

    private static final char TRANSPOSE_TYPE_CONJUCATE = 'c';

    private static final char TRANSPOSE_TYPE_NOTRANSPOSE = 'n';

    private static final char TRANSPOSE_TYPE_TRANSPOSE = 't';

    private static char transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return '0';
    }


    private static int transTypeToInt(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return cublasOperation.CUBLAS_OP_C;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_N;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_T;
        }
        return '0';
    }

}
