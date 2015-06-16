package org.kevoree.modeling.infer.states.Bayesian;

/**
 * Created by assaad on 17/02/15.
 */
public class EnumSubstate extends BayesianSubstate{

    public int[] getCounter() {
        return counter;
    }

    public void setCounter(int[] counter) {
        this.counter = counter;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private int[] counter;
    private int total=0;

    public void initialize(int number){
        counter=new int[number];
    }




    @Override
    public double calculateProbability(Object feature) {
        Integer res=(Integer) feature;
        double p=counter[res];
        if(total!=0){
            return p/total;
        }
        else {
            return 0;
        }
    }

    @Override
    public void train(Object feature) {
        Integer res=(Integer) feature;
        counter[res]++;
        total++;
    }

    @Override
    public String save(String separator) {
        if(counter==null||counter.length==0){
            return "EnumSubstate"+separator;
        }
        StringBuilder sb=new StringBuilder();
        sb.append("EnumSubstate"+separator);
        for(int i=0;i<counter.length;i++){
            sb.append(counter[i]+separator);
        }
        return sb.toString();
    }

    @Override
    public void load(String payload, String separator) {
        String[]res=payload.split(separator);
        counter=new int[res.length];
        total=0;
        for(int i=0;i<res.length;i++){
            counter[i]=Integer.parseInt(res[i]);
            total+=counter[i];
        }

    }

    @Override
    public BayesianSubstate cloneState() {
        EnumSubstate cloned =new EnumSubstate();
        int[] newCounter=new int[counter.length];
        for(int i=0;i<counter.length;i++){
            newCounter[i]=counter[i];
        }
        cloned.setCounter(newCounter);
        cloned.setTotal(total);
        return cloned;
    }
}
