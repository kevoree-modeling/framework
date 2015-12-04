package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Test;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.solver.LU;

/**
 * Created by assaad on 02/09/15.
 */
public class LUTest {
    @Test
    public void testLUFactorize(){
        int r=2;
        int[] dimA = {r, r};

        KBlas java = new JavaBlas();

        NativeArray2D A=new NativeArray2D(dimA[0],dimA[1]);
        A.set(0, 0, 3);
        A.set(0, 1, 1);
        A.set(1, 0, -6);
        A.set(1, 1, -4);

        LU dlu = new LU(dimA[0],dimA[1]);
        dlu.factor(A, java);

        KArray2D res= dlu.getLU();

       // res=dlu.getLower();
      //  res=dlu.getUpper();
       // System.out.println("done");

  /*      DenseMatrix64F ej=new DenseMatrix64F(dimA[0],dimA[1]);
        ej.set(0,0,3);
        ej.set(0,1,1);
        ej.set(1, 0, -6);
        ej.set(1, 1, -4);

        LUDecompositionAlt_D64 ludec = new LUDecompositionAlt_D64();
        ludec.decompose(ej);
        DenseMatrix64F luejml = ludec.getLU();*/

      //  System.out.println("done");
        //todo add assert here
    }

}
