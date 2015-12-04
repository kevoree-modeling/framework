package org.kevoree.modeling.util.maths.matrix.solvers;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class SimpleSVD<T extends SimpleMatrix> {

    private static void swapRowOrCol(DenseMatrix64F M, boolean tran, int i, int bigIndex) {
        double tmp;
        if( tran ) {
            // swap the rows
            for( int col = 0; col < M.numCols; col++ ) {
                tmp = M.get(i,col);
                M.set(i,col,M.get(bigIndex,col));
                M.set(bigIndex,col,tmp);
            }
        } else {
            // swap the columns
            for( int row = 0; row < M.numRows; row++ ) {
                tmp = M.get(row,i);
                M.set(row,i,M.get(row,bigIndex));
                M.set(row,bigIndex,tmp);
            }
        }
    }

    public static double singularThreshold( SvdImplicitQrDecompose_D64 svd ) {
        double largest = 0;
        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        for( int j = 0; j < N; j++ ) {
            if( w[j] > largest)
                largest = w[j];
        }

        int M = Math.max(svd.getNumCols(),svd.getNumRows());
        return M*largest* CommonOps.EPS;
    }

    public static void descendingOrder( DenseMatrix64F U , boolean tranU ,
                                        DenseMatrix64F W ,
                                        DenseMatrix64F V , boolean tranV )
    {
        int numSingular = Math.min(W.numRows,W.numCols);

     //   checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < numSingular; i++ ) {
            double bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < numSingular; j++ ) {
                double v = W.get(j,j);

                if( v > bigValue ) {
                    bigValue = v;
                    bigIndex = j;
                }
            }

            // only swap if the current index is not the smallest
            if( bigIndex == i)
                continue;

            if( bigIndex == -1 ) {
                // there is at least one uncountable singular value.  just stop here
                break;
            }

            double tmp = W.get(i,i);
            W.set(i,i,bigValue);
            W.set(bigIndex,bigIndex,tmp);

            if( V != null ) {
                swapRowOrCol(V, tranV, i, bigIndex);
            }

            if( U != null ) {
                swapRowOrCol(U, tranU, i, bigIndex);
            }
        }
    }


    private SvdImplicitQrDecompose_D64 svd;
    private T U;
    private T W;
    private T V;

    private DenseMatrix64F mat;

    // tolerance for singular values
    double tol;


    public SimpleSVD( DenseMatrix64F mat , boolean compact ) {
        this.mat = mat;
        svd=new SvdImplicitQrDecompose_D64(compact,true,true,false);
        if( !svd.decompose(mat) )
            throw new RuntimeException("Decomposition failed");
        U = (T)SimpleMatrix.wrap(svd.getU(null,false));
        W = (T)SimpleMatrix.wrap(svd.getW(null));
        V = (T)SimpleMatrix.wrap(svd.getV(null,false));

        // order singular values from largest to smallest
        descendingOrder(U.getMatrix(),false,W.getMatrix(),V.getMatrix(),false);

        tol = singularThreshold(svd);

    }


    public T getU() {
        return U;
    }

    public T getW() {
        return W;
    }

    public T getV() {
        return V;
    }

/*
    public double quality() {
        return DecompositionFactory.quality(mat,U.getMatrix(),W.getMatrix(),V.transpose().getMatrix());
    }


    public SimpleMatrix nullSpace() {
        return SimpleMatrix.wrap(SingularOps.nullSpace(svd,null,tol));
    }

    public double getSingleValue( int index ) {
        return W.get(index,index);
    }


    public int rank() {
        return SingularOps.rank(svd,tol);
    }


    public int nullity() {
        return SingularOps.nullity(svd,10.0*UtilEjml.EPS);
    }

    public SingularValueDecomposition getSVD() {
        return svd;
    }*/
}