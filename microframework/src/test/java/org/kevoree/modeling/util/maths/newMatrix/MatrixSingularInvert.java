package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

/**
 * Created by assaad on 04/09/15.
 */
public class MatrixSingularInvert {

    @Test
    public void invert(){

        int r=5;
        int[] dimA = {r, r};
        boolean rand = true;
        double eps = 1e-5;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        for(int i=0;i<r;i++){
            matA.set(1,i,matA.get(0,i)*2); //creating a singular non-invertible matrix
        }

        JavaBlas java = new JavaBlas();
        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0], dimA[1]);
        CommonOps.copyMatrix(matA, ejmlmatA);

        KArray2D res = MatrixOperations.invert(matA, java);
        SimpleMatrix resEjml = ejmlmatA.invert();

        NativeArray2D matB=new NativeArray2D(r,2);

        KArray2D matC = MatrixOperations.solve(matA,matB,false, KBlasTransposeType.NOTRANSPOSE,java);

    }
}
