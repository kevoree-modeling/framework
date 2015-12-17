package org.kevoree.modeling.util.maths.structure.matrix;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.solver.QR;

/**
 * Created by assaad on 16/12/15.
 */
public class PolynomialFitBlas {
    NativeArray2D y;
    NativeArray2D A;
    KArray2D coef;
    int degree=0;

    KBlas blas;

    public PolynomialFitBlas(int degree, KBlas blas) {
        this.degree=degree;
        this.blas=blas;
    }

    public double[] getCoef() {
        return coef.data();
    }

    public void fit(double samplePoints[], double[] observations) {


        y = new NativeArray2D(observations.length, 1);
        y.setData(observations);

        A = new NativeArray2D(y.rows(), degree + 1);

        // cset up the A matrix
        for (int i = 0; i < observations.length; i++) {
            double obs = 1;
            for (int j = 0; j < degree+1; j++) {
                A.set(i, j, obs);
                obs *= samplePoints[i];
            }
        }
        // processValues the A matrix and see if it failed
        QR solver = QR.factorize(A,true,blas);
        coef = new NativeArray2D(degree + 1, 1);
        solver.solve(y,coef,blas);
    }

    public static double extrapolate(double time, double[] weights) {
        double result = 0;
        double power = 1;
        for (int j = 0; j < weights.length; j++) {
            result += weights[j] * power;
            power = power * time;
        }
        return result;
    }

}
