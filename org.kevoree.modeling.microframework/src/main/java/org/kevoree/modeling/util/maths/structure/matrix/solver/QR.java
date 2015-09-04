package org.kevoree.modeling.util.maths.structure.matrix.solver;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class QR{

    /**
     * The orthogonal matrix
     */
    private  KArray2D Q;
    private  KArray2D R;

    /**
     * Factorisation sizes
     */
    int m, n, k;

    /**
     * Work arrays
     */
    double[] work, workGen;

    /**
     * Scales for the reflectors
     */
    double[] tau;

    /**
     * Constructs an empty QR decomposition
     *
     * @param rows
     *            Number of rows. Must be larger than or equal the number of
     *            columns
     * @param columns
     *            Number of columns
     */
    public QR(int rows, int columns) {
        if (columns > rows)
            throw new RuntimeException("n > m");

        this.m = rows;
        this.n = columns;
        this.k = Math.min(m, n);
        tau = new double[k];
        R = new NativeArray2D(m, n);

    }

    /**
     * Convenience method to compute a QR decomposition
     *
     * @param A
     *            Matrix to decompose. Not modified
     * @return Newly allocated decomposition
     */
    public static QR factorize(KArray2D A, boolean workInPlace, KBlas blas) {
        return new QR(A.rows(), A.columns()).factor(A,workInPlace, blas);
    }


    public QR factor(KArray2D matA, boolean workInPlace, KBlas blas) {
        KArray2D A;
        if(!workInPlace){
            A=new NativeArray2D(matA.rows(),matA.columns());
            A.setData(matA.data().clone());
        }
        else {
            A=matA;
        }

        int lwork;

        // Query optimal workspace. First for computing the factorization
        {
            work = new double[1];
            int[] info = new int[1];
            info[0]=0;
            blas.dgeqrf(m, n, new double[0],0, m,
                    new double[0],0, work,0, -1, info);

            if (info[0] != 0)
                lwork = n;
            else
                lwork = (int) work[0];
            lwork = Math.max(1, lwork);
            work = new double[lwork];
        }

        // Workspace needed for generating an explicit orthogonal matrix
        {
            workGen = new double[1];
            int[] info = new int[1];
            info[0]=0;
            blas.dorgqr(m, n, k, new double[0],0,m,new double[0], 0,workGen,0, -1, info);

            if (info[0] != 0)
                lwork = n;
            else
                lwork = (int) workGen[0];
            lwork = Math.max(1, lwork);
            workGen = new double[lwork];
        }

        /*
         * Calculate factorisation, and extract the triangular factor
         */
        int[] info = new int[1];
        info[0]=0;
        blas.dgeqrf(m, n, A.data(),0, m, tau,0,work,0, work.length, info);

        if (info[0] < 0)
            throw new RuntimeException(""+info[0]);

        for(int col=0;col<A.columns();col++){
            for(int row=0;row<=col;row++){
                R.set(row,col,A.get(row,col));
            }
        }

        /*
         * Generate the orthogonal matrix
         */
        info[0] = 0;
        blas.dorgqr(m, n, k, A.data(),0, m, tau,0,workGen, 0, workGen.length, info);

        if (info[0] < 0)
            throw new RuntimeException();

        Q=A;

        return this;
    }

    /**
     * Returns the upper triangular factor
     */
    public KArray2D getR() {
        return R;
    }

    public KArray2D getQ() {
        return Q;
    }
}

