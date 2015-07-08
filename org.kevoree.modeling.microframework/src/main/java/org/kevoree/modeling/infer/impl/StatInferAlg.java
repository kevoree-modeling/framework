package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.meta.MetaType;

//TODO
public class StatInferAlg implements KInferAlg {

    private static int MIN=0;
    private static int MAX=1;
    private static int SUM=2;
    private static int SUMSQuare=3;
    //to keep updated
    private static int NUMOFFIELDS =4;





    @Override
    public void train(double[][] trainingSet, double[] expectedResultSet, KObject origin, KMetaDependencies meta) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        //Create initial segment if empty
        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            ks.extendInfer(meta.index(), NUMOFFIELDS *trainingSet[0].length+1,meta.origin());
        }

        //get the state to double[]
        double[] state = ks.getInfer(meta.index(),meta.origin());

        //update the state
        for(int i=0;i<trainingSet.length;i++){
            for(int j=0; j<getNumOfInput(meta);j++){
                //If this is the first datapoint
                if(state[NUMOFFIELDS *trainingSet[0].length]==0){
                    state[MIN+j* NUMOFFIELDS]=trainingSet[i][j];
                    state[MAX+j* NUMOFFIELDS]=trainingSet[i][j];
                    state[SUM+j* NUMOFFIELDS]=trainingSet[i][j];
                    state[SUMSQuare+j* NUMOFFIELDS]=trainingSet[i][j]*trainingSet[i][j];
                }

                else{
                    if(trainingSet[i][j]<state[MIN + j * NUMOFFIELDS]) {
                        state[MIN + j * NUMOFFIELDS] = trainingSet[i][j];
                    }
                    if(trainingSet[i][j]>state[MAX + j * NUMOFFIELDS]) {
                        state[MAX + j * NUMOFFIELDS] = trainingSet[i][j];
                    }
                    state[SUM+j* NUMOFFIELDS]+=trainingSet[i][j];
                    state[SUMSQuare+j* NUMOFFIELDS]+=trainingSet[i][j]*trainingSet[i][j];
                }
            }
            //Global counter
            state[NUMOFFIELDS *getNumOfInput(meta)]++;
        }

        //Save the state back to the segment
        for(int i=0;i< NUMOFFIELDS *getNumOfInput(meta)+1;i++){
            ks.setInferElem(meta.index(),i,state[i],meta.origin());
        }


    }

    public int getNumOfInput(KMetaDependencies meta){
        int counter =0;
        for(KMeta km: meta.origin().metaElements()){
            if(km.metaType().equals(MetaType.INPUT)){
                counter++;
            }
        }
        return counter;
    }

    @Override
    public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {
        return getAvgAll(origin,meta);
    }

    public double[] getAvgAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[getNumOfInput(meta)];
        for(int i=0; i<getNumOfInput(meta);i++){
            result[i]=getAvg(i,origin,meta);
        }
        return result;
    }

    public double[] getMinAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[getNumOfInput(meta)];
        for(int i=0; i<getNumOfInput(meta);i++){
            result[i]=getMin(i, origin, meta);
        }
        return result;
    }

    public double[] getMaxAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[getNumOfInput(meta)];
        for(int i=0; i<getNumOfInput(meta);i++){
            result[i]=getMax(i, origin, meta);
        }
        return result;
    }

    public double[] getVarianceAll(KObject origin, KMetaDependencies meta, double[] avgs){
        double[] result = new double[getNumOfInput(meta)];
        for(int i=0; i<getNumOfInput(meta);i++){
            result[i]=getVariance(i, origin, meta, avgs[i]);
        }
        return result;
    }

    public double getAvg(int featureNum, KObject origin, KMetaDependencies meta){
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count=ks.getInferElem(meta.index(),ks.getInferSize(meta.index(),meta.origin())-1,meta.origin());
        if(count==0){
            return 0;
        }

        return ks.getInferElem(meta.index(),featureNum* NUMOFFIELDS +SUM,meta.origin())/count;
    }

    public double getMin(int featureNum, KObject origin, KMetaDependencies meta){
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count=ks.getInferElem(meta.index(),ks.getInferSize(meta.index(),meta.origin())-1,meta.origin());
        if(count==0){
            return 0;
        }

        return ks.getInferElem(meta.index(),featureNum* NUMOFFIELDS +MIN,meta.origin());
    }

    public double getMax(int featureNum, KObject origin, KMetaDependencies meta){
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count=ks.getInferElem(meta.index(),ks.getInferSize(meta.index(),meta.origin())-1,meta.origin());
        if(count==0){
            return 0;
        }

        return ks.getInferElem(meta.index(),featureNum* NUMOFFIELDS +MAX,meta.origin());
    }

    public double getVariance(int featureNum, KObject origin, KMetaDependencies meta, double avg){
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), meta.index(), false, meta.origin(), null);

        if (ks.getInferSize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count=ks.getInferElem(meta.index(),ks.getInferSize(meta.index(),meta.origin())-1,meta.origin());
        if(count==0){
            return 0;
        }

        return ks.getInferElem(meta.index(),featureNum* NUMOFFIELDS +SUMSQuare,meta.origin())/count-avg*avg;

    }

}
