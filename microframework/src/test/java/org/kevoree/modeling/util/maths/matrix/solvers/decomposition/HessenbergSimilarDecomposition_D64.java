package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class HessenbergSimilarDecomposition_D64{
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private DenseMatrix64F QH;
    // number of rows and columns of the matrix being decompose
    private int N;

    // the first element in the orthogonal vectors
    private double gammas[];
    // temporary storage
    private double b[];
    private double u[];


    public HessenbergSimilarDecomposition_D64(int initialSize) {
        gammas = new double[ initialSize ];
        b = new double[ initialSize ];
        u = new double[ initialSize ];
    }




    public boolean decompose( DenseMatrix64F A )
    {
        if( A.numRows != A.numCols )
            throw new RuntimeException("A must be square.");
        if( A.numRows <= 0 )
            return false;

        QH = A;

        N = A.numCols;

        if( b.length < N ) {
            b = new double[ N ];
            gammas = new double[ N ];
            u = new double[ N ];
        }
        return _decompose();
    }


    public boolean inputModified() {
        return true;
    }


    public DenseMatrix64F getQH() {
        return QH;
    }


    public DenseMatrix64F getH( DenseMatrix64F H ) {
        if( H == null ) {
            H = new DenseMatrix64F(N,N);
        }
        else
            H.zero();

        // copy the first row
        System.arraycopy(QH.data, 0, H.data, 0, N);

        for( int i = 1; i < N; i++ ) {
            for( int j = i-1; j < N; j++ ) {
                H.set(i,j, QH.get(i,j));
            }
        }

        return H;
    }

    /**
     * An orthogonal matrix that has the following property: H = Q<sup>T</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public DenseMatrix64F getQ( DenseMatrix64F Q ) {
        if( Q == null ) {
            Q = new DenseMatrix64F(N,N);
            for( int i = 0; i < N; i++ ) {
                Q.data[i*N+i] = 1;
            }
        } else if( N != Q.numRows || N != Q.numCols )
            throw new RuntimeException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CommonOps.setIdentity(Q);

        for( int j = N-2; j >= 0; j-- ) {
            u[j+1] = 1;
            for( int i = j+2; i < N; i++ ) {
                u[i] = QH.get(i,j);
            }
            QrHelperFunctions_D64.rank1UpdateMultR(Q, u, gammas[j], j + 1, j + 1, N, b);
        }

        return Q;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        double h[] = QH.data;

        for( int k = 0; k < N-2; k++ ) {
            // find the largest value in this column
            // this is used to normalize the column and mitigate overflow/underflow
            double max = 0;

            for( int i = k+1; i < N; i++ ) {
                // copy the householder vector to vector outside of the matrix to reduce caching issues
                // big improvement on larger matrices and a relatively small performance hit on small matrices.
                double val = u[i] = h[i*N+k];
                val = Math.abs(val);
                if( val > max )
                    max = val;
            }

            if( max > 0 ) {
                // -------- set up the reflector Q_k

                double tau = 0;
                // normalize to reduce overflow/underflow
                // and compute tau for the reflector
                for( int i = k+1; i < N; i++ ) {
                    double val = u[i] /= max;
                    tau += val*val;
                }

                tau = Math.sqrt(tau);

                if( u[k+1] < 0 )
                    tau = -tau;

                // write the reflector into the lower left column of the matrix
                double nu = u[k+1] + tau;
                u[k+1] = 1.0;

                for( int i = k+2; i < N; i++ ) {
                    h[i*N+k] = u[i] /= nu;
                }

                double gamma = nu/tau;
                gammas[k] = gamma;

                // ---------- multiply on the left by Q_k
                QrHelperFunctions_D64.rank1UpdateMultR(QH, u, gamma, k + 1, k + 1, N, b);

                // ---------- multiply on the right by Q_k
                QrHelperFunctions_D64.rank1UpdateMultL(QH, u, gamma, 0, k + 1, N);

                // since the first element in the householder vector is known to be 1
                // store the full upper hessenberg
                h[(k+1)*N+k] = -tau*max;

            } else {
                gammas[k] = 0;
            }

        }

        return true;
    }

    public double[] getGammas() {
        return gammas;
    }
}