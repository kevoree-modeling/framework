package org.kevoree.modeling.util.maths.matrix;

import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.matrix.solvers.LUDecompositionAlt_D64;
import org.kevoree.modeling.util.maths.matrix.solvers.LinearSolverLu_D64;

import java.util.Arrays;

public class CommonOps {

    public static int BLOCK_WIDTH = 60;
    public static int TRANSPOSE_SWITCH = 375;
    public static int MULT_COLUMN_SWITCH = 15;
    //public static int MULT_TRANAB_COLUMN_SWITCH = 40;
   // public static int MULT_INNER_SWITCH = 100;
    public static double EPS = Math.pow(2,-52);

/*
    public static final double TOL32 = 1e-4;
    public static final double TOL64 = 1e-8;
    public static int CMULT_COLUMN_SWITCH = 7;
    public static int SWITCH_BLOCK64_CHOLESKY = 1000;
    public static int SWITCH_BLOCK64_QR = 1500;
    public static int BLOCK_WIDTH_CHOL = 20;
    public static int BLOCK_SIZE = BLOCK_WIDTH*BLOCK_WIDTH;
*/

    public static void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            MatrixVectorMult.mult(a, b, c);
        } else if( b.numCols >= MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.mult_reorder(a,b,c);
        } else {
            MatrixMatrixMult.mult_small(a,b,c);
        }
    }

    public static void memset( double[] data , double val ) {
        for( int i = 0; i < data.length; i++ ) {
            data[i] = val;
        }
    }

    /*
    public static void multalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols >= MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.mult_reorderalpha(alpha, a, b, c);
        } else {
            MatrixMatrixMult.mult_smallalpha(alpha, a, b, c);
        }
    }
*/

    public static void multTransA( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            if( a.numCols >= MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= MULT_COLUMN_SWITCH ||
                b.numCols >= MULT_COLUMN_SWITCH  ) {
            MatrixMatrixMult.multTransA_reorder(a,b,c);
        } else {
            MatrixMatrixMult.multTransA_small(a,b,c);
        }
    }

/*
    public static void multTransalphaA( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols >= MULT_COLUMN_SWITCH ||
                b.numCols >= MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransA_reorderalpha(alpha, a, b, c);
        } else {
            MatrixMatrixMult.multTransA_smallalpha(alpha, a, b, c);
        }
    }
*/

    public static void multTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1 ) {
            MatrixVectorMult.mult(a, b, c);
        } else {
            MatrixMatrixMult.multTransB(a, b, c);
        }
    }

