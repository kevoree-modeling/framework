package org.kevoree.modeling.util.maths.expression;

import org.junit.Assert;
import org.junit.Test;

public abstract class BaseKMathExpressionEngineTest {

    protected abstract KMathExpressionEngine createEngine();

    @Test
    public void expressionTest() {
        KMathExpressionEngine ex = createEngine();
        ex.setVarResolver(new KMathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                if (potentialVarName.equals("PI")) {
                    return Math.PI;
                }
                if (potentialVarName.equals("TRUE")) {
                    return 1.0;
                }
                if (potentialVarName.equals("FALSE")) {
                    return 0.0;
                } else if (potentialVarName.equals("price")) {
                    return 10.0;
                }
                return null;
            }
        });
        Assert.assertTrue(ex.eval("(3.5+price*8-14/7)%4") == 1.5);
    }

}
