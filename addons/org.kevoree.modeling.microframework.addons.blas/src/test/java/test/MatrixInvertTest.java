package test;

import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;

/**
 * Created by assaad on 03/09/15.
 */
public class MatrixInvertTest {
    @Test
    public void invertMatrix() {
        int r = 1000;
        int[] dimA = {r, r};
        boolean rand = true;
        double eps = 1e-7;

        NativeArray2D matA = new NativeArray2D(dimA[0], dimA[1]);
        MatrixOperations.initMatrice(matA, rand);
        JavaBlas java = new JavaBlas();
        JavaBlas java = new JavaBlas();

        SimpleMatrix ejmlmatA = new SimpleMatrix(dimA[0], dimA[1]);
        MatrixOperations.copyMatrix(matA, ejmlmatA);

        long timestart, timeend;

        timestart = System.currentTimeMillis();
        KArray2D res = MatrixOperations.invert(matA, java);
        timeend = System.currentTimeMillis();
        System.out.println("java blas invert " + ((double) (timeend - timestart)) / 1000);
    }
}
