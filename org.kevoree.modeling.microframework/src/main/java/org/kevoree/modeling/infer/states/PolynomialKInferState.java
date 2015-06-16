package org.kevoree.modeling.infer.states;

import org.kevoree.modeling.infer.KInferState;

/**
 * Created by assaad on 10/02/15.
 */
public class PolynomialKInferState extends KInferState {
    private boolean _isDirty = false;

    public long getTimeOrigin() {
        return timeOrigin;
    }

    public void setTimeOrigin(long timeOrigin) {
        this.timeOrigin = timeOrigin;
    }

    public boolean is_isDirty() {
        return _isDirty;
    }

    public long getUnit() {
        return unit;
    }

    public void setUnit(long unit) {
        this.unit = unit;
    }

    private long timeOrigin;
    private long unit;
    private double[] weights;


    public static double maxError(double[] coef, double[] normalizedTimes, double[] results) {
        double maxErr = 0;
        double temp = 0;

        for (int i = 0; i < normalizedTimes.length; i++) {
            double val = internal_extrapolate(normalizedTimes[i], coef);
            temp = Math.abs(val - results[i]);
            if (temp > maxErr) {
                maxErr = temp;
            }
        }
        return maxErr;
    }

    private static double internal_extrapolate(double normalizedTime, double[] coef) {
        double result = 0;
        double power = 1;
        for (int j = 0; j < coef.length; j++) {
            result += coef[j] * power;
            power = power * normalizedTime;
        }
        return result;
    }


    @Override
    public String save() {
        String s="";
        StringBuilder sb=new StringBuilder();
        sb.append(timeOrigin+"/");
        sb.append(unit+"/");
        if(weights!=null) {
            for (int i = 0; i < weights.length; i++) {
                sb.append(weights[i]+"/");
            }
            s=sb.toString();
        }

        return s;
    }

    @Override
    public void load(String payload) {
        try {
            String[] previousState = payload.split("/");
            if(previousState.length>0) {
                timeOrigin = Long.parseLong(previousState[0]);
                unit=Long.parseLong(previousState[1]);
                int size=previousState.length-2;
                weights = new double[size];
                for (int i = 0; i <  size; i++) {
                    weights[i] = Double.parseDouble(previousState[i-2]);
                }
            }

        } catch (Exception e) {
        }
        _isDirty = false;
    }

    @Override
    public boolean isDirty() {
        return _isDirty;
    }

    public void set_isDirty(boolean value){
        _isDirty=value;
    }


    @Override
    public KInferState cloneState() {
        PolynomialKInferState cloned = new PolynomialKInferState();
        double[] clonearray=new double[weights.length];
        for(int i=0; i<weights.length;i++){
            clonearray[i]=weights[i];
        }
        cloned.setWeights(clonearray);
        cloned.setTimeOrigin(getTimeOrigin());
        cloned.setUnit(getUnit());
        return cloned;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
        _isDirty=true;
    }

    public Object infer(long time) {
        double t= ((double)(time-timeOrigin))/unit;
        return internal_extrapolate(t,weights);

    }
}
