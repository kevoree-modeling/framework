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

    public KArray2D getLU(){
        return LU;
    }

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
            throw new RuntimeException();

        LU.setData(A.data());

        return this;
    }


    public KArray2D getLower()
    {
        int numRows = LU.rows();
        int numCols = LU.rows() < LU.columns() ? LU.rows() : LU.columns();
        NativeArray2D lower = new NativeArray2D(numRows,numCols);


        for( int i = 0; i < numCols; i++ ) {
            lower.set(i,i,1.0);

            for( int j = 0; j < i; j++ ) {
                lower.set(i,j, LU.get(i,j));
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    lower.set(i,j, LU.get(i,j));
                }
            }
        }
        return lower;
    }


    public KArray2D getUpper()
    {
        int numRows = LU.rows() < LU.columns() ? LU.rows() : LU.columns();
        int numCols = LU.columns();

        KArray2D upper = new NativeArray2D(numRows, numCols);


        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                upper.set(i,j, LU.get(i,j));
            }
        }

        return upper;
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
        return transSolve(B, KBlasTransposeType.NOTRANSPOSE, blas);
    }




    public KArray2D transSolve(KArray2D B, KBlasTransposeType trans, KBlas blas) {
        if (singular) {
         //   throw new MatrixSingularException();
        }
        if (B.rows() != LU.rows())
            throw new RuntimeException("B.numRows() != LU.numRows()");

        int[] info = new int[1];
        blas.dgetrs(trans, LU.rows(),
                B.columns(), LU.data(), 0,LU.rows(), piv,0,
                B.data(), 0,LU.rows(), info);

        if (info[0] < 0)
            throw new RuntimeException();

        return B;
    }

    public boolean invert(KArray2D A, KBlas blas) {
        int[] info = new int[1];
        info[0]=0;
        blas.dgetrf(A.rows(), A.columns(), A.data(), 0,A.rows(), piv,0, info);

       /* System.out.println("After f");
        for(int i=0;i<A.rows()*A.columns(); i++){
            System.out.print(A.getAtIndex(i)+" ");
        }
        System.out.println();

        System.out.println("PIV");
        for(int i=0;i<piv.length; i++){
            System.out.print(piv[i]+" ");
        }
        System.out.println();*/

        if (info[0] > 0)
            singular = true;
        else if (info[0] < 0)
            throw new RuntimeException();

        int lwork = A.rows()*A.rows();
        double[] work = new double[lwork];
        for(int i=0;i<lwork;i++){
            work[i]=0;
        }

        blas.dgetri(A.rows(),A.data(),0,A.rows(),piv,0,work,0,lwork,info);

        if(info[0]!=0){
            return false;
        }
        else {
            return true;
        }

    }
}

