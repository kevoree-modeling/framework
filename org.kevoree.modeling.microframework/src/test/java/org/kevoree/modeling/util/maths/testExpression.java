package org.kevoree.modeling.util.maths;

/**
 * Created by assaad on 07/07/15.
 */
public class testExpression {
    public static void main(String[] arg){
        Expression ex = new Expression("(3.5+price*8-14/7)%4");
        ex.setVariable("price","5");

        System.out.println(ex.eval());
    }
}
