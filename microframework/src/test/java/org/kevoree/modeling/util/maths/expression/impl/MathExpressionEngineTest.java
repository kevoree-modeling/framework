package org.kevoree.modeling.util.maths.expression.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.maths.expression.BaseKMathExpressionEngineTest;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;

public class MathExpressionEngineTest extends BaseKMathExpressionEngineTest {

    @Override
    protected KMathExpressionEngine createEngine() {
        return new MathExpressionEngine();
    }

    @Test
    public void tokenTest() {
        MathExpressionTokenizer tokenizer = new MathExpressionTokenizer("(3.5+price*8-14/7)%4");
        int i=0;
        while(tokenizer.hasNext()){
            String tok = tokenizer.next();
            i++;
        }
        Assert.assertEquals(i, 13);
    }
}
