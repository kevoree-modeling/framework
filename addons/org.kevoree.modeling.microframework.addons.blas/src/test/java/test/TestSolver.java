package test;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.blas.NetlibBlas;
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
        int dim=2000;
        int dim2=100;
        KArray2D matA= MatrixOperations.random(dim,dim);
        KArray2D matB= MatrixOperations.random(dim,dim2);
        long timestart, timeend;
        KBlas netblas=new NetlibBlas();
        KBlas javaBlas = new JavaBlas();

        timestart=System.currentTimeMillis();
        KArray2D matXnetlib=MatrixOperations.solve(matA,matB,false, KBlasTransposeType.NOTRANSPOSE,netblas);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib invert " + ((double) (timeend - timestart)) / 1000+" s");

        timestart=System.currentTimeMillis();
        KArray2D matXjava=MatrixOperations.solve(matA,matB,false, KBlasTransposeType.NOTRANSPOSE,javaBlas);
        timeend=System.currentTimeMillis();
        System.out.println("Java invert " + ((double) (timeend - timestart)) / 1000+" s");



        KArray2D matCnetlib= MatrixOperations.multiply(matA, matXnetlib, netblas);
        KArray2D matCjava= MatrixOperations.multiply(matA,matXjava,javaBlas);

        boolean test=true;
        int count=0;
        for (int i = 0; i < matCjava.rows(); i++) {
            for (int j = 0; j < matCjava.columns(); j++) {
                if(Math.abs(matCjava.get(i, j)-matB.get(i, j))> eps){
                    test =false;
                    count++;
                }

                if(Math.abs(matCnetlib.get(i, j)-matB.get(i, j))> eps){
                    test =false;
                    count++;
                }
            }
        }
        System.out.println("Error counts: "+count);
        Assert.assertTrue(test);
    }
}
