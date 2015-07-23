package org.kevoree.modeling.util.maths.matrix;


public class MatrixFeatures {



    public static boolean isZeros( DenseMatrix64F m , double tol )
    {
        int length = m.getNumElements();

        for( int i = 0; i < length; i++ ) {
            if( Math.abs(m.getValueAtIndex(i)) > tol )
                return false;
        }
        return true;
    }


    public static boolean isVector( DenseMatrix64F mat ) {
        return (mat.getNumCols() == 1 || mat.getNumRows() == 1);
    }

    public static boolean isSquare( DenseMatrix64F mat ) {
        return mat.numCols == mat.numRows;
    }


    public static boolean isSymmetricDouble( DenseMatrix64F m , double tol ) {
        if( m.numCols != m.numRows )
            return false;

        double max = CommonOps.elementMaxAbs(m);

        for( int i = 0; i < m.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                double a = m.get(i,j)/max;
                double b = m.get(j,i)/max;

                double diff = Math.abs(a-b);

                if( !(diff <= tol) ) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean isSymmetric( DenseMatrix64F m ) {
        return isSymmetricDouble(m,0.0);
    }


    public static boolean isSkewSymmetric( DenseMatrix64F A , double tol ){
        if( A.numCols != A.numRows )
            return false;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                double a = A.get(i,j);
                double b = A.get(j,i);

                double diff = Math.abs(a+b);

                if( !(diff <= tol) ) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean isInverse( DenseMatrix64F a , DenseMatrix64F b , double tol ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        int numRows = a.numRows;
        int numCols = a.numCols;

        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                double total = 0;
                for( int k = 0; k < numCols; k++ ) {
                    total += a.get(i,k)*b.get(k,j);
                }

                if( i == j ) {
                    if( !(Math.abs(total-1) <= tol) )
                        return false;
                } else if( !(Math.abs(total) <= tol) )
                    return false;
            }
        }

        return true;
    }


    public static boolean isEqualsDouble( DenseMatrix64F a , DenseMatrix64F b , double tol )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        if( tol == 0.0 )
            return isEquals(a,b);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            if( !(tol >= Math.abs(a.getValueAtIndex(i) - b.getValueAtIndex(i))) ) {
                return false;
            }
        }
        return true;
    }


    public static boolean isEquals( DenseMatrix64F a, DenseMatrix64F b ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        final int length = a.getNumElements();
        for( int i = 0; i < length; i++ ) {
            if( !(a.getValueAtIndex(i) == b.getValueAtIndex(i)) ) {
                return false;
            }
        }

        return true;
    }

/*
    public static boolean isIdentical( DenseMatrix64F a, DenseMatrix64F b , double tol ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }
        if( tol < 0 )
            throw new RuntimeException("Tolerance must be greater than or equal to zero.");

        final int length = a.getNumElements();
        for( int i = 0; i < length; i++ ) {
            double valA = a.getValueAtIndex(i);
            double valB = b.getValueAtIndex(i);

            // if either is negative or positive infinity the result will be positive infinity
            // if either is NaN the result will be NaN
            double diff = Math.abs(valA-valB);

            // diff = NaN == false
            // diff = infinity == false
            if( tol >= diff )
                continue;

            if( Double.isNaN(valA) ) {
                return Double.isNaN(valB);
            } else if( Double.isInfinite(valA) ) {
                return valA == valB;
            } else {
                return false;
            }
        }

        return true;
    }*/


    public static boolean isIdentity( DenseMatrix64F mat , double tol )
    {
        // see if the result is an identity matrix
        int index = 0;
        for( int i = 0; i < mat.numRows; i++ ) {
            for( int j = 0; j < mat.numCols; j++ ) {
                if( i == j ) {
                    if( !(Math.abs(mat.getValueAtIndex(index++)-1) <= tol) )
                        return false;
                } else {
                    if( !(Math.abs(mat.getValueAtIndex(index++)) <= tol) )
                        return false;
                }
            }
        }

        return true;
    }


    public static boolean isConstantVal( DenseMatrix64F mat , double val , double tol )
    {
        // see if the result is an identity matrix
        int index = 0;
        for( int i = 0; i < mat.numRows; i++ ) {
            for( int j = 0; j < mat.numCols; j++ ) {
                if( !(Math.abs(mat.getValueAtIndex(index++)-val) <= tol) )
                    return false;

            }
        }

        return true;
    }


    public static boolean isDiagonalPositive( DenseMatrix64F a ) {
        for( int i = 0; i < a.numRows; i++ ) {
            if( !(a.get(i,i) >= 0) )
                return false;
        }
        return true;
    }


    public static boolean isFullRank( DenseMatrix64F a ) {
        throw new RuntimeException("Implement");
    }


    public static boolean isNegative(DenseMatrix64F a, DenseMatrix64F b, double tol) {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new RuntimeException("matrix dimensions must match");

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            if( !(Math.abs(a.getValueAtIndex(i) + b.getValueAtIndex(i)) <= tol) )
                return false;
        }

        return true;
    }


    public static boolean isUpperTriangle(DenseMatrix64F A , int hessenberg , double tol ) {
        if( A.numRows != A.numCols )
            return false;

        for( int i = hessenberg+1; i < A.numRows; i++ ) {
            for( int j = 0; j < i-hessenberg; j++ ) {
                if( !(Math.abs(A.get(i,j)) <= tol) ) {
                    return false;
                }
            }
        }
        return true;
    }


