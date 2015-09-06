package org.kevoree.modeling.blas;

import static jcuda.jcublas.JCublas.*;
import static jcuda.jcublas.JCublas2.*;
import static jcuda.jcublas.JCublas2.cublasGetVector;
import static jcuda.jcusolver.JCusolverDn.cusolverDnCreate;
import static jcuda.runtime.JCuda.*;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToDevice;

import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;
import jcuda.Sizeof;
import jcuda.jcublas.*;
import jcuda.Pointer;
import jcuda.jcublas.cublasHandle;
import jcuda.jcusolver.JCusolverDn;
import jcuda.jcusolver.cusolverDnHandle;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.netlib.util.intW;

import java.util.Random;


public class JCudaBlas implements KBlas {

    private cublasHandle handle;
   // private jcuda.jcusolver.cusolverDnHandle cuHandle;

    public JCudaBlas() {
        JCublas.initialize();
        JCublas2.initialize();
        JCublas.setExceptionsEnabled(true);
        JCublas2.setExceptionsEnabled(true);
        handle = new cublasHandle();
        cublasCreate(handle);
        jcuda.jcusolver.JCusolver.initialize();

      // cuHandle = new cusolverDnHandle();
        //cusolverDnCreate(cuHandle);

    }



    @Override
    public void dgemm(KBlasTransposeType transA, KBlasTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC) {

        // Allocate memory on the device
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        cudaMalloc(d_A, matA.length * Sizeof.DOUBLE);
        cudaMalloc(d_B, matB.length * Sizeof.DOUBLE);
        cudaMalloc(d_C, matC.length * Sizeof.DOUBLE);

        // Copy the memory from the host to the device
        JCublas2.cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        JCublas2.cublasSetVector(matB.length, Sizeof.DOUBLE, Pointer.to(matB), 1, d_B, 1);
        JCublas2.cublasSetVector(matC.length, Sizeof.DOUBLE, Pointer.to(matC), 1, d_C, 1);

        // Execute sgemm
        Pointer pAlpha = Pointer.to(new double[]{alpha});
        Pointer pBeta = Pointer.to(new double[]{beta});
        cublasDgemm(handle, transTypeToInt(transA), transTypeToInt(transB), m, n, k,
                pAlpha, d_A, ldA, d_B, ldB, pBeta, d_C, ldC);

        // Copy the result from the device to the host
        JCublas2.cublasGetVector(matC.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matC), 1);