/*
    public static void multTransalphaB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        MatrixMatrixMult.multTransBalpha(alpha, a, b, c);
    }


    public static void multTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransAB_aux(a, b, c, null);
        } else {
            MatrixMatrixMult.multTransAB(a, b, c);
        }
    }


    public static void multTransalphaAB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols >= MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransAB_auxalpha(alpha, a, b, c, null);
        } else {
            MatrixMatrixMult.multTransABalpha(alpha, a, b, c);
        }
    }


    public static double dot( DenseMatrix64F a , DenseMatrix64F b ) {
         return VectorVectorMult.innerProd(a,b);
    }


    public static void multInner( DenseMatrix64F a , DenseMatrix64F c )
    {

        if( a.numCols >= MULT_INNER_SWITCH ) {
            MatrixMultProduct.inner_small(a, c);
        } else {
            MatrixMultProduct.inner_reorder(a, c);
        }
    }


    public static void multOuter( DenseMatrix64F a , DenseMatrix64F c )
    {
          MatrixMultProduct.outer(a, c);
    }


    public static void multAdd( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            MatrixVectorMult.multAdd(a, b, c);
        } else {
            if( b.numCols >= MULT_COLUMN_SWITCH ) {
                MatrixMatrixMult.multAdd_reorder(a, b, c);
            } else {
                MatrixMatrixMult.multAdd_small(a, b, c);
            }
        }
    }


    public static void multAddalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols >= MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAdd_reorderalpha(alpha, a, b, c);
        } else {
            MatrixMatrixMult.multAdd_smallalpha(alpha, a, b, c);
        }
    }


    public static void multAddTransA( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            if( a.numCols >= MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multAddTransA_small(a,b,c);
            }
        } else {
            if( a.numCols >= MULT_COLUMN_SWITCH ||
                    b.numCols >= MULT_COLUMN_SWITCH  ) {
                MatrixMatrixMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixMatrixMult.multAddTransA_small(a,b,c);
            }
        }
    }


    public static void multAddTransAalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= MULT_COLUMN_SWITCH ||
                b.numCols >= MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransA_reorderalpha(alpha, a, b, c);
        } else {
            MatrixMatrixMult.multAddTransA_smallalpha(alpha, a, b, c);
        }
    }


    public static void multAddTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        MatrixMatrixMult.multAddTransB(a, b, c);
    }

    public static void multAddTransBalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        MatrixMatrixMult.multAddTransBalpha(alpha, a, b, c);
    }


    public static void multAddTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1 ) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multAddTransA_small(a,b,c);
            }
        } else if( a.numCols >= MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransAB_aux(a,b,c,null);
        } else {
            MatrixMatrixMult.multAddTransAB(a,b,c);
        }
    }
 */

    public static void subvector(DenseMatrix64F A, int rowA, int colA, int length , boolean row, int offsetV, DenseMatrix64F v) {
        if( row ) {
            for( int i = 0; i < length; i++ ) {
                v.setValueAtIndex(offsetV + i, A.get(rowA, colA + i));
            }
        } else {
            for( int i = 0; i < length; i++ ) {
                v.setValueAtIndex( offsetV +i , A.get(rowA+i,colA));
            }
        }
    }

    public static SimpleMatrix abs(SimpleMatrix matrix) {
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++) {
                matrix.setValue2D(i, j, Math.abs(matrix.getValue2D(i, j)));
            }
        }
        return matrix;
    }

    /*
    public static SimpleMatrix elemSqrt(SimpleMatrix matrix) {
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++) {
                matrix.setValue2D(i, j, Math.sqrt(matrix.getValue2D(i, j)));
            }
        }
        return matrix;
    }

    public static SimpleMatrix elemPow(SimpleMatrix matrix, double p) {
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++) {
                matrix.setValue2D(i, j, Math.pow(matrix.getValue2D(i, j), p));
            }
        }
        return matrix;
    }

    public static SimpleMatrix deleteElementsFromVector(SimpleMatrix vector, double[] elements, int vectorSize) {
        SimpleMatrix newVector = new SimpleMatrix(vectorSize, 1);
        int j = 0;
        for (int i = 0; i < vector.numRows(); i++)
            if (elements[i] == 1)
                newVector.setValue2D(j++, 0, vector.getValue1D(i));
        return newVector;
    }

    public static SimpleMatrix ones(int rows, int cols) {
        SimpleMatrix matrix = new SimpleMatrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                matrix.setValue2D(i, j, 1);
            }
        }
        return matrix;
    }

    public static SimpleMatrix doubleListToMatrix(double[] valueList) {
        SimpleMatrix m = new SimpleMatrix(1, valueList.length);
        for (int i = 0; i < valueList.length; i++)
            m.setValue2D(0, i, valueList[i]);
        return m;
    }

    public static double[] setNegativeValuesToZero(double[] valueList) {
        for (int i = 0; i < valueList.length; i++) {
            if (valueList[i] < 0)
                valueList[i]=0;
        }
        return valueList;
    }

    public static double maxVectorElement(SimpleMatrix matrix){
        double d = PrimitiveHelper.DOUBLE_MIN_VALUE();
        for (int i = 0; i < matrix.numRows(); i++) {
            if(matrix.getValue2D(i, 0)>d)
                d = matrix.getValue2D(i, 0);
        }
        return d;
    }
    public static int maxVectorElementIndex(SimpleMatrix matrix){
        double d = PrimitiveHelper.DOUBLE_MIN_VALUE();
        int row = 0;
        for (int i = 0; i < matrix.numRows(); i++) {
            if(matrix.getValue2D(i, 0)>d){
                d = matrix.getValue2D(i,0);
                row = i;
            }
        }
        return row;
    }

    public static void multAddTransABalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols >= MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransAB_auxalpha(alpha, a, b, c, null);
        } else {
            MatrixMatrixMult.multAddTransABalpha(alpha, a, b, c);
        }
    }
*/

    public static double trace( DenseMatrix64F a ) {
        int N = Math.min(a.numRows,a.numCols);
        double sum = 0;
        int index = 0;
        for( int i = 0; i < N; i++ ) {
            sum += a.getValueAtIndex(index);
            index += 1 + a.numCols;
        }

        return sum;
    }

    public static DenseMatrix64F transposeMatrix( DenseMatrix64F A, DenseMatrix64F A_tran)
    {
        if( A_tran == null ) {
            A_tran = new DenseMatrix64F(A.numCols,A.numRows);
        } else {
            if( A.numRows != A_tran.numCols || A.numCols != A_tran.numRows ) {
                throw new RuntimeException("Incompatible matrix dimensions");
            }
        }
        if( A.numRows > TRANSPOSE_SWITCH &&
                A.numCols > TRANSPOSE_SWITCH )
            TransposeAlgs.block(A,A_tran,BLOCK_WIDTH);
        else
            TransposeAlgs.standard(A,A_tran);

        return A_tran;
    }

    public static void transpose( DenseMatrix64F mat ) {
        if( mat.numCols == mat.numRows ){
            TransposeAlgs.square(mat);
        } else {
            DenseMatrix64F b = new DenseMatrix64F(mat.numCols,mat.numRows);
            transposeMatrix(mat,b);
            mat.setMatrix(b);
        }
    }


