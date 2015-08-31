package org.kevoree.modeling.blas;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;

public class JCudaBlas implements KBlas {

    public JCudaBlas() {
        //Initialize the kernel
        JCublas.cublasInit();
    }

    @Override
    public void shutdown() {
        JCublas.cublasShutdown();
    }

    @Override
    public void dscal(double alpha, KArray2D matA) {

    }

    @Override
    public void dgemm(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {
        // Allocate memory on the device
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        JCublas.cublasAlloc(matA.rows() * matA.columns(), Sizeof.DOUBLE, d_A);
        JCublas.cublasAlloc(matB.rows() * matB.columns(), Sizeof.DOUBLE, d_B);
        JCublas.cublasAlloc(matC.rows() * matC.columns(), Sizeof.DOUBLE, d_C);

        // Copy the memory from the host to the device
        JCublas.cublasSetVector(matA.rows() * matA.columns(), Sizeof.DOUBLE, Pointer.to(matA.data()), 1, d_A, 1);
        JCublas.cublasSetVector(matB.rows() * matB.columns(), Sizeof.DOUBLE, Pointer.to(matB.data()), 1, d_B, 1);
        JCublas.cublasSetVector(matC.rows() * matC.columns(), Sizeof.DOUBLE, Pointer.to(matC.data()), 1, d_C, 1);

        // Execute sgemm
        JCublas.cublasDgemm(transTypeToChar(transa), transTypeToChar(transb), matA.rows(), matB.columns(), matA.columns(), alpha, d_A, matA.rows(), d_B, matB.rows(), beta, d_C, matC.rows());

        // Copy the result from the device to the host
        JCublas.cublasGetVector(matC.rows() * matC.columns(), Sizeof.DOUBLE, d_C, 1, Pointer.to(matC.data()), 1);

        // Clean up
        JCublas.cublasFree(d_A);
        JCublas.cublasFree(d_B);
        JCublas.cublasFree(d_C);
    }

    private static final char TRANSPOSE_TYPE_CONJUCATE = 'c';

    private static final char TRANSPOSE_TYPE_NOTRANSPOSE = 'n';

    private static final char TRANSPOSE_TYPE_TRANSPOSE = 't';

    private static char transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUCATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return '0';
    }

}
