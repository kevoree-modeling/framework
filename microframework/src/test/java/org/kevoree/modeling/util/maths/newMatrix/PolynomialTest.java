package org.kevoree.modeling.util.maths.newMatrix;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.PolynomialFit;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;
import org.kevoree.modeling.util.maths.structure.matrix.PolynomialFitBlas;

/**
 * Created by assaad on 16/12/15.
 */
public class PolynomialTest {

    @Test
    public void polytest(){
        double eps=1e-7;
        double[] coef={5,-4,1,7};
        double[] t={0,1,2,3,4,5,6,7,8,9};
        double[] res=new double[t.length];
        KBlas java = new JavaBlas();

        for(int i=0;i<t.length;i++){
            res[i]=PolynomialFit.extrapolate(t[i],coef);
        }


        PolynomialFit pf = new PolynomialFit(coef.length-1);
        pf.fit(t,res);
        double[] ejmlCoef=pf.getCoef();

       for(int i=0;i<coef.length;i++){
           Assert.assertTrue(Math.abs(ejmlCoef[i]-coef[i])< eps);
        }


       PolynomialFitBlas pfJava = new PolynomialFitBlas(coef.length-1,java);
        pfJava.fit(t,res);
        double[] javaCoef=pfJava.getCoef();

        for(int i=0;i<coef.length;i++){
            Assert.assertTrue(Math.abs(javaCoef[i]-coef[i])< eps);
        }



    }


}
