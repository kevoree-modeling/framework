package org.kevoree.modeling.util.maths.matrix;

import org.kevoree.modeling.util.maths.matrix.solvers.SimpleEVD;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;

public class SimpleMatrix{

    protected DenseMatrix64F mat;

    public DenseMatrix64F getMatrix() {
        return mat;
    }

/*


    public SimpleMatrix kron( SimpleMatrix B ) {
        SimpleMatrix ret = createMatrix(mat.getNumRows * B.getNumRows(), mat.getNumCols * B.getNumCols());
        CommonOps.kron(mat, B.getMatrix(), ret.getMatrix());
        return ret;
    }




    public SimpleMatrix minus( SimpleMatrix b ) {
        SimpleMatrix ret = copy();
        CommonOps.subtract3mat(getMatrix(), b.getMatrix(), ret.getMatrix());
        return ret;
    }








    public SimpleMatrix plus( double beta , SimpleMatrix b ) {
        SimpleMatrix ret = copy();

        CommonOps.addEqualsbeta(ret.getMatrix(), beta, b.getMatrix());

        return ret;
    }


    public double dot( SimpleMatrix v ) {
        if( !isVector() ) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        } else if( !v.isVector() ) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }

        return VectorVectorMult.innerProd(mat,v.getMatrix());
    }


    public boolean isVector() {
        return mat.getNumRows == 1 || mat.getNumCols == 1;
    }


    public SimpleMatrix scale( double val ) {
        SimpleMatrix ret = copy();

        CommonOps.scale(val, ret.getMatrix());

        return ret;
    }

    public SimpleMatrix divide( double val ) {
        SimpleMatrix ret = copy();

        CommonOps.divide(ret.getMatrix(), val);

        return ret;
    }




    public void setPrimitiveType( SimpleMatrix a ) {
        mat.setMatrix(a.getMatrix());
    }



    public void setPrimitiveType( double val ) {
        CommonOps.fill(mat, val);
    }


    public void zero() {
        mat.zero();
    }







    public void reshape( int getNumRows , int getNumCols ) {
        mat.reshapeBoolean(getNumRows, getNumCols, false);
    }




    public void setRow( int row , int offset , double ...values ) {
        for( int i = 0; i < values.length; i++ ) {
            mat.setPrimitiveType(row,offset+i,values[i]);
        }
    }

    public void setColumn( int column , int offset , double ...values ) {
        for( int i = 0; i < values.length; i++ ) {
            mat.setPrimitiveType(offset+i,column,values[i]);
        }
    }


    public double getPrimitiveType( int row , int col ) {
        return mat.getPrimitiveType(row,col);
    }


    public double getPrimitiveType( int index ) {
        return mat.data[ index ];
    }

*/

    public void setValue2D( int row , int col , double value ) {
        mat.set(row, col, value);
    }


    public void setValue1D( int index , double value ) {
        mat.setValueAtIndex(index, value);
    }


    public double getValue2D( int row , int col ) {
        return mat.get(row,col);
    }


    public double getValue1D( int index ) {
        return mat.data[ index ];
    }

    public int getIndex( int row , int col ) {
        return row * mat.numCols + col;
    }

    public SimpleMatrix mult( SimpleMatrix b ) {
        SimpleMatrix ret = createMatrix(mat.numRows, b.getMatrix().numCols);
        CommonOps.mult(mat, b.getMatrix(), ret.getMatrix());
        return ret;
    }

    public SimpleMatrix scale( double val ) {
        SimpleMatrix ret = copy();

        CommonOps.scale(val, ret.getMatrix());

        return ret;
    }

    public SimpleMatrix plus( SimpleMatrix b ) {
        SimpleMatrix ret = copy();
        CommonOps.addEquals(ret.getMatrix(), b.getMatrix());
        return ret;
    }

    /*
    public SimpleMatrix plus( double beta ) {
        SimpleMatrix ret = createMatrix(getNumRows(), getNumCols());
        CommonOps.addval2mat(getMatrix(), beta, ret.getMatrix());
        return ret;
    }

    public SimpleMatrix minus( double b ) {
        SimpleMatrix ret = copy();
        CommonOps.subtract(getMatrix(), b, ret.getMatrix());
        return ret;
    }*/

    public SimpleMatrix copy() {
        SimpleMatrix ret = createMatrix(mat.numRows, mat.numCols);
        ret.getMatrix().setMatrix(this.getMatrix());
        return ret;
    }


