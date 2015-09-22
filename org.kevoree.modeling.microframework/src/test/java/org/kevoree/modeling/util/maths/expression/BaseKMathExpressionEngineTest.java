package org.kevoree.modeling.util.maths.expression;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.PrimitiveHelper;

public abstract class BaseKMathExpressionEngineTest {

    protected abstract KMathExpressionEngine createEngine();

    @Test
    public void expressionTest() {
        KMathExpressionEngine ex = createEngine();
        ex.setVarResolver(new KMathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                if (PrimitiveHelper.equals(potentialVarName,"PI")) {
                    return Math.PI;
                }
                if (PrimitiveHelper.equals(potentialVarName,"TRUE")) {
                    return 1.0;
                }
                if (PrimitiveHelper.equals(potentialVarName,"FALSE")) {
                    return 0.0;
                } else if (PrimitiveHelper.equals(potentialVarName,"price")) {
                    return 10.0;
                }
                return null;
            }
        });
        Assert.assertTrue(ex.parse("(3.5+price*8-14/7)%4").eval(null) == 1.5);
    }

}
