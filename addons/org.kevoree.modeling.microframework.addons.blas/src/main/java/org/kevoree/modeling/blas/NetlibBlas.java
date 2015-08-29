package org.kevoree.modeling.blas;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import com.github.fommil.netlib.BLAS;

public class NetlibBlas implements KBlas {
    private BLAS blas;

    public NetlibBlas(){
        blas=BLAS.getInstance();
    }

    @Override
    public void dscal(double alpha, KArray2D matA) {
        blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
    }

    @Override
    public void dgemm(char transa, char transb, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {
        blas.dgemm(String.valueOf(transa), String.valueOf(transb), matA.rows(), matB.columns(), matA.columns(), alpha, matA.data(), matA.rows(), matB.data(), matB.rows(), beta, matC.data(), matC.rows());
    }

    @Override
    public void shutdown() {
        blas=null;
    }


}