/*
  public static DenseMatrix64F rref( DenseMatrix64F A , int numUnknowns, DenseMatrix64F reduced ) {
        if( reduced == null ) {
            reduced = new DenseMatrix64F(A.getNumRows,A.getNumCols);
        } else if( reduced.getNumCols != A.getNumCols || reduced.getNumRows != A.getNumRows )
            throw new RuntimeException("'re' must have the same shape as the original input matrix");

        if( numUnknowns <= 0 )
            numUnknowns = Math.min(A.getNumCols,A.getNumRows);

        ReducedRowEchelonForm<DenseMatrix64F> alg = new RrefGaussJordanRowPivot();
        alg.setTolerance(elementMaxAbs(A)* UtilEjml.EPS*Math.max(A.getNumRows,A.getNumCols));

        reduced.setPrimitiveType(A);
        alg.reduce(reduced, numUnknowns);

        return reduced;
    }

    public static boolean solve( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F x )
    {
        LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.general(a.getNumRows,a.getNumCols);

        // make sure the inputs 'a' and 'b' are not modified
        solver = new LinearSolverSafe<DenseMatrix64F>(solver);

        if( !solver.setA(a) )
            return false;

        solver.solve(b,x);
        return true;
    }











    */


    public static double det( DenseMatrix64F mat )
    {

        int numCol = mat.getNumCols();
        int numRow = mat.getNumRows();

        if( numCol != numRow ) {
            throw new RuntimeException("Must be a square matrix.");
        } else if( numCol <= 1 ) {
            return mat.getValueAtIndex(0);
        } else {
            LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            if( !alg.decompose(mat) )
                return 0.0;
            return alg.computeDeterminant();
        }
    }


    public static boolean invert( DenseMatrix64F mat, DenseMatrix64F result ) {
        LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
        LinearSolverLu_D64 solver = new LinearSolverLu_D64(alg);
        if (solver.modifiesA())
            mat = mat.copy();

        if (!solver.setA(mat))
            return false;
        solver.invert(result);
        return true;
    }


   /* public static boolean pinv( DenseMatrix64F A , DenseMatrix64F invA )
    {
        LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.pseudoInverse(true);
        if( solver.modifiesA())
            A = A.copy();

        if( !solver.setA(A) )
            return false;

        solver.invert(invA);
        return true;
    }*/


    public static void extractImpl(DenseMatrix64F src,
                               int srcY0, int srcX0,
                               DenseMatrix64F dst,
                               int dstY0, int dstX0,
                               int numRows, int numCols)
    {
        for( int y = 0; y < numRows; y++ ) {
            int indexSrc = src.getIndex(y+srcY0,srcX0);
            int indexDst = dst.getIndex(y+dstY0,dstX0);
            System.arraycopy(src.data,indexSrc,dst.data,indexDst, numCols);
        }
    }


    public static void extractInsert( DenseMatrix64F src,
                                int srcY0, int srcY1,
                                int srcX0, int srcX1,
                                      DenseMatrix64F dst ,
                                int dstY0, int dstX0 )
    {

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;
        extractImpl(src, srcY0, srcX0, dst, dstY0, dstX0, h, w);

    }

    public static void insert( DenseMatrix64F src, DenseMatrix64F dest, int destY0, int destX0) {
        extractInsert(src, 0, src.getNumRows(), 0, src.getNumCols(), dest, destY0, destX0);
    }

    public static DenseMatrix64F extract4Int( DenseMatrix64F src,
                                          int srcY0, int srcY1,
                                          int srcX0, int srcX1 )
    {
        if( srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows )
            throw new RuntimeException("srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.getNumRows");
        if( srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols )
            throw new RuntimeException("srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.getNumCols");

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;

        DenseMatrix64F dst = new DenseMatrix64F(h,w);
        extractImpl(src, srcY0, srcX0, dst, 0, 0, h, w);
        return dst;
    }

    public static DenseMatrix64F[] columnsToVector(DenseMatrix64F A, DenseMatrix64F[] v)
    {
        DenseMatrix64F []ret;
        if( v == null || v.length < A.numCols ) {
            ret = new DenseMatrix64F[ A.numCols ];
        } else {
            ret = v;
        }

        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DenseMatrix64F(A.numRows,1);
            } else {
                ret[i].reshapeBoolean(A.numRows,1, false);
            }

            DenseMatrix64F u = ret[i];

            for( int j = 0; j < A.numRows; j++ ) {
                u.set(j,0, A.get(j,i));
            }
        }

        return ret;
    }


    public static DenseMatrix64F[] rowsToVector(DenseMatrix64F A, DenseMatrix64F[] v)
    {
        DenseMatrix64F []ret;
        if( v == null || v.length < A.numRows ) {
            ret = new DenseMatrix64F[ A.numRows ];
        } else {
            ret = v;
        }


        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DenseMatrix64F(A.numCols,1);
            } else {
                ret[i].reshapeBoolean(A.numCols, 1, false);
            }

            DenseMatrix64F u = ret[i];

            for( int j = 0; j < A.numCols; j++ ) {
                u.set(j,0, A.get(i,j));
            }
        }

        return ret;
    }


    public static void setIdentity( DenseMatrix64F mat )
    {
        int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;

        Arrays.fill(mat.data, 0, mat.getNumElements(), 0);

        int index = 0;
        for( int i = 0; i < width; i++ , index += mat.numCols + 1) {
            mat.data[index] = 1;
        }
    }

    public static DenseMatrix64F identity1D( int width )
    {
        DenseMatrix64F ret = new DenseMatrix64F(width,width);

        for( int i = 0; i < width; i++ ) {
            ret.set(i,i,1.0);
        }

        return ret;
    }


    public static DenseMatrix64F identity( int numRows , int numCols )
    {
        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);

        int small = numRows < numCols ? numRows : numCols;

        for( int i = 0; i < small; i++ ) {
            ret.set(i,i,1.0);
        }

        return ret;
    }


    public static DenseMatrix64F diag( double[] diagEl )
    {
        return diagMatrix(null,diagEl.length,diagEl);
    }


    public static DenseMatrix64F diagMatrix( DenseMatrix64F ret , int width , double[] diagEl )
    {
        if( ret == null ) {
            ret = new DenseMatrix64F(width,width);
        } else {
            CommonOps.fill(ret, 0);
        }

        for( int i = 0; i < width; i++ ) {
            ret.set(i, i, diagEl[i]);
        }

        return ret;
    }


    /*
    public static DenseMatrix64F diagR( int numRows , int numCols , double ...diagEl )
    {
        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);

        int o = Math.min(numRows,numCols);

        for( int i = 0; i < o; i++ ) {
            ret.set(i,i,diagEl[i]);
        }

        return ret;
    }
*/

    public static void kron( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {
        int numColsC = A.numCols*B.numCols;
        int numRowsC = A.numRows*B.numRows;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                double a = A.get(i,j);

                for( int rowB = 0; rowB < B.numRows; rowB++ ) {
                    for( int colB = 0; colB < B.numCols; colB++ ) {
                        double val = a*B.get(rowB,colB);
                        C.set(i*B.numRows+rowB,j*B.numCols+colB,val);
                    }
                }
            }
        }
    }






    public static void extractDiag( DenseMatrix64F src, DenseMatrix64F dst )
    {
        int N = Math.min(src.numRows, src.numCols);

        if( !MatrixFeatures.isVector(dst) ) {
            throw new RuntimeException("Expected a vector for dst.");
        }

        for( int i = 0; i < N; i++ ) {
            dst.setValueAtIndex(i, src.get(i, i));
        }
    }


    public static DenseMatrix64F extractRow( DenseMatrix64F a , int row , DenseMatrix64F out ) {
        if( out == null)
            out = new DenseMatrix64F(1,a.numCols);

        System.arraycopy(a.data,a.getIndex(row,0),out.data,0,a.numCols);

        return out;
    }


    public static DenseMatrix64F extractColumn( DenseMatrix64F a , int column , DenseMatrix64F out ) {
        if( out == null)
            out = new DenseMatrix64F(a.numRows,1);

        int index = column;
        for (int i = 0; i < a.numRows; i++, index += a.numCols ) {
            out.data[i] = a.data[index];
        }
        return out;
    }



    public static double elementMax( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        double max = a.getValueAtIndex(0);
        for( int i = 1; i < size; i++ ) {
            double val = a.getValueAtIndex(i);
            if( val >= max ) {
                max = val;
            }
        }

        return max;
    }

    public static double elementMaxAbs( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        double max = 0;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.getValueAtIndex(i));
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

