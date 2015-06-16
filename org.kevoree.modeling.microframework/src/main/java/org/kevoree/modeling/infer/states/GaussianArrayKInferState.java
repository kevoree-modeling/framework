package org.kevoree.modeling.infer.states;

import org.kevoree.modeling.infer.KInferState;

/**
 * Created by duke on 10/02/15.
 */
public class GaussianArrayKInferState extends KInferState {

    private boolean _isDirty = false;

    public double[] getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double[] sumSquares) {
        this.sumSquares = sumSquares;
    }

    private double[] sumSquares=null;
    private double[] sum=null;
    private double epsilon=0;
    private int nb = 0;

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        _isDirty = true;
        this.nb = nb;
    }

    public double[] getSum() {
        return sum;
    }

    public void setSum(double[] sum) {
        _isDirty = true;
        this.sum = sum;
    }


    public double calculateProbability(double[] features){
        int size=sum.length;
        double[] avg= new double[size];
        double[] variances= new double[size];
        double p=1;
        for(int i=0;i<size;i++){
            avg[i]=sum[i] / nb;
            variances[i]=sumSquares[i]/nb-avg[i]*avg[i];
            p= p* (1/Math.sqrt(2*Math.PI*variances[i]))*Math.exp(-((features[i]-avg[i])*(features[i]-avg[i]))/(2*variances[i]));
        }
        return p;
    }

    public boolean infer(double[] features){
        return (calculateProbability(features)<=epsilon);
    }

    public Double[] getAverage(){
        if(nb!=0) {
            int size=sum.length;
            Double[] avg= new Double[size];
            for(int i=0;i<size;i++){
                avg[i]=sum[i] / nb;
            }
            return avg;
        }
        else
            return null;
    }

    public void train(double[] features, boolean result, double alpha){
        int size=features.length;
        if(nb==0){
            sumSquares=new double[size];
            sum=new double[size];
        }

        for(int i=0;i<size;i++) {
            sum[i] += features[i];
            sumSquares[i] += features[i] * features[i];
        }
        nb++;

        double proba=calculateProbability(features);
        double diff=proba-epsilon;
        //Update epsilon when the guess is wrong
        if((proba<epsilon && result==false)||(proba>epsilon && result==true)){
            epsilon=epsilon+alpha*diff;
        }
        _isDirty=true;
    }

    public Double[] getVariance(){
        if(nb!=0) {
            int size=sum.length;
            double[] avg= new double[size];
            //sum / nb
            Double[] newvar= new Double[size];
            for(int i=0;i<size;i++){
                avg[i]=sum[i] / nb;
                newvar[i]=sumSquares[i]/nb-avg[i]*avg[i];
            }
            return newvar;
        }
        else
            return null;
    }

    public void clear(){
        nb=0;
        sum=null;
        sumSquares=null;
        _isDirty=true;
    }


    @Override
    public String save() {
        StringBuilder sb = new StringBuilder();
        sb.append(nb+"/");
        sb.append(epsilon+"/");
        int size=sumSquares.length;
        for(int i=0;i<size;i++){
            sb.append(sum[i]+"/");
        }
        for(int i=0;i<size;i++){
            sb.append(sumSquares[i]+"/");
        }
        return sb.toString();

    }

    @Override
    public void load(String payload) {
        try {
            String[] previousState = payload.split("/");
            nb = Integer.parseInt(previousState[0]);
            epsilon=Double.parseDouble(previousState[1]);
            int size=(previousState.length-2)/2;
            sum=new double[size];
            sumSquares=new double[size];
            for(int i=0;i<size;i++){
               sum[i]=Double.parseDouble(previousState[i+2]);
            }
            for(int i=0;i<size;i++){
                sumSquares[i]=Double.parseDouble(previousState[i+2+size]);
            }

        } catch (Exception e) {
            sum = null;
            sumSquares=null;
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
        GaussianArrayKInferState cloned = new GaussianArrayKInferState();
        cloned.setNb(getNb());
        if(nb!=0) {
            double[] newSum = new double[sum.length];
            double[] newSumSquares = new double[sumSquares.length];
            for (int i = 0; i < sum.length; i++) {
                newSum[i] = sum[i];
                newSumSquares[i] = sumSquares[i];
            }
            cloned.setSum(newSum);
            cloned.setSumSquares(newSumSquares);
        }
        return cloned;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
