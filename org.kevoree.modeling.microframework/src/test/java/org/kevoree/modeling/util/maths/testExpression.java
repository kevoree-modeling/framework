package org.kevoree.modeling.util.maths;

import org.junit.Test;
import org.junit.Assert;
import org.kevoree.modeling.util.maths.expression.impl.MathExpressionEngine;

public class testExpression {
    @Test
    public void expressionTest(){
        MathExpressionEngine ex = new MathExpressionEngine();
        ex.setVariable("price","10");
        Assert.assertEquals(ex.eval("(3.5+price*8-14/7)%4"),1.5,0.0001);
    }
}