/*
    public static double elementMin( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        double min = a.getValueAtIndex(0);
        for( int i = 1; i < size; i++ ) {
            double val = a.getValueAtIndex(i);
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }


    public static double elementMinAbs( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        double min = Math.abs(a.getValueAtIndex(0));
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.getValueAtIndex(i));
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }


    public static void elementMult2mat( DenseMatrix64F a , DenseMatrix64F b )
    {

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.times(i, b.getValueAtIndex(i));
        }
    }*/


    public static void elementMult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, a.getValueAtIndex(i) * b.getValueAtIndex(i));
        }
    }

/*
    public static void elementDiv2mat( DenseMatrix64F a , DenseMatrix64F b )
    {
        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.div(i, b.getValueAtIndex(i));
        }
    }


    public static void elementDiv( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, a.getValueAtIndex(i) / b.getValueAtIndex(i));
        }
    }
*/

    public static double elementSum( DenseMatrix64F mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += mat.getValueAtIndex(i);
        }

        return total;
    }

/*
    public static double elementSumAbs( DenseMatrix64F mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += Math.abs(mat.getValueAtIndex(i));
        }

        return total;
    }


    public static void elementPower( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C ) {

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(A.data[i], B.data[i]);
        }
    }


    public static void elementPoweralpha( double a , DenseMatrix64F B , DenseMatrix64F C ) {

        int size = B.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(a,B.data[i]);
        }
    }


    public static void elementPowerMat( DenseMatrix64F A , double b, DenseMatrix64F C ) {


        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(A.data[i], b);
        }
    }


    public static void elementLog( DenseMatrix64F A , DenseMatrix64F C ) {


        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.log(A.data[i]);
        }
    }


    public static void elementExp( DenseMatrix64F A , DenseMatrix64F C ) {

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.exp(A.data[i]);
        }
    }


    public static DenseMatrix64F sumRows( DenseMatrix64F input , DenseMatrix64F output ) {
        if( output == null ) {
            output = new DenseMatrix64F(input.numRows,1);
        }

        for( int row = 0; row < input.numRows; row++ ) {
            double total = 0;

            int end = (row+1)*input.numCols;
            for( int index = row*input.numCols; index < end; index++ ) {
                total += input.data[index];
            }

            output.setValueAtIndex(row, total);
        }
        return output;
    }


    public static DenseMatrix64F sumCols( DenseMatrix64F input , DenseMatrix64F output ) {
        if( output == null ) {
            output = new DenseMatrix64F(1,input.numCols);
        }

        for( int cols = 0; cols < input.numCols; cols++ ) {
            double total = 0;

            int index = cols;
            int end = index + input.numCols*input.numRows;
            for( ; index < end; index += input.numCols ) {
                total += input.data[index];
            }

            output.setValueAtIndex(cols, total);
        }
        return output;
    }
*/

    public static void addEquals( DenseMatrix64F a , DenseMatrix64F b )
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, b.getValueAtIndex(i));
        }
    }


    public static void addEqualsbeta( DenseMatrix64F a , double beta, DenseMatrix64F b )
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, beta * b.getValueAtIndex(i));
        }
    }


    public static void add( final DenseMatrix64F a , final DenseMatrix64F b , final DenseMatrix64F c )
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, a.getValueAtIndex(i) + b.getValueAtIndex(i));
        }
    }

