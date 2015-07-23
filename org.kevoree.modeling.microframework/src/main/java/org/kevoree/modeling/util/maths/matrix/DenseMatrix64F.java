package org.kevoree.modeling.util.maths.matrix;

import java.util.Arrays;

public class DenseMatrix64F {
    public int numRows;
    public int numCols;
    public double[] data;
    public static int MULT_COLUMN_SWITCH = 15;

    public DenseMatrix64F(int numRows, int numCols){
        data = new double[numRows * numCols];
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public DenseMatrix64F constructorDenseMatrix( DenseMatrix64F orig ) {
        DenseMatrix64F result=new DenseMatrix64F(orig.numRows,orig.numCols);
        System.arraycopy(orig.data, 0, result.data, 0, orig.getNumElements());
        return result;
    }


    public static void setIdentity(DenseMatrix64F mat) {
        int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;
        Arrays.fill(mat.data, 0, mat.getNumElements(), 0);
        int index = 0;
        for (int i = 0; i < width; i++) {
            mat.data[index] = 1;
            index += mat.numCols + 1;
        }
    }

    public static DenseMatrix64F widentity(int width) {
        DenseMatrix64F ret = new DenseMatrix64F(width, width);
        for (int i = 0; i < width; i++) {
            ret.cset(i, i, 1.0);
        }
        return ret;
    }

    public static DenseMatrix64F identity(int numRows, int numCols) {
        DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
        int small = numRows < numCols ? numRows : numCols;
        for (int i = 0; i < small; i++) {
            ret.cset(i, i, 1.0);
        }
        return ret;
    }

    public void constructorRow(int numRows, int numCols, boolean rowMajor, double[] data) {
        final int length = numRows * numCols;
        this.data = new double[ length ];
        this.numRows = numRows;
        this.numCols = numCols;
        setArray(numRows, numCols, rowMajor, data);
    }

    public void constructor2DArray( double data[][] ) {
        this.numRows = data.length;
        this.numCols = data[0].length;

        this.data = new double[ numRows*numCols ];

        int pos = 0;
        for( int i = 0; i < numRows; i++ ) {
            double []row = data[i];
            System.arraycopy(row,0,this.data,pos,numCols);

            pos += numCols;
        }
    }



    public void constructor1dArray( int length ) {
        data = new double[ length ];
    }



 /*   public static DenseMatrix64F wrap( int numRows , int numCols , double []data ) {
        DenseMatrix64F s = new DenseMatrix64F(numRows,numCols);
        s.data = data;
        return s;
    }*/






    public boolean isInBounds( int row  , int col ) {
        return( col >= 0 && col < numCols && row >= 0 && row < numRows );
    }



    public void zero() {
        Arrays.fill(data, 0, getNumElements(), 0.0);
    }

    public DenseMatrix64F copy() {
        return constructorDenseMatrix(this);
    }



    public static void fill(DenseMatrix64F a, double value) {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }



    public void reshapeBoolean(int numRows, int numCols, boolean saveValues) {
        if (data.length < numRows * numCols) {
            double[] d = new double[numRows * numCols];
            if (saveValues) {
                System.arraycopy(data, 0, d, 0, getNumElements());
            }
            this.data = d;
        }
        this.numRows = numRows;
        this.numCols = numCols;
    }


    public void cset(int row, int col, double value) {
        data[row * numCols + col] = value;
    }

    public void add( int row , int col , double value ) {
        data[ row * numCols + col ] += value;
    }

    public double plus(int index, double val) {
        return data[index] += val;
    }


    public DenseMatrix64F plusMatrix(DenseMatrix64F matrix2) {
        for(int i=0;i<data.length;i++){
            data[i]+=matrix2.data[i];
        }
        return this;
    }

    public DenseMatrix64F scale(double value) {
            for( int i = 0; i <  data.length; i++ ) {
                data[i] *= value;
            }
        return this;
    }


    public double minus( int index , double val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] -= val;
    }


    public double times( int index , double val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] *= val;
    }

    public double div( int index , double val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] /= val;
    }


    public void reshape( int numRows , int numCols ) {
        reshapeBoolean(numRows,numCols,false);
    }



    public int getNumRows() {
        return numRows;
    }
    public int getNumCols() {
        return numCols;
    }
    public double[] getData() {
        return data;
    }
    public double get(int row, int col) {
        return data[row * numCols + col];
    }
    public int getNumElements() {
        return numRows * numCols;
    }
    public int getIndex( int row , int col ) {
        return row * numCols + col;
    }

    public double getValueAtIndex(int index) {
        return data[index];
    }

    public double setValueAtIndex(int index, double val) {
        return data[index] = val;
    }


    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }
    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    public void setData( double[] data ) {
        this.data = data;
    }
    public void setMatrix( DenseMatrix64F b )
    {
        this.reshape(b.numRows,b.numCols);
        int dataLength = b.getNumElements();
        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }
    public void set( int row , int col , double value ) {
        data[ row * numCols + col ] = value;
    }

    public void setArray(int numRows, int numCols, boolean rowMajor, double[] data)
    {
        reshapeBoolean(numRows, numCols, false);
        int length = numRows*numCols;

        if( rowMajor ) {
            System.arraycopy(data,0,this.data,0,length);
        } else {
            int index = 0;
            for( int i = 0; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    this.data[index++] = data[j * numRows + i];
                }
            }
        }
    }

}
