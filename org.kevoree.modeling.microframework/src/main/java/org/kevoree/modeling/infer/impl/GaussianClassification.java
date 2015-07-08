package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
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


    //to replace later
    private int numOfInput;
    private int numOfOutput;

    private int getIndex(int input, int output, int field){
        return output*(NUMOFFIELDS*numOfInput+1)+NUMOFFIELDS*input+field;
    }

    private int getCounter(int output){
        return output*(NUMOFFIELDS*numOfInput+1)+NUMOFFIELDS*numOfInput+NUMOFFIELDS;
    }


    public double[] getAvg(int output, double[] state){
        double[] avg = new double[numOfInput];
        double total = state[getCounter(output)];
        if(total!=0) {
            for (int i = 0; i < numOfInput; i++) {
                avg[i] = state[getIndex(i, output, SUM)] / total;
            }
        }
        return avg;
    }

    public double[] getVariance(int output, double[] state, double[] avg){
        double[] variances = new double[numOfInput];
        double total = state[getCounter(output)];
        if(total!=0) {
            for (int i = 0; i < numOfInput; i++) {
                variances[i] = state[getIndex(i, output, SUMSQUARE)] / total - avg[i]*avg[i];
            }
        }
        return variances;
    }

    @Override
    public void train(double[][] trainingSet, double[] expectedResultSet, KObject origin, KMetaDependencies meta) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        //Create initial segment if empty
        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            ks.extendInfer(meta.index(),numOfOutput*(numOfInput*NUMOFFIELDS+1),meta.origin());
        }

        //get the state to double[]
        double[] state = ks.getInfer(meta.index(),meta.origin());

        //update the state
        for(int i=0;i<trainingSet.length;i++) {
            int output = (int) expectedResultSet[i];
            for (int j = 0; j < numOfInput; j++) {
                //If this is the first datapoint
                if (state[getCounter(output)] == 0) {
                    state[getIndex(j,output,MIN)] = trainingSet[i][j];
                    state[getIndex(j,output,MAX)] = trainingSet[i][j];
                    state[getIndex(j,output,SUM)] = trainingSet[i][j];
                    state[getIndex(j,output, SUMSQUARE)] = trainingSet[i][j] * trainingSet[i][j];
                } else {
                    if (trainingSet[i][j] < state[getIndex(j,output,MIN)]) {
                        state[getIndex(j,output,MIN)] = trainingSet[i][j];
                    }
                    if (trainingSet[i][j] > state[getIndex(j,output,MAX)]) {
                        state[getIndex(j,output,MAX)] = trainingSet[i][j];
                    }
                    state[getIndex(j,output,SUM)] += trainingSet[i][j];
                    state[getIndex(j,output, SUMSQUARE)] += trainingSet[i][j] * trainingSet[i][j];
                }
            }
            //Global counter
            state[getCounter(output)]++;
        }
    }

    @Override
    public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {

        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);
        double[] state = ks.getInfer(meta.index(), meta.origin());

        double[] result = new double[1];
        double prob=0;
        double maxprob=0;

        for(int output=0; output<numOfOutput;output++){
            double[] avg =getAvg(output,state);
            double[] variance =getVariance(output,state,avg);
            prob= Distribution.gaussian(features,avg,variance);
            if(prob>maxprob){
                maxprob=prob;
                result[0]=output;
            }
        }
        return result;
    }
}