    public int numRows() {
        return mat.numRows;
    }


    public int numCols() {
        return mat.numCols;
    }


    public int getNumElements() {
        return mat.getNumElements();
    }

    public SimpleMatrix extractDiag()
    {
        int N = Math.min(mat.numCols,mat.numRows);

        SimpleMatrix diag = createMatrix(N, 1);

        CommonOps.extractDiag(mat, diag.getMatrix());

        return diag;
    }

    public boolean isIdentical( SimpleMatrix  a, double tol) {
        return MatrixFeatures.isIdentical(mat, a.getMatrix(), tol);
    }

    public double trace() {
        return CommonOps.trace(mat);
    }

    public double elementMaxAbs() {
        return CommonOps.elementMaxAbs(mat);
    }


    public double elementSum() {
        return CommonOps.elementSum(mat);
    }


    public SimpleMatrix elementMult( SimpleMatrix b )
    {
        SimpleMatrix c = createMatrix(mat.numRows,mat.numCols);

        CommonOps.elementMult(mat, b.getMatrix(), c.getMatrix());

        return c;
    }


    public SimpleMatrix elementDiv( SimpleMatrix b )
    {
        SimpleMatrix c = createMatrix(mat.numRows,mat.numCols);

        CommonOps.elementDiv(mat, b.getMatrix(), c.getMatrix());

        return c;
    }


    public SimpleMatrix elementPowerMatrix( SimpleMatrix b )
    {
        SimpleMatrix c = createMatrix(mat.numRows,mat.numCols);

        CommonOps.elementPower(mat, b.getMatrix(), c.getMatrix());

        return c;
    }


    public SimpleMatrix elementPower( double b )
    {
        SimpleMatrix c = createMatrix(mat.numRows,mat.numCols);

        CommonOps.elementPowerMat(mat, b, c.getMatrix());

        return c;
    }

    public SimpleMatrix elementExp()
    {
        SimpleMatrix c = createMatrix(mat.numRows, mat.numCols);

        CommonOps.elementExp(mat, c.getMatrix());

        return c;
    }

    public SimpleMatrix elementLog()
    {
        SimpleMatrix c = createMatrix(mat.numRows, mat.numCols);

        CommonOps.elementLog(mat, c.getMatrix());

        return c;
    }

    public SimpleMatrix negative() {
        SimpleMatrix A = copy();
        CommonOps.changeSign(A.getMatrix());
        return A;
    }






