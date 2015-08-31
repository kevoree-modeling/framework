package org.kevoree.modeling.blas;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import com.github.fommil.netlib.BLAS;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;

public class NetlibBlas implements KBlas {
    private BLAS blas;

    public NetlibBlas() {
        blas = BLAS.getInstance();
    }

    @Override
    public void dscal(double alpha, KArray2D matA) {
        blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
    }

    @Override
    public void dgemm(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {
        blas.dgemm(transTypeToChar(transa), transTypeToChar(transb), matA.rows(), matB.columns(), matA.columns(), alpha, matA.data(), matA.rows(), matB.data(), matB.rows(), beta, matC.data(), matC.rows());
    }

    @Override
    public void shutdown() {
        blas = null;
    }

    private static final String TRANSPOSE_TYPE_CONJUCATE = "c";

    private static final String TRANSPOSE_TYPE_NOTRANSPOSE = "n";

    private static final String TRANSPOSE_TYPE_TRANSPOSE = "t";

    private static String transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUCATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return null;
    }

}
