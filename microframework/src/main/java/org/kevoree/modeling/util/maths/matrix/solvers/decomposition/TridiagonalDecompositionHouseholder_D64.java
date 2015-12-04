package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class TridiagonalDecompositionHouseholder_D64 {

    /**
     * Only the upper right triangle is used.  The Tridiagonal portion stores
     * the tridiagonal matrix.  The rows store householder vectors.
     */
    private DenseMatrix64F QT;

    // The size of the matrix
    private int N;

    // temporary storage
    private double w[];
    // gammas for the householder operations
    private double gammas[];
    // temporary storage
    private double b[];

    public TridiagonalDecompositionHouseholder_D64() {
        N = 1;
        w = new double[N];
        b = new double[N];
        gammas = new double[N];
    }

    /**
     * Returns the internal matrix where the decomposed results are stored.
     * @return
     */
    public DenseMatrix64F getQT() {
        return QT;
    }

    public void getDiagonal(double[] diag, double[] off) {
        for( int i = 0; i < N; i++ ) {
            diag[i] = QT.data[i*N+i];

            if( i+1 < N ) {
                off[i] = QT.data[i*N+i+1];
            }
        }
    }


    public DenseMatrix64F getT( DenseMatrix64F T ) {
        if( T == null ) {
            T = new DenseMatrix64F(N,N);
        } else if( N != T.numRows || N != T.numCols )
            throw new RuntimeException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            T.zero();


        T.data[0] = QT.data[0];

        for( int i = 1; i < N; i++ ) {
            T.set(i,i, QT.get(i,i));
            double a = QT.get(i-1,i);
            T.set(i-1,i,a);
            T.set(i,i-1,a);
        }

        if( N > 1 ) {
            T.data[(N-1)*N+N-1] = QT.data[(N-1)*N+N-1];
            T.data[(N-1)*N+N-2] = QT.data[(N-2)*N+N-1];
        }

        return T;
    }


    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean transposed ) {
        if( Q == null ) {
            Q = CommonOps.identity1D(N);
        } else if( N != Q.numRows || N != Q.numCols )
            throw new RuntimeException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CommonOps.setIdentity(Q);

        for( int i = 0; i < N; i++ ) w[i] = 0;

        if( transposed ) {
            for( int j = N-2; j >= 0; j-- ) {
                w[j+1] = 1;
                for( int i = j+2; i < N; i++ ) {
                    w[i] = QT.data[j*N+i];
                }
                QrHelperFunctions_D64.rank1UpdateMultL(Q, w, gammas[j + 1], j + 1, j + 1, N);
            }
        } else {
            for( int j = N-2; j >= 0; j-- ) {
                w[j+1] = 1;
                for( int i = j+2; i < N; i++ ) {
                    w[i] = QT.get(j,i);
                }
                QrHelperFunctions_D64.rank1UpdateMultR(Q, w, gammas[j + 1], j + 1, j + 1, N, b);
            }
        }

        return Q;
    }


    public boolean decompose( DenseMatrix64F A ) {
        init(A);

        for( int k = 1; k < N; k++ ) {
            similarTransform(k);
        }

        return true;
    }


    private void similarTransform( int k) {
        double t[] = QT.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        int rowU = (k-1)*N;

        for( int i = k; i < N; i++ ) {
            double val = Math.abs(t[rowU+i]);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = QrHelperFunctions_D64.computeTauAndDivide(k, N, t, rowU, max);

            // write the reflector into the lower left column of the matrix
            double nu = t[rowU+k] + tau;
            QrHelperFunctions_D64.divideElements(k + 1, N, t, rowU, nu);
            t[rowU+k] = 1.0;

            double gamma = nu/tau;
            gammas[k] = gamma;

            // ---------- Specialized householder that takes advantage of the symmetry
            householderSymmetric(k,gamma);

            // since the first element in the householder vector is known to be 1
            // store the full upper hessenberg
            t[rowU+k] = -tau*max;
        } else {
            gammas[k] = 0;
        }
    }

    public void householderSymmetric( int row , double gamma )
    {
        int startU = (row-1)*N;

        // compute v = -gamma*A*u
        for( int i = row; i < N; i++ ) {
            double total = 0;
            // the lower triangle is not written to so it needs to traverse upwards
            // to get the information.  Reduces the number of matrix writes need
            // improving large matrix performance
            for( int j = row; j < i; j++ ) {
                total += QT.data[j*N+i]*QT.data[startU+j];
            }
            for( int j = i; j < N; j++ ) {
                total += QT.data[i*N+j]*QT.data[startU+j];
            }
            w[i] = -gamma*total;
        }
        // alpha = -0.5*gamma*u^T*v
        double alpha = 0;

        for( int i = row; i < N; i++ ) {
            alpha += QT.data[startU+i]*w[i];
        }
        alpha *= -0.5*gamma;

        // w = v + alpha*u
        for( int i = row; i < N; i++ ) {
            w[i] += alpha*QT.data[startU+i];
        }
        // A = A + w*u^T + u*w^T
        for( int i = row; i < N; i++ ) {

            double ww = w[i];
            double uu = QT.data[startU+i];

            int rowA = i*N;
            for( int j = i; j < N; j++ ) {
                // only write to the upper portion of the matrix
                // this reduces the number of cache misses
                QT.data[rowA+j] += ww*QT.data[startU+j] + w[j]*uu;
            }
        }

    }



    public void init( DenseMatrix64F A ) {
        if( A.numRows != A.numCols)
            throw new RuntimeException("Must be square");

        if( A.numCols != N ) {
            N = A.numCols;

            if( w.length < N ) {
                w = new double[ N ];
                gammas = new double[N];
                b = new double[N];
            }
        }

        QT = A;
    }


    public boolean inputModified() {
        return true;
    }
}