    public boolean isInBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < mat.numRows && col < mat.numCols;
    }


    public void printDimensions() {
        System.out.println("[rows = " + numRows() + " , cols = " + numCols() + " ]");
    }

    /*
    public SimpleMatrix(int getNumRows, int getNumCols, boolean rowMajor, double ...data) {
        mat = new DenseMatrix64F(getNumRows,getNumCols, rowMajor, data);
    }


    public SimpleMatrix(double data[][]) {
        mat = new DenseMatrix64F(data);
    }





    public SimpleMatrix( SimpleMatrix orig ) {
        this.mat = orig.mat.copy();
    }

    public SimpleMatrix( DenseMatrix64F orig ) {
        this.mat = orig.copy();
    }
*/

    public SimpleMatrix transpose() {
        SimpleMatrix ret = createMatrix(mat.numCols, mat.numRows);
        CommonOps.transposeMatrix(mat, ret.getMatrix());
        return ret;
    }


    public SimpleMatrix(int numRows, int numCols) {
        mat = new DenseMatrix64F(numRows, numCols);
    }


    public static SimpleMatrix wrap( DenseMatrix64F internalMat ) {
        SimpleMatrix ret = new SimpleMatrix(internalMat.numRows,internalMat.numCols);
        ret.mat = internalMat;
        return ret;
    }


    public static SimpleMatrix identity( int width ) {
        SimpleMatrix ret = new SimpleMatrix(width,width);

        CommonOps.setIdentity(ret.mat);

        return ret;
    }

    public SimpleMatrix minus( SimpleMatrix b ) {
        SimpleMatrix ret = copy();
        CommonOps.subtract3mat(getMatrix(), b.getMatrix(), ret.getMatrix());
        return ret;
    }

    public SimpleMatrix invert() {
        SimpleMatrix ret = createMatrix(mat.numRows, mat.numCols);
        CommonOps.invert(mat, ret.getMatrix());
        return ret;
    }


   /* public SimpleMatrix pseudoInverse() {
        SimpleMatrix ret = createMatrix(mat.getNumCols,mat.getNumRows);
        CommonOps.pinv(mat, ret.getMatrix());
        return ret;
    }*/

    public static SimpleMatrix diag( double ...vals ) {
        DenseMatrix64F m = CommonOps.diag(vals);
        SimpleMatrix ret = wrap(m);
        return ret;
    }

    public double determinant() {
        double ret = CommonOps.det(mat);
        return ret;
    }


    protected SimpleMatrix createMatrix( int numRows , int numCols ) {
        SimpleMatrix sm= new SimpleMatrix(numRows,numCols);
        return sm;
    }

    public SimpleMatrix extractVector( boolean extractRow , int element )
    {
        int length = extractRow ? mat.numCols : mat.numRows;

        SimpleMatrix ret = extractRow ? createMatrix(1,length) : createMatrix(length,1);

        if( extractRow ) {
            CommonOps.subvector(mat,element,0,length,true,0,ret.getMatrix());
        } else {
            CommonOps.subvector(mat,0,element,length,false,0,ret.getMatrix());
        }
        return ret;
    }

    public SimpleEVD eig() {
        return new SimpleEVD(mat);
    }

    public SimpleSVD svd( boolean compact ) {
        return new SimpleSVD(mat,compact);
    }

    public SimpleMatrix combine( int insertRow, int insertCol, SimpleMatrix B) {

        int maxRow = insertRow + B.numRows();
        int maxCol = insertCol + B.numCols();

        SimpleMatrix ret;

        if( maxRow > mat.numRows || maxCol > mat.numCols) {
            int M = Math.max(maxRow,mat.numRows);
            int N = Math.max(maxCol,mat.numCols);

            ret = createMatrix(M,N);
            ret.insertIntoThis(0,0,this);
        } else {
            ret = copy();
        }

        ret.insertIntoThis(insertRow,insertCol,B);

        return ret;
    }

    public void insertIntoThis(int insertRow, int insertCol, SimpleMatrix B) {
        CommonOps.insert(B.getMatrix(), mat, insertRow, insertCol);
    }





    /*




    public SimpleMatrix extractMatrix(int y0 , int y1, int x0 , int x1 ) {
        if( y0 == SimpleMatrix.END ) y0 = mat.getNumRows;
        if( y1 == SimpleMatrix.END ) y1 = mat.getNumRows;
        if( x0 == SimpleMatrix.END ) x0 = mat.getNumCols;
        if( x1 == SimpleMatrix.END ) x1 = mat.getNumCols;

        SimpleMatrix ret = createMatrix(y1-y0,x1-x0);

        CommonOps.extract(mat, y0, y1, x0, x1, ret.getMatrix(), 0, 0);

        return ret;
    }


    public MatrixIterator64F iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator64F(mat,rowMajor, minRow, minCol, maxRow, maxCol);
    }





    public SimpleMatrix solve( SimpleMatrix b )
    {
        SimpleMatrix x = createMatrix(mat.getNumCols,b.getMatrix().getNumCols);

        if( !CommonOps.solve(mat, b.getMatrix(), x.getMatrix()) )
            throw new SingularMatrixException();

        if( MatrixFeatures.hasUncountable(x.getMatrix()) )
            throw new SingularMatrixException("Solution contains uncountable numbers");

        return x;
    }



    public double normF() {
        return NormOps.normF(mat);
    }


    public double conditionP2() {
        return NormOps.conditionP2(mat);
    }


    public double determinant() {
        double ret = CommonOps.det(mat);
        // if the decomposition silently failed then the matrix is most likely singular
        if(UtilEjml.isUncountable(ret))
            return 0;
        return ret;
    }









    public SimpleEVD eig() {
        return new SimpleEVD(mat);
    }



    public static SimpleMatrix random(int getNumRows, int getNumCols, double minValue, double maxValue, Random rand) {
        SimpleMatrix ret = new SimpleMatrix(getNumRows,getNumCols);
        RandomMatrices.setRandom(ret.mat,minValue,maxValue,rand);
        return ret;
    }


    public static SimpleMatrix randomNormal( SimpleMatrix covariance , Random random ) {
        CovarianceRandomDraw draw = new CovarianceRandomDraw(random,covariance.getMatrix());

        SimpleMatrix found = new SimpleMatrix(covariance.getNumRows(),1);
        draw.next(found.getMatrix());

        return found;
    }

*/

}
