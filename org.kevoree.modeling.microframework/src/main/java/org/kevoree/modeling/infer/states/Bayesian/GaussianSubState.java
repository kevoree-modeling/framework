package org.kevoree.modeling.infer.states.Bayesian;

/**
 * Created by duke on 10/02/15.
 */
public class GaussianSubState extends BayesianSubstate {


    public double getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    private double sumSquares=0;
    private double sum=0;
    private int nb = 0;

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        this.nb = nb;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }


    @Override
    public double calculateProbability(Object feature){

        Double fet=(Double) feature;
        double avg= sum/ nb;
        double variances= sumSquares/nb-avg*avg;
        return (1/Math.sqrt(2*Math.PI*variances))*Math.exp(-((fet-avg)*(fet-avg))/(2*variances));
    }

    public Double getAverage(){
        if(nb!=0) {
            Double avg= sum / nb;
            return avg;
        }
        else
            return null;
    }

    @Override
    public void train(Object feature){
        Double fet=(Double) feature;
        sum += fet;
        sumSquares += fet * fet;
        nb++;
    }

    public Double getVariance(){
        if(nb!=0) {
            double avg= sum / nb;
            //sum / nb
            Double newvar=sumSquares/nb-avg*avg;
            return newvar;
        }
        else
            return null;
    }

    public void clear(){
        nb=0;
        sum=0;
        sumSquares=0;
    }



    @Override
    public String save(String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append("GaussianSubState"+separator);
        sb.append(nb+separator);
        sb.append(sum+separator);
        sb.append(sumSquares);
        return sb.toString();

    }

    @Override
    public void load(String payload,String separator) {
        try {
            String[] previousState = payload.split(separator);
            nb = Integer.parseInt(previousState[0]);
            sum=Double.parseDouble(previousState[1]);
            sumSquares=Double.parseDouble(previousState[2]);

        } catch (Exception e) {
            sum = 0;
            sumSquares=0;
            nb = 0;
        }
    }


    @Override
    public BayesianSubstate cloneState() {
        GaussianSubState cloned = new GaussianSubState();
        cloned.setNb(getNb());
        cloned.setSum(getSum());
        cloned.setSumSquares(getSumSquares());
        return cloned;
    }

}