/*
    public static void addbeta ( DenseMatrix64F a , double beta , DenseMatrix64F b , DenseMatrix64F c )
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, a.getValueAtIndex(i) + beta*b.getValueAtIndex(i) );
        }
    }


    public static void addalphabeta ( double alpha , DenseMatrix64F a , double beta , DenseMatrix64F b , DenseMatrix64F c )
    {

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, alpha * a.getValueAtIndex(i) + beta * b.getValueAtIndex(i));
        }
    }


    public static void add3Mat( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.setValueAtIndex(i, alpha * a.getValueAtIndex(i) + b.getValueAtIndex(i));
        }
    }


    public static void addval( DenseMatrix64F a , double val ) {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, val);
        }
    }


    public static void addval2mat( DenseMatrix64F a , double val , DenseMatrix64F c ) {

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] + val;
        }
    }


    public static void subtract( DenseMatrix64F a , double val , DenseMatrix64F c ) {

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] - val;
        }
    }


    public static void subtract1( double val , DenseMatrix64F a , DenseMatrix64F c ) {

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = val - a.data[i];
        }
    }



    public static void subtractEquals(DenseMatrix64F a, DenseMatrix64F b)
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.data[i] -= b.data[i];
        }
    }
*/

    public static void subtract3mat (DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c)
    {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] - b.data[i];
        }
    }

    public static void scale( double alpha , DenseMatrix64F a )
    {
        // on very small matrices (2 by 2) the call to getNumElements() can slow it down
        // slightly compared to other libraries since it involves an extra multiplication.
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] *= alpha;
        }
    }

