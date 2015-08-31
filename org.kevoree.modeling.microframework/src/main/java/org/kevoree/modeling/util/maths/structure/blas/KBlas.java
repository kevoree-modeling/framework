package org.kevoree.modeling.util.maths.structure.blas;

import org.kevoree.modeling.util.maths.structure.KArray2D;

public interface KBlas {

    //Level 1 Blas
    //For matrix-matrix multiplication

    //matA := alpha* matA
    void dscal(double alpha,KArray2D matA);

    //Level 3 Blas
    //For matrix-matrix multiplication

    //matC := alpha*op(matA)*op(matB) + beta*matC
    //trans: 'n': matA -> matA (normal)
    //trans: 't': matA -> transpose(matA)
    //trans: 'c': matA -> conjugateTransp(matA)
    void dgemm(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta,  KArray2D matC);

    void trans(KArray2D matA, KArray2D result);

    void shutdown();

}
