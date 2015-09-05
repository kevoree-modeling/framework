package org.kevoree.modeling.util.maths.structure.blas;

import org.kevoree.modeling.util.maths.structure.KArray2D;

public interface KBlas {

    /**
     * Matrix multiplications
     * matC = alpha*matA*matB + beta*matC
     * @param transA: if we need to transpose matrix A
     * @param transB: if we need to transpose matrix B
     * @param m: rows for C
     * @param n: columns for C
     * @param k: intermediate dimension
     * @param alpha: multiplicative parameter for matA*matB
     * @param matA: first matrix
     * @param offsetA: offset in the array of matA, usually 0
     * @param ldA: number of rows in matA to jump to reach the second column
     * @param matB: second matrix
     * @param offsetB: offset in the array of matB, usually 0
     * @param ldB: number of rows in matB to jump to reach the second column
     * @param beta: scaling parameter for matC
     * @param matC: third matrix, which contains the result.
     * @param offsetC: offset in the array of matC, usually 0
     * @param ldC: number of rows in matC to jump to reach the second column     */
    void dgemm(KBlasTransposeType transA, KBlasTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC);

    /**
     * To solve AX=B
     * @param transA: if we need to transpose matrix A
     * @param dim: Dimension of matrix A
     * @param nrhs: Number of column in matrix B
     * @param matA: matrix A
     * @param offsetA: offset in the array of matrix A
     * @param ldA: number of rows in matA to jump to reach the second column
     * @param ipiv: The pivot indices from DGETRF;
     * @param offsetIpiv: Offset of IPIV
     * @param matB: matrix B -> it will become the result at the end.
     * @param offsetB: offset in the array of matrix B
     * @param ldB: number of rows in matB to jump to reach the second column
     * @param info: info[0]=0 -> successful exit
     */
    void dgetrs(KBlasTransposeType transA, int dim, int nrhs, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] matB, int offsetB, int ldB, int[] info);

    /**
     * To invert a matrix
     * @param dim: Dimension of matrix A
     * @param matA: MatrixA to invert -> the result will be stored here
     * @param offsetA: offset in the array of matrix A
     * @param ldA: number of rows in matA to jump to reach the second column
     * @param ipiv: The pivot indices from DGETRF;
     * @param offsetIpiv: Offset of IPIV
     * @param work: A workspace for invert
     * @param offsetWork: offset in the array of workspace
     * @param ldWork: number of rows in workspace to jump to reach the second column
     * @param info: info[0]=0 -> successful exit
     */
    void dgetri(int dim, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] work, int offsetWork, int ldWork, int[] info);

    /**
     * To factorize LU matrix
     * @param rows: rows of matrix A
     * @param columns: columns of matrix A
     * @param matA: MatrixA to invert -> the result will be stored here
     * @param offsetA: offset in the array of matrix A
     * @param ldA: number of rows in matA to jump to reach the second column
     * @param ipiv: The pivot indices from DGETRF;
     * @param offsetIpiv: Offset of IPIV
     * @param info: info[0]=0 -> successful exit
     */
    void dgetrf(int rows, int columns, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, int[] info);


    /**
     ** DORGQR generates an M-by-N real matrix Q with orthonormal columns,
     *  which is defined as the first N columns of a product of K elementary
     *  reflectors of order M
     * @param m  The number of rows of the matrix Q. M >= 0.
     * @param n  The number of columns of the matrix Q. M >= N >= 0.
     * @param k  The number of elementary reflectors whose product defines the matrix Q. N >= K >= 0.
     * @param matA (input/output) DOUBLE PRECISION array
     * @param offsetA offset in the array of matrix A
     * @param ldA  number of rows in matA to jump to reach the second column
     * @param taw (input) DOUBLE PRECISION array, dimension (K)
     *          TAU(i) must contain the scalar factor of the elementary
     *          reflector H(i), as returned by DGEQRF.
     * @param offsetTaw offset in the array of matrix Taw
     * @param work  (workspace/output) DOUBLE PRECISION array,
     * @param offsetWork offset in the array of matrix Work
     * @param lWork Size of matrix work
     * @param info info return
     */
    void dorgqr(int m, int n, int k, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetWork, int lWork, int[] info);

    /**
     * DGEQRF computes a QR factorization of a real M-by-N matrix A:  A = Q * R.
     * @param m  The number of rows of the matrix Q. M >= 0.
     * @param n  The number of columns of the matrix Q. M >= N >= 0.
     * @param matA (input/output) DOUBLE PRECISION array
     * @param offsetA offset in the array of matrix A
     * @param ldA  number of rows in matA to jump to reach the second column
     * @param taw (input) DOUBLE PRECISION array, dimension (K)
     *          TAU(i) must contain the scalar factor of the elementary
     *          reflector H(i), as returned by DGEQRF.
     * @param offsetTaw offset in the array of matrix Taw
     * @param work  (workspace/output) DOUBLE PRECISION array,
     * @param lWork Size of matrix work
     * @param info info return
     */
    void dgeqrf(int m, int n, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetwork, int lWork, int[] info);

    /**
     * To shutdown the blas library
     */
    void shutdown();

}
