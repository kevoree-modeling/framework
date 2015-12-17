package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.PolynomialFit;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.solver.QR;

/**
 * Created by assaad on 17/12/15.
 */
public class QRTest {
    @Test
    public void qrtest(){
        double eps=1e-7;
        double[] coef={5,-4,1,7};
        int degree=coef.length-1;
        double[] t={0,1,2,3,4,5,6,7};
        double[] res=new double[t.length];
        KBlas java = new JavaBlas();

        for(int i=0;i<t.length;i++){
            res[i]= PolynomialFit.extrapolate(t[i],coef);
        }

        NativeArray2D A = new NativeArray2D(res.length, degree + 1);

        // cset up the A matrix
        for (int i = 0; i < res.length; i++) {
            double obs = 1;
            for (int j = 0; j < degree+1; j++) {
                A.set(i, j, obs);
                obs *= t[i];
            }
        }




        QR solver = QR.factorize(A,true,java);

        KArray2D Q= solver.getQ();
        KArray2D R=  solver.getR();

        KArray2D y=new NativeArray2D(res.length,1);
        y.setData(res);
        KArray2D coefNew = new NativeArray2D(degree + 1, 1);

        solver.solve(y,coefNew,java);

        int x=0;

        for(int i=0;i<coef.length;i++){
            Assert.assertTrue(Math.abs(coefNew.data()[i]-coef[i])< eps);
        }

     /*   PolynomialFit pf = new PolynomialFit(coef.length-1);
        pf.fit(t,res);
        double[] ejmlCoef=pf.getCoef();

        for(int i=0;i<coef.length;i++){
            Assert.assertTrue(Math.abs(ejmlCoef[i]-coef[i])< eps);
        }*/




    }

}
