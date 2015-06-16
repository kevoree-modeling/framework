package org.kevoree.modeling.extrapolation.impl.maths;

import java.util.Arrays;

public class DenseMatrix64F {
    public int numRows;
    public int numCols;
    public double[] data;
    public static int MULT_COLUMN_SWITCH = 15;

    public static void multTransA_smallMV(DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C) {
        int cIndex = 0;
        for (int i = 0; i < A.numCols; i++) {
            double total = 0.0;
            int indexA = i;
            for (int j = 0; j < A.numRows; j++) {
                total += A.get(indexA) * B.get(j);
                indexA += A.numCols;
            }
            C.set(cIndex++, total);
        }
    }

    public static void multTransA_reorderMV(DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C) {
        if (A.numRows == 0) {
            DenseMatrix64F.fill(C, 0);
            return;
        }
        double B_val = B.get(0);
        for (int i = 0; i < A.numCols; i++) {
            C.set(i, A.get(i) * B_val);
        }
        int indexA = A.numCols;
        for (int i = 1; i < A.numRows; i++) {
            B_val = B.get(i);
            for (int j = 0; j < A.numCols; j++) {
                C.plus(j, A.get(indexA++) * B_val);
            }
        }
    }

    public static void multTransA_reorderMM(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        if (a.numCols == 0 || a.numRows == 0) {
            DenseMatrix64F.fill(c, 0);
            return;
        }
        double valA;
        for (int i = 0; i < a.numCols; i++) {
            int indexC_start = i * c.numCols;
            // first assign R
            valA = a.get(i);
            int indexB = 0;
            int end = indexB + b.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                c.set(indexC++, valA * b.get(indexB++));
            }
            // now increment it
            for (int k = 1; k < a.numRows; k++) {
                valA = a.unsafe_get(k, i);
                end = indexB + b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    c.plus(indexC++, valA * b.get(indexB++));
                }
            }
        }
    }

    public static void multTransA_smallMM(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        int cIndex = 0;
        for (int i = 0; i < a.numCols; i++) {
            for (int j = 0; j < b.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows * b.numCols;
                double total = 0;
                // loop for k
                for (; indexB < end; indexB += b.numCols) {
                    total += a.get(indexA) * b.get(indexB);
                    indexA += a.numCols;
                }
                c.set(cIndex++, total);
            }
        }
    }

    public static void multTransA(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        if (b.numCols == 1) {
            if (a.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH) {
                multTransA_reorderMV(a, b, c);
            } else {
                multTransA_smallMV(a, b, c);
            }
        } else if (a.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH || b.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH) {
            multTransA_reorderMM(a, b, c);
        } else {
            multTransA_smallMM(a, b, c);
        }
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

    public static void fill(DenseMatrix64F a, double value) {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }

    public double get(int index) {
        return data[index];
    }

    public double set(int index, double val) {
        return data[index] = val;
    }


    public double plus(int index, double val) {
        return data[index] += val;
    }

    public DenseMatrix64F(int numRows, int numCols) {
        data = new double[numRows * numCols];
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public void reshape(int numRows, int numCols, boolean saveValues) {
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

    public double unsafe_get(int row, int col) {
        return data[row * numCols + col];
    }

    public int getNumElements() {
        return numRows * numCols;
    }

}