        // Clean up
        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);
        cudaFree(pAlpha);
        cudaFree(pBeta);
    }

    @Override
    public void dgetrs(KBlasTransposeType transA, int dim, int nrhs, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] matB, int offsetB, int ldB, int[] info) {
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();

        cudaMalloc(d_A, matA.length * Sizeof.DOUBLE);
        cudaMalloc(d_B, matB.length * Sizeof.DOUBLE);
        cudaMalloc(d_C, matB.length * Sizeof.DOUBLE);

        Pointer pAlpha = Pointer.to(new double[]{1.0});
        Pointer pBeta = Pointer.to(new double[]{0.0});

        invertMatrix(dim, d_A);
        cublasDgemm(handle, transTypeToInt(transA), transTypeToInt(KBlasTransposeType.NOTRANSPOSE), dim, matB.length / dim, dim,
                pAlpha, d_A, ldA, d_B, ldB, pBeta, d_C, ldB);

        JCublas2.cublasGetVector(matB.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matB), 1);

        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);
        cudaFree(pAlpha);
        cudaFree(pBeta);



     /*   Pointer d_ipiv = new Pointer();
        Pointer infow = new Pointer();

        cudaMalloc(d_A, matA.length * Sizeof.DOUBLE);
        cudaMalloc(d_B, matB.length * Sizeof.DOUBLE);

        cudaMalloc(d_ipiv, ipiv.length * Sizeof.INT);
        cudaMalloc(infow, Sizeof.INT);
        // Copy the memory from the host to the device
        JCublas2.cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        JCublas2.cublasSetVector(matB.length, Sizeof.DOUBLE, Pointer.to(matB), 1, d_B, 1);
        JCublas2.cublasSetVector(ipiv.length, Sizeof.INT, Pointer.to(ipiv), 1, d_ipiv, 1);
        JCublas2.cublasSetVector(1, Sizeof.INT, Pointer.to(new int[1]), 1, infow, 1);


      //  JCusolverDn.cusolverDnDgetrs(cuHandle, transTypeToInt(transA), dim, nrhs, d_A, ldA, d_ipiv, d_B, ldB, infow);

        // Copy the result from the device to the host
        cublasGetVector(matB.length, Sizeof.DOUBLE, d_B, 1, Pointer.to(matB), 1);


        cudaFree(d_ipiv);
        cudaFree(infow);*/
        // Clean up




    }



    @Override
    public void dgetri(int n, double[] A, int offsetA, int ldA, int[] pivots, int offsetIpiv, double[] work, int offsetWork, int ldWork, int[] info) {
        Pointer dA = new Pointer();
        cublasAlloc(n * n, Sizeof.DOUBLE, dA);
        JCublas.cublasSetMatrix(n, n, Sizeof.DOUBLE, Pointer.to(A), n, dA, n);

        // Perform inv(U)
  /*      cudaDtrtri(n, dA);

        // Solve inv(A)*L = inv(U)
        Pointer dWork = new Pointer();
        cublasAlloc(n - 1, Sizeof.DOUBLE, dWork);

        for (int i = n - 1; i > 0; i--)
        {
            Pointer offset = at(dA, ((i - 1) * n + i));
            cudaMemcpy(dWork, offset, (n - 1) * Sizeof.DOUBLE,
                    cudaMemcpyDeviceToDevice);
            cublasDscal(n - i, 0, offset, 1);
            cublasDgemv('n', n, n - i, -1.0f,
                    at(dA, i * n), n, dWork, 1, 1.0f, at(dA, ((i - 1) * n)), 1);
        }

        cublasFree(dWork);

        // Pivot back to original order
        for (int i = n - 1; i >= 0; i--)
        {
            if (i != pivots[i])
            {
                cublasDswap(n, at(dA, i * n), 1, at(dA, pivots[i] * n), 1);
            }
        }*/

        invertMatrix(n, dA);
        JCublas.cublasGetMatrix(n, n, Sizeof.DOUBLE, dA, n, Pointer.to(A), n);
        cublasFree(dA);
    }


    @Override
    public void dgetrf(int m, int n, double[] A, int offsetA, int ldA, int[] pivots, int offsetIpiv, int[] info) {
   /*     Pointer dA = new Pointer();
        cublasAlloc(n * n, Sizeof.DOUBLE, dA);
        JCublas.cublasSetMatrix(n, n, Sizeof.DOUBLE, Pointer.to(A), n, dA, n);

        for (int i = 0; i < n; i++)
        {
            pivots[i] = i;
        }

        double[] factor = { 0.0f };
        Pointer pFactor = Pointer.to(factor);
        for (int i = 0; i < n - 1; i++)
        {
            Pointer offset = at(dA, i * n + i);

            int pivot = i - 1 + cublasIsamax(n - i, offset, 1);
            if (pivot != i)
            {
                pivots[i] = pivot;
                cublasDswap(n, at(dA, pivot), n, at(dA, i), n);
            }

            JCublas.cublasGetVector(1, Sizeof.DOUBLE, offset, 1, pFactor, 1);
            cublasDscal(n - i - 1, 1 / factor[0], at(offset, 1), 1);
            cublasDger(n - i - 1, n - i - 1, -1.0f,
                    at(offset, 1), 1, at(offset, n), n, at(offset, n + 1), n);
        }

        JCublas.cublasGetMatrix(n, n, Sizeof.DOUBLE, dA, n, Pointer.to(A), n);
        cublasFree(dA);

*/
    }

    @Override
    public void dorgqr(int m, int n, int k, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetWork, int lWork, int[] info) {


    }

    @Override
    public void dgeqrf(int m, int n, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetwork, int lWork, int[] info) {

    }

    @Override
      public void shutdown() {

        cublasDestroy(handle);
    }


    /**
     * Copies the given n x n matrix into device memory, inverts it by calling
     * {@link #invertMatrix(int, Pointer)}, and copies it back into the given
     * array. <br />
     * <br />
     * This method assumes that CUBLAS already has been initialized by calling
     * cublasInit.
     *
     * @param n The size of the matrix
     * @param A The matrix
     */
    public static void invertMatrix(int n, double A[])
    {
        Pointer dA = new Pointer();
        cublasAlloc(n * n, Sizeof.DOUBLE, dA);
        JCublas.cublasSetMatrix(n, n, Sizeof.DOUBLE, Pointer.to(A), n, dA, n);
        invertMatrix(n, dA);
        JCublas.cublasGetMatrix(n, n, Sizeof.DOUBLE, dA, n, Pointer.to(A), n);
        cublasFree(dA);
    }

    /**
     * Invert the n x n matrix that is given in device memory.<br />
     * <br />
     * This method assumes that CUBLAS already has been initialized by calling
     * cublasInit.
     *
     * @param n The size of the matrix
     * @param dA The matrix
     */
    public static void invertMatrix(int n, Pointer dA)
    {
        // Perform LU factorization
        int[] pivots = cudaDgetrfSquare(n, dA);

        // Perform inversion on factorized matrix
        cudaDgetri(n, dA, pivots);
    }

    /**
     * Convenience method that returns a pointer with the given offset (in
     * number of 4-byte double elements) from the given pointer.
     *
     * @param p The pointer
     * @param doubleOffset The offset
     * @return The new pointer
     */
    private static Pointer at(Pointer p, int doubleOffset)
    {
        return p.withByteOffset(doubleOffset * Sizeof.DOUBLE);
    }

    /**
     * cudaSgetrf performs an in-place LU factorization on a square matrix. Uses
     * the unblocked BLAS2 approach
     *
     * @param n The matrix size
     * @param dA The pointer to the matrix (in device memory)
     * @return The pivots
     */
    private static int[] cudaDgetrfSquare(int n, Pointer dA)
    {
        int[] pivots = new int[n];
        for (int i = 0; i < n; i++)
        {
            pivots[i] = i;
        }

        double[] factor = { 0.0f };
        Pointer pFactor = Pointer.to(factor);
        for (int i = 0; i < n - 1; i++)
        {
            Pointer offset = at(dA, i * n + i);

            int pivot = i - 1 + cublasIsamax(n - i, offset, 1);
            if (pivot != i)
            {
                pivots[i] = pivot;
                cublasDswap(n, at(dA, pivot), n, at(dA, i), n);
            }

            JCublas.cublasGetVector(1, Sizeof.DOUBLE, offset, 1, pFactor, 1);
            cublasDscal(n - i - 1, 1 / factor[0], at(offset, 1), 1);
            cublasDger(n - i - 1, n - i - 1, -1.0f,
                    at(offset, 1), 1, at(offset, n), n, at(offset, n + 1), n);
        }
        return pivots;
    }

    /***
     * cudaSgetri Computes the inverse of an LU-factorized square matrix
     *
     * @param n
     *            The matrix size
     * @param dA
     *            The matrix in device memory
     * @param pivots
     *            The pivots
     */
    private static void cudaDgetri(int n, Pointer dA, int[] pivots)
    {
        // Perform inv(U)
        cudaDtrtri(n, dA);

        // Solve inv(A)*L = inv(U)
        Pointer dWork = new Pointer();
        cublasAlloc(n - 1, Sizeof.DOUBLE, dWork);

        for (int i = n - 1; i > 0; i--)
        {
            Pointer offset = at(dA, ((i - 1) * n + i));
            cudaMemcpy(dWork, offset, (n - 1) * Sizeof.DOUBLE,
                    cudaMemcpyDeviceToDevice);
            cublasDscal(n - i, 0, offset, 1);
            cublasDgemv('n', n, n - i, -1.0f,
                    at(dA, i * n), n, dWork, 1, 1.0f, at(dA, ((i - 1) * n)), 1);
        }

        cublasFree(dWork);

        // Pivot back to original order
        for (int i = n - 1; i >= 0; i--)
        {
            if (i != pivots[i])
            {
                cublasDswap(n, at(dA, i * n), 1, at(dA, pivots[i] * n), 1);
            }
        }

    }

    /***
     * cudaStrtri Computes the inverse of an upper triangular matrix in place
     * Uses the unblocked BLAS2 approach
     *
     * @param n The size of the matrix
     * @param dA The matrix
     */
    private static void cudaDtrtri(int n, Pointer dA)
    {
        double[] factor = { 0.0f };
        Pointer pFactor = Pointer.to(factor);
        for (int i = 0; i < n; i++)
        {
            Pointer offset = at(dA, i * n);
            JCublas.cublasGetVector(1, Sizeof.DOUBLE, at(offset, i), 1, pFactor, 1);
            factor[0] = 1 / factor[0];
            JCublas.cublasSetVector(1, Sizeof.DOUBLE, pFactor, 1, at(offset, i), 1);
            cublasDtrmv('u', 'n', 'n', i, dA, n, offset, 1);
            cublasDscal(i, -factor[0], offset, 1);
        }
    }

    // === Utility methods for this sample ====================================

    /**
     * Creates a new array with the given size, containing random data
     *
     * @param size The size of the array
     * @return The array
     */
    private static double[] createRandomDoubleData(int size)
    {
        Random random = new Random(0);
        double a[] = new double[size];
        for (int i = 0; i < size; i++)
        {
            a[i] = random.nextDouble();
        }
        return a;
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
