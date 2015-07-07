package org.kevoree.modeling.util.maths;

import org.junit.Test;
import org.junit.Assert;

public class testExpression {
    @Test
    public void expressionTest(){
        Expression ex = new Expression("(3.5+price*8-14/7)%4");
        ex.setVariable("price","10");
        Assert.assertEquals(ex.eval(),1.5,0.0001);
    }
}
