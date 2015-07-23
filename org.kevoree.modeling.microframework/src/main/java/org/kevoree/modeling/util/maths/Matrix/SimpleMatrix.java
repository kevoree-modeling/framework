package org.kevoree.modeling.util.maths.matrix;

public class SimpleMatrix{

    protected DenseMatrix64F mat;

    public DenseMatrix64F getMatrix() {
        return mat;
    }

/*
    public SimpleMatrix mult( SimpleMatrix b ) {
        SimpleMatrix ret = createMatrix(mat.numRows,b.getMatrix().numCols);
        CommonOps.mult(mat, b.getMatrix(), ret.getMatrix());
        return ret;
    }

    public SimpleMatrix kron( SimpleMatrix B ) {
        SimpleMatrix ret = createMatrix(mat.numRows * B.numRows(), mat.numCols * B.numCols());
        CommonOps.kron(mat, B.getMatrix(), ret.getMatrix());
        return ret;
    }


    public SimpleMatrix plus( SimpleMatrix b ) {
        SimpleMatrix ret = copy();
        CommonOps.addEquals(ret.getMatrix(), b.getMatrix());
        return ret;
    }


    public SimpleMatrix minus( SimpleMatrix b ) {
        SimpleMatrix ret = copy();
        CommonOps.subtract3mat(getMatrix(), b.getMatrix(), ret.getMatrix());
        return ret;
    }


    public SimpleMatrix minus( double b ) {
        SimpleMatrix ret = copy();

        CommonOps.subtract(getMatrix(), b, ret.getMatrix());

        return ret;
    }


    public SimpleMatrix plus( double beta ) {
        SimpleMatrix ret = createMatrix(numRows(), numCols());

        CommonOps.addval2mat(getMatrix(), beta, ret.getMatrix());

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
        return mat.numRows == 1 || mat.numCols == 1;
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




    public void set( SimpleMatrix a ) {
        mat.setMatrix(a.getMatrix());
    }



    public void set( double val ) {
        CommonOps.fill(mat, val);
    }


    public void zero() {
        mat.zero();
    }




    public double trace() {
        return CommonOps.trace(mat);
    }


    public void reshape( int numRows , int numCols ) {
        mat.reshapeBoolean(numRows, numCols, false);
    }


    public void set( int row , int col , double value ) {
        mat.set(row, col, value);
    }


    public void set( int index , double value ) {
        mat.setValueAtIndex(index, value);
    }


    public void setRow( int row , int offset , double ...values ) {
        for( int i = 0; i < values.length; i++ ) {
            mat.set(row,offset+i,values[i]);
        }
    }

    public void setColumn( int column , int offset , double ...values ) {
        for( int i = 0; i < values.length; i++ ) {
            mat.set(offset+i,column,values[i]);
        }
    }


    public double get( int row , int col ) {
        return mat.get(row,col);
    }


    public double get( int index ) {
        return mat.data[ index ];
    }

*/
    public int getIndex( int row , int col ) {
        return row * mat.numCols + col;
    }




    public SimpleMatrix copy() {
        SimpleMatrix ret = createMatrix(mat.numRows,mat.numCols);
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

        SimpleMatrix diag = createMatrix(N,1);

        CommonOps.extractDiag(mat,diag.getMatrix());

        return diag;
    }

   /* public boolean isIdentical( SimpleMatrix  a, double tol) {
        return MatrixFeatures.isIdentical(mat, a.getMatrix(), tol);
    }*/



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
        System.out.println("[rows = "+numRows()+" , cols = "+numCols()+" ]");
    }

    /*
    public SimpleMatrix(int numRows, int numCols, boolean rowMajor, double ...data) {
        mat = new DenseMatrix64F(numRows,numCols, rowMajor, data);
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


    public static SimpleMatrix diag( double ...vals ) {
        DenseMatrix64F m = CommonOps.diag(vals);
        SimpleMatrix ret = wrap(m);
        return ret;
    }



    protected SimpleMatrix createMatrix( int numRows , int numCols ) {
        SimpleMatrix sm= new SimpleMatrix(numRows,numCols);
        return sm;
    }


    /*

    public SimpleMatrix combine( int insertRow, int insertCol, SimpleMatrix B) {

        if( insertRow == SimpleMatrix.END ) {
            insertRow = mat.numRows;
        }

        if( insertCol == SimpleMatrix.END ) {
            insertCol = mat.numCols;
        }

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


    public SimpleMatrix extractMatrix(int y0 , int y1, int x0 , int x1 ) {
        if( y0 == SimpleMatrix.END ) y0 = mat.numRows;
        if( y1 == SimpleMatrix.END ) y1 = mat.numRows;
        if( x0 == SimpleMatrix.END ) x0 = mat.numCols;
        if( x1 == SimpleMatrix.END ) x1 = mat.numCols;

        SimpleMatrix ret = createMatrix(y1-y0,x1-x0);

        CommonOps.extract(mat, y0, y1, x0, x1, ret.getMatrix(), 0, 0);

        return ret;
    }


    public MatrixIterator64F iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator64F(mat,rowMajor, minRow, minCol, maxRow, maxCol);
    }

    public SimpleMatrix transpose() {
        SimpleMatrix ret = createMatrix(mat.numCols, mat.numRows);
        CommonOps.transpose(mat, ret.getMatrix());
        return ret;
    }

    public SimpleMatrix invert() {
        SimpleMatrix ret = createMatrix(mat.numRows,mat.numCols);
        if( !CommonOps.invert(mat,ret.getMatrix()) ) {
            throw new SingularMatrixException();
        }
        if( MatrixFeatures.hasUncountable(ret.getMatrix()))
            throw new SingularMatrixException("Solution has uncountable numbers");
        return ret;
    }


    public SimpleMatrix pseudoInverse() {
        SimpleMatrix ret = createMatrix(mat.numCols,mat.numRows);
        CommonOps.pinv(mat, ret.getMatrix());
        return ret;
    }


    public SimpleMatrix solve( SimpleMatrix b )
    {
        SimpleMatrix x = createMatrix(mat.numCols,b.getMatrix().numCols);

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



    public SimpleMatrix extractVector( boolean extractRow , int element )
    {
        int length = extractRow ? mat.numCols : mat.numRows;

        SimpleMatrix ret = extractRow ? createMatrix(1,length) : createMatrix(length,1);

        if( extractRow ) {
            SpecializedOps.subvector(mat,element,0,length,true,0,ret.getMatrix());
        } else {
            SpecializedOps.subvector(mat,0,element,length,false,0,ret.getMatrix());
        }

        return ret;
    }


    public SimpleSVD svd() {
        return new SimpleSVD(mat,false);
    }


    public SimpleSVD svd( boolean compact ) {
        return new SimpleSVD(mat,compact);
    }


    public SimpleEVD eig() {
        return new SimpleEVD(mat);
    }


    public void insertIntoThis(int insertRow, int insertCol, SimpleMatrix B) {
        CommonOps.insert(B.getMatrix(), mat, insertRow, insertCol);
    }


    public static SimpleMatrix random(int numRows, int numCols, double minValue, double maxValue, Random rand) {
        SimpleMatrix ret = new SimpleMatrix(numRows,numCols);
        RandomMatrices.setRandom(ret.mat,minValue,maxValue,rand);
        return ret;
    }


    public static SimpleMatrix randomNormal( SimpleMatrix covariance , Random random ) {
        CovarianceRandomDraw draw = new CovarianceRandomDraw(random,covariance.getMatrix());

        SimpleMatrix found = new SimpleMatrix(covariance.numRows(),1);
        draw.next(found.getMatrix());

        return found;
    }

*/

}
