package org.kevoree.modeling.util.maths.expression.impl;

/**
 * Abstract definition of a supported expression function. A function is
 * defined by a name, the number of parameters and the actual processing
 * implementation.
 */
public class MathFunction {
    /**
     * Name of this function.
     */
    private String name;
    /**
     * Number of parameters expected for this function.
     */
    private int numParams;

    /**
     * Creates a new function with given name and parameter count.
     *
     * @param name      The name of the function.
     * @param numParams The number of parameters for this function.
     */
    public MathFunction(String name, int numParams) {
        this.name = name.toUpperCase();
        this.numParams = numParams;
    }

    public String getName() {
        return name;
    }

    public int getNumParams() {
        return numParams;
    }


    public double eval(double[] p){
        if(name.equals("NOT")){
            return (p[0] == 0) ? 1 : 0;
        }
        else if(name.equals("IF")){
            return !(p[0]==0) ? p[1] : p[2];
        }
        else if(name.equals("RAND")){
            return Math.random();
        }
        else if(name.equals("SIN")){
            return Math.sin(p[0]);
        }
        else if(name.equals("COS")) {
            return Math.cos(p[0]);
        }
        else if(name.equals("TAN")){
            return Math.tan(p[0]);
        }
        else if(name.equals("ASIN")){
            return Math.asin(p[0]);
        }
        else if(name.equals("ACOS")){
            return Math.acos(p[0]);
        }
        else if(name.equals("ATAN")){
            return Math.atan(p[0]);
        }
        else if(name.equals("MAX")){
            return p[0]>p[1] ? p[0] : p[1];
        }
        else if(name.equals("MIN")){
            return p[0]<p[1] ? p[0] : p[1];
        }
        else if(name.equals("ABS")){
            return Math.abs(p[0]);
        }
        else if(name.equals("LOG")){
            return Math.log(p[0]);
        }
        else if(name.equals("ROUND")){
            //todo round
        }
        else if(name.equals("FLOOR")) {
            //todo floor
        }
        else if(name.equals("CEILING")){
            //todo ceiling
        }
        else if(name.equals("SQRT")){
            return Math.sqrt(p[0]);
        }

        return 0;
    }

}