/*
    public static void scalemat( double alpha , DenseMatrix64F a , DenseMatrix64F b)
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new RuntimeException("Matrices must have the same shape");

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = a.data[i]*alpha;
        }
    }


    public static void divide0( double alpha , DenseMatrix64F a )
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] = alpha/a.data[i];
        }
    }

    public static void divide( DenseMatrix64F a , double alpha)
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] /= alpha;
        }
    }


    public static void divide2( double alpha , DenseMatrix64F a , DenseMatrix64F b)
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new RuntimeException("Matrices must have the same shape");

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = alpha/a.data[i];
        }
    }


    public static void divide3( DenseMatrix64F a , double alpha  , DenseMatrix64F b)
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new RuntimeException("Matrices must have the same shape");

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = a.data[i]/alpha;
        }
    }
*/

    public static void changeSign( DenseMatrix64F a )
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] = -a.data[i];
        }
    }

/*
    public static void changeSign2mat( DenseMatrix64F input , DenseMatrix64F output)
    {
        if( input.numRows != output.numRows || input.numCols != output.numCols )
            throw new RuntimeException("Matrices must have the same shape");

        final int size = input.getNumElements();

        for( int i = 0; i < size; i++ ) {
            output.data[i] = -input.data[i];
        }
    }
*/

    public static void fill(DenseMatrix64F a, double value)
    {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }

    public static void normalizeF( DenseMatrix64F A ) {
        double val = normF(A);

        if( val == 0 )
            return;

        int size = A.getNumElements();

        for( int i = 0; i < size; i++) {
            A.div(i , val);
        }
    }

    public static double normF( DenseMatrix64F a ) {
        double total = 0;

        double scale = CommonOps.elementMaxAbs(a);

        if( scale == 0.0 )
            return 0.0;

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            double val = a.getValueAtIndex(i)/scale;
            total += val*val;
        }

        return scale*Math.sqrt(total);
    }

    public static void extract6M ( DenseMatrix64F src,
                                int srcY0, int srcY1,
                                int srcX0, int srcX1,
                                   DenseMatrix64F dst ,
                                int dstY0, int dstX0 )
    {


        int w = srcX1-srcX0;
        int h = srcY1-srcY0;
            extractImpl(src,srcY0,srcX0,dst,dstY0,dstX0, h, w);

    }
}