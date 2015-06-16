package org.kevoree.modeling.extrapolation.impl.maths;

public class PolynomialFitEjml {
    // Vandermonde matrix
    DenseMatrix64F A;
    // matrix containing computed polynomial coefficients
    DenseMatrix64F coef;
    // observation matrix
    DenseMatrix64F y;
    // solver used to compute
    AdjLinearSolverQr solver;

    public PolynomialFitEjml(int degree) {
        coef = new DenseMatrix64F(degree + 1, 1);
        A = new DenseMatrix64F(1, degree + 1);
        y = new DenseMatrix64F(1, 1);
        // create a solver that allows elements to be added or removed efficiently
        solver = new AdjLinearSolverQr();
    }

    public double[] getCoef() {
        return coef.data;
    }

    public void fit(double samplePoints[], double[] observations) {
        y.reshape(observations.length, 1, false);
        System.arraycopy(observations, 0, y.data, 0, observations.length);
        A.reshape(y.numRows, coef.numRows, false);
        // cset up the A matrix
        for (int i = 0; i < observations.length; i++) {
            double obs = 1;
            for (int j = 0; j < coef.numRows; j++) {
                A.cset(i, j, obs);
                obs *= samplePoints[i];
            }
        }
        // process the A matrix and see if it failed
        solver.setA(A);
        solver.solve(y, coef);
    }

}
