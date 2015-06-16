package org.kevoree.modeling.infer.states;

import org.kevoree.modeling.infer.KInferState;

/**
 * Created by assaad on 10/02/15.
 */
public class DoubleArrayKInferState extends KInferState {
    private boolean _isDirty = false;

    private double[] weights;



    @Override
    public String save() {
        String s="";
        StringBuilder sb=new StringBuilder();
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
                weights = new double[previousState.length];
                for (int i = 0; i < previousState.length; i++) {
                    weights[i] = Double.parseDouble(previousState[i]);
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
        DoubleArrayKInferState cloned = new DoubleArrayKInferState();
        double[] clonearray=new double[weights.length];
        for(int i=0; i<weights.length;i++){
            clonearray[i]=weights[i];
        }
        cloned.setWeights(clonearray);
        return cloned;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
        _isDirty=true;
    }
}