/*
    public static boolean isOrthogonal( DenseMatrix64F Q , double tol )
    {
        if( Q.numRows < Q.numCols ) {
            throw new RuntimeException("The number of rows must be more than or equal to the number of columns");
        }

        DenseMatrix64F u[] = CommonOps.columnsToVector(Q, null);

        for( int i = 0; i < u.length; i++ ) {
            DenseMatrix64F a = u[i];

            for( int j = i+1; j < u.length; j++ ) {
                double val = VectorVectorMult.innerProd(a,u[j]);

                if( !(Math.abs(val) <= tol))
                    return false;
            }
        }

        return true;
    }

    public static boolean isPositiveDefinite( DenseMatrix64F A ) {
        if( !isSquare(A))
            return false;

        CholeskyDecompositionInner_D64 chol = new CholeskyDecompositionInner_D64(true);
        if( chol.inputModified() )
            A = A.copy();

        return chol.decompose(A);
    }


    public static boolean isPositiveSemidefinite( DenseMatrix64F A ) {
        if( !isSquare(A))
            return false;

        EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(A.numCols,false);
        if( eig.inputModified() )
            A = A.copy();
        eig.decompose(A);

        for( int i = 0; i < A.numRows; i++ ) {
            Complex64F v = eig.getEigenvalue(i);

            if( v.getReal() < 0 )
                return false;
        }

        return true;
    }

    public static int rank( DenseMatrix64F A ) {
        return rank(A, UtilEjml.EPS*100);
    }



    public static boolean isRowsLinearIndependent( DenseMatrix64F A )
    {
        // LU decomposition
        LUDecomposition<DenseMatrix64F> lu = DecompositionFactory.lu(A.numRows,A.numCols);
        if( lu.inputModified() )
            A = A.copy();

        if( !lu.decompose(A))
            throw new RuntimeException("Decompositon failed?");

        // if they are linearly independent it should not be singular
        return !lu.isSingular();
    }

    public static int rank( DenseMatrix64F A , double threshold ) {
        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols,false,false,true);

        if( svd.inputModified() )
            A = A.copy();

        if( !svd.decompose(A) )
            throw new RuntimeException("Decomposition failed");

        return SingularOps.rank(svd, threshold);
    }

    public static int nullity( DenseMatrix64F A ) {
        return nullity(A, UtilEjml.EPS*100);
    }


    public static int nullity( DenseMatrix64F A , double threshold ) {
        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols,false,false,true);

        if( svd.inputModified() )
            A = A.copy();

        if( !svd.decompose(A) )
            throw new RuntimeException("Decomposition failed");

        return SingularOps.nullity(svd,threshold);
    }
    */
}