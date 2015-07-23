package org.kevoree.modeling.util.maths.matrix;

public class VectorVectorMult {

    public static double innerProd(DenseMatrix64F x, DenseMatrix64F y) {
        int m = x.getNumElements();

        double total = 0;
        for (int i = 0; i < m; i++) {
            total += x.getValueAtIndex(i) * y.getValueAtIndex(i);
        }

        return total;
    }

    public static double innerProdA(DenseMatrix64F x, DenseMatrix64F A, DenseMatrix64F y) {
        int n = A.numRows;
        int m = A.numCols;

        double result = 0;

        for (int i = 0; i < m; i++) {
            double total = 0;

            for (int j = 0; j < n; j++) {
                total += x.getValueAtIndex(j) * A.get(j, i);
            }

            result += total * y.getValueAtIndex(i);
        }

        return result;
    }


    public static double innerProdTranA(DenseMatrix64F x, DenseMatrix64F A, DenseMatrix64F y) {
        int n = A.numRows;
        double result = 0;
        for (int i = 0; i < n; i++) {
            double total = 0;

            for (int j = 0; j < n; j++) {
                total += x.getValueAtIndex(j) * A.get(i, j);
            }

            result += total * y.getValueAtIndex(i);
        }

        return result;
    }

    public static void outerProd(DenseMatrix64F x, DenseMatrix64F y, DenseMatrix64F A) {
        int m = A.numRows;
        int n = A.numCols;

        int index = 0;
        for (int i = 0; i < m; i++) {
            double xdat = x.getValueAtIndex(i);
            for (int j = 0; j < n; j++) {
                A.setValueAtIndex(index++, xdat * y.getValueAtIndex(j));
            }
        }
    }


    public static void addOuterProd(double gamma, DenseMatrix64F x, DenseMatrix64F y, DenseMatrix64F A) {
        int m = A.numRows;
        int n = A.numCols;
        int index = 0;
        if (gamma == 1.0) {
            for (int i = 0; i < m; i++) {
                double xdat = x.getValueAtIndex(i);
                for (int j = 0; j < n; j++) {
                    A.plus(index++, xdat * y.getValueAtIndex(j));
                }
            }
        } else {
            for (int i = 0; i < m; i++) {
                double xdat = x.getValueAtIndex(i);
                for (int j = 0; j < n; j++) {
                    A.plus(index++, gamma * xdat * y.getValueAtIndex(j));
                }
            }
        }
    }


    public static void householder(double gamma,
                                   DenseMatrix64F u,
                                   DenseMatrix64F x, DenseMatrix64F y) {
        int n = u.getNumElements();
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += u.getValueAtIndex(i) * x.getValueAtIndex(i);
        }
        for (int i = 0; i < n; i++) {
            y.setValueAtIndex(i, x.getValueAtIndex(i) + gamma * u.getValueAtIndex(i) * sum);
        }
    }


    public static void rank1Update4Mat(double gamma,
                                       DenseMatrix64F A,
                                       DenseMatrix64F u, DenseMatrix64F w,
                                       DenseMatrix64F B) {
        int n = u.getNumElements();
        int matrixIndex = 0;
        for (int i = 0; i < n; i++) {
            double elementU = u.data[i];

            for (int j = 0; j < n; j++, matrixIndex++) {
                B.data[matrixIndex] = A.data[matrixIndex] + gamma * elementU * w.data[j];
            }
        }
    }

    public static void rank1Update(double gamma,
                                   DenseMatrix64F A,
                                   DenseMatrix64F u,
                                   DenseMatrix64F w) {
        int n = u.getNumElements();
        int matrixIndex = 0;
        for (int i = 0; i < n; i++) {
            double elementU = u.data[i];

            for (int j = 0; j < n; j++) {
                A.data[matrixIndex++] += gamma * elementU * w.data[j];
            }
        }
    }
}
