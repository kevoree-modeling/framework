package org.kevoree.modeling.util.maths.structure.matrix;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class DenseLU {

    /**
     * Holds the LU factors
     */
    private KArray2D LU;

    /**
     * Row pivotations
     */
    private int[] piv;

    /**
     * True if the matrix was singular
     */
    private boolean singular;

    /**
     * Constructor for DenseLU
     *
     * @param m
     *            Number of rows
     * @param n
     *            Number of columns
     */
    public DenseLU(int m, int n) {
        LU = new NativeArray2D(m, n);
        piv = new int[Math.min(m, n)];
    }

    /**
     * Creates an LU decomposition of the given matrix
     *
     * @param A
     *            Matrix to decompose. Not modified
     * @return The current decomposition
     */
    public static DenseLU factorize(KArray2D A, KBlas blas) {
        return new DenseLU(A.rows(), A.columns()).factor(A,blas);
    }

    /**
     * Creates an LU decomposition of the given matrix
     *
     * @param A
     *            Matrix to decompose. Overwritten with the decomposition
     * @return The current decomposition
     */
    public DenseLU factor(KArray2D A, KBlas blas) {
        singular = false;

        int[] info = new int[1];
        info[0]=0;
        blas.dgetrf(A.rows(), A.columns(), A.data(), 0,A.rows(), piv,0, info);

        if (info[0] > 0)
            singular = true;
        else if (info[0] < 0)
            throw new IllegalArgumentException();

        LU.setData(A.data());

        return this;
    }


    /**
     * Returns the row pivots
     */
    public int[] getPivots() {
        return piv;
    }

    /**
     * Checks for singularity
     */
    public boolean isSingular() {
        return singular;
    }

    /**
     * Computes <code>A\B</code>, overwriting <code>B</code>
     */
    public KArray2D solve(KArray2D B, KBlas blas) {
        return solve(B, KBlasTransposeType.NOTRANSPOSE,blas);
    }

    /**
     * Computes <code>A<sup>T</sup>\B</code>, overwriting <code>B</code>
     */
    public KArray2D transSolve(KArray2D B, KBlas blas) {
        return solve(B, KBlasTransposeType.TRANSPOSE, blas);
    }

    private KArray2D solve(KArray2D B, KBlasTransposeType trans, KBlas blas) {
        if (singular) {
         //   throw new MatrixSingularException();
        }
        if (B.rows() != LU.rows())
            throw new IllegalArgumentException("B.numRows() != LU.numRows()");

        int[] info = new int[1];
        blas.dgetrs(trans, LU.rows(),
                B.columns(), LU.data(), 0,LU.rows(), piv,0,
                B.data(), 0,LU.rows(), info);

        if (info[0] < 0)
            throw new IllegalArgumentException();

        return B;
    }

}

