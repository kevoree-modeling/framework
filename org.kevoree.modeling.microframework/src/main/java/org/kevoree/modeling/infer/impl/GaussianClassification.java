package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.Distribution;

/**
 * Created by assaad on 08/07/15.
 */
public class GaussianClassification implements KInferAlg {

    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQUARE = 3;
    //to keep updated
    private static int NUMOFFIELDS = 4;




    private int getIndex(int input, int output, int field, KMetaDependencies meta ){
        return output*(NUMOFFIELDS*meta.origin().inputs().length+1)+NUMOFFIELDS*input+field;
    }

    private int getCounter(int output,  KMetaDependencies meta){
        return output*(NUMOFFIELDS*meta.origin().inputs().length+1)+NUMOFFIELDS*meta.origin().inputs().length+NUMOFFIELDS;
    }


    public double[] getAvg(int output, double[] state, KMetaDependencies meta ){
        double[] avg = new double[meta.origin().inputs().length];
        double total = state[getCounter(output, meta)];
        if(total!=0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                avg[i] = state[getIndex(i, output, SUM, meta)] / total;
            }
        }
        return avg;
    }

    public double[] getVariance(int output, double[] state, double[] avg,  KMetaDependencies meta){
        double[] variances = new double[meta.origin().inputs().length];
        double total = state[getCounter(output, meta)];
        if(total!=0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                variances[i] = state[getIndex(i, output, SUMSQUARE, meta)] / total - avg[i]*avg[i];
            }
        }
        return variances;
    }

    @Override
    public void train(double[][] trainingSet, double[] expectedResultSet, KObject origin, KMetaDependencies meta) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        //Create initial segment if empty
        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            ks.extendInfer(meta.index(),meta.origin().outputs().length*(meta.origin().inputs().length*NUMOFFIELDS+1),meta.origin());
        }

        //get the state to double[]
        double[] state = ks.getInfer(meta.index(),meta.origin());

        //update the state
        for(int i=0;i<trainingSet.length;i++) {
            int output = (int) expectedResultSet[i];
            for (int j = 0; j < meta.origin().inputs().length; j++) {
                //If this is the first datapoint
                if (state[getCounter(output, meta)] == 0) {
                    state[getIndex(j,output,MIN, meta)] = trainingSet[i][j];
                    state[getIndex(j,output,MAX, meta)] = trainingSet[i][j];
                    state[getIndex(j,output,SUM, meta)] = trainingSet[i][j];
                    state[getIndex(j,output, SUMSQUARE, meta)] = trainingSet[i][j] * trainingSet[i][j];
                } else {
                    if (trainingSet[i][j] < state[getIndex(j,output,MIN, meta)]) {
                        state[getIndex(j,output,MIN, meta)] = trainingSet[i][j];
                    }
                    if (trainingSet[i][j] > state[getIndex(j,output,MAX, meta)]) {
                        state[getIndex(j,output,MAX, meta)] = trainingSet[i][j];
                    }
                    state[getIndex(j,output,SUM, meta)] += trainingSet[i][j];
                    state[getIndex(j,output, SUMSQUARE, meta)] += trainingSet[i][j] * trainingSet[i][j];
                }
            }
            //Global counter
            state[getCounter(output, meta)]++;
        }
    }

    @Override
    public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {

        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);
        double[] state = ks.getInfer(meta.index(), meta.origin());

        double[] result = new double[1];
        double prob=0;
        double maxprob=0;

        for(int output=0; output<meta.origin().outputs().length;output++){
            double[] avg =getAvg(output,state, meta);
            double[] variance =getVariance(output,state,avg, meta);
            prob= Distribution.gaussian(features,avg,variance);
            if(prob>maxprob){
                maxprob=prob;
                result[0]=output;
            }
        }
        return result;
    }
}
