package org.kevoree.modeling.infer.states;

import org.kevoree.modeling.infer.KInferState;

/**
 * Created by duke on 10/02/15.
 */
public class AnalyticKInferState extends KInferState {

    private boolean _isDirty = false;

    public double getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    private double sumSquares=0;
    private double sum = 0;
    private int nb = 0;

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        _isDirty=true;
        this.min = min;
    }


    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        _isDirty=true;
        this.max = max;
    }

    private double min;
    private double max;

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        _isDirty = true;
        this.nb = nb;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        _isDirty = true;
        this.sum = sum;
    }


    public Double getAverage(){
        if (nb != 0) {
            return sum / nb;
        }
        else
            return null;
    }

    public void train(double value){
        if(nb==0){
            max = value;
            min = value;
        }
        else{
            if(value<min){
                min = value;
            }
            if(value>max){
                max=value;
            }
        }
        sum+=value;
        sumSquares+=value*value;
        nb++;
        _isDirty=true;
    }

    public Double getVariance(){
        if(nb!=0) {
            double avg= sum / nb;
            double newvar= sumSquares/nb-avg*avg;
            return newvar;
        }
        else
            return null;
    }

    public void clear(){
        nb=0;
        sum=0;
        sumSquares=0;
        _isDirty=true;
    }


    @Override
    public String save() {
        return sum + "/" + nb+ "/" +min+ "/" +max+ "/" +sumSquares;
    }

    @Override
    public void load(String payload) {
        try {
            String[] previousState = payload.split("/");
            sum = Double.parseDouble(previousState[0]);
            nb = Integer.parseInt(previousState[1]);
            min = Double.parseDouble(previousState[2]);
            max = Double.parseDouble(previousState[3]);
            sumSquares= Double.parseDouble(previousState[4]);
        } catch (Exception e) {
            sum = 0;
            nb = 0;
        }
        _isDirty = false;
    }

    @Override
    public boolean isDirty() {
        return _isDirty;
    }

    @Override
    public KInferState cloneState() {
        AnalyticKInferState cloned = new AnalyticKInferState();
        cloned.setSumSquares(getSumSquares());
        cloned.setNb(getNb());
        cloned.setSum(getSum());
        cloned.setMax(getMax());
        cloned.setMin(getMin());
        return cloned;
    }
}
