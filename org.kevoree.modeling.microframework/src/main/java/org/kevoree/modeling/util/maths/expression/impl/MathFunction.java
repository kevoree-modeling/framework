package org.kevoree.modeling.util.maths.expression.impl;

import java.util.Calendar;
import java.util.Date;

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
            long factor = (long) Math.pow(10, p[1]);
            double value = p[0] * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }
        else if(name.equals("FLOOR")) {
            Math.floor(p[0]);
        }
        else if(name.equals("CEILING")){
            Math.ceil(p[0]);
        }
        else if(name.equals("SQRT")){
            return Math.sqrt(p[0]);
        }

        else if(name.equals("SECONDS")){
            return date_to_seconds(p[0]);
        }
        else if(name.equals("MINUTES")){
            return date_to_minutes(p[0]);
        }
        else if(name.equals("HOURS")){
            return date_to_hours(p[0]);
        }
        else if(name.equals("DAY")){
            return date_to_days(p[0]);
        }
        else if(name.equals("MONTH")){
            return date_to_months(p[0]);
        }
        else if(name.equals("YEAR")){
            return date_to_year(p[0]);
        }
        else if(name.equals("DAYOFWEEK")){
            return date_to_dayofweek(p[0]);
        }
        return 0;
    }


    /** @native ts
     * var date = new Date(value);
     * return date.getSeconds();
     */
    private double date_to_seconds(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.SECOND);
    }
    /** @native ts
     * var date = new Date(value);
     * return date.getMinutes();
     */
    private double date_to_minutes(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.MINUTE);
    }

    /** @native ts
     * var date = new Date(value);
     * return date.getHours();
     */
    private double date_to_hours(double value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }


    /** @native ts
     * var date = new Date(value);
     * return date.getDate();
     */
    private double date_to_days(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    //January is 0, december is 11
    /** @native ts
     * var date = new Date(value);
     * return date.getMonth();
     */
    private double date_to_months(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.MONTH);
    }

    /** @native ts
     * var date = new Date(value);
     * return date.getFullYear();
     */
    private double date_to_year(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.YEAR);
    }

    //Sunday is 0, friday is 6
    /** @native ts
     * var date = new Date(value);
     * return date.getDay();
     */
    private double date_to_dayofweek(double value){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date((long)value));
        return calendar.get(Calendar.DAY_OF_WEEK)-1;

    }



}