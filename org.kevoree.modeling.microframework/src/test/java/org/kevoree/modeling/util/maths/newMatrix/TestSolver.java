package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

/**
 * Created by assaad on 04/09/15.
 */
public class TestSolver {
    @Test
    public void solve(){
        double eps=1e-5;
        int dim=7;
        int dim2=4;
        KArray2D matA= MatrixOperations.random(dim,dim);
        KArray2D matB= MatrixOperations.random(dim,dim2);
        KBlas blas=new JavaBlas();
        KArray2D matX=MatrixOperations.solve(matA,matB,false, KBlasTransposeType.NOTRANSPOSE,blas);

        KArray2D matC= MatrixOperations.multiply(matA,matX,blas);

        boolean test=true;
        int count=0;
        for (int i = 0; i < matC.rows(); i++) {
            for (int j = 0; j < matC.columns(); j++) {
                if(Math.abs(matC.get(i, j)-matB.get(i, j))> eps){
                    test =false;
                    count++;
                }
            }
        }
        Assert.assertTrue(test);
    }
}
