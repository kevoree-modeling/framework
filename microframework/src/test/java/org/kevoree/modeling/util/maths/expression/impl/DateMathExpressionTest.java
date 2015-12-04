package org.kevoree.modeling.util.maths.expression.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.KMathVariableResolver;

import java.util.Calendar;

public class DateMathExpressionTest {

    protected KMathExpressionEngine createEngine() {
        return new MathExpressionEngine();
    }

    /**
     * @native ts
     * var date = new Date(2015,2,11,20,53,47,0);
     * return date.getTime();
     */
    public double getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 02, 11, 20, 53, 47); // date of wednesday 11 march 2015, 20h:53min:47sec
        return (double) calendar.getTimeInMillis();
    }

    @Test
    public void date() {
        KMathExpressionEngine ex = createEngine();
        ex.setVarResolver(new KMathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                if (PrimitiveHelper.equals(potentialVarName, "time")) {
                    return getDate();
                }
                return null;
            }
        });
        Assert.assertTrue(ex.parse("SECONDS(time)").eval(null) == 47);
        Assert.assertTrue(ex.parse("MINUTES(time)").eval(null) == 53);
        Assert.assertTrue(ex.parse("HOURS(time)").eval(null) == 20);
        Assert.assertTrue(ex.parse("DAY(time)").eval(null) == 11);
        Assert.assertTrue(ex.parse("MONTH(time)").eval(null) == 2);
        Assert.assertTrue(ex.parse("YEAR(time)").eval(null) == 2015);
        Assert.assertTrue(ex.parse("DAYOFWEEK(time)").eval(null) == 3);


    }
}
