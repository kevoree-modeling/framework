package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.meta.MetaType;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

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

        Array1D state = new Array1D(NUMOFFIELDS *trainingSet[0].length+1,0,meta.index(),ks,meta.origin());


        //update the state
        for(int i=0;i<trainingSet.length;i++){
            for(int j=0; j<meta.origin().inputs().length;j++){
                //If this is the first datapoint
                if(state.get(NUMOFFIELDS *trainingSet[0].length)==0){
                    state.set(MIN+j* NUMOFFIELDS,trainingSet[i][j]);
                    state.set(MAX+j* NUMOFFIELDS,trainingSet[i][j]);
                    state.set(SUM+j* NUMOFFIELDS,trainingSet[i][j]);
                    state.set(SUMSQuare+j* NUMOFFIELDS,trainingSet[i][j]*trainingSet[i][j]);
                }

                else{
                    if(trainingSet[i][j]<state.get(MIN + j * NUMOFFIELDS)) {
                        state.set(MIN + j * NUMOFFIELDS, trainingSet[i][j]);
                    }
                    if(trainingSet[i][j]>state.get(MAX + j * NUMOFFIELDS)) {
                        state.set(MAX + j * NUMOFFIELDS , trainingSet[i][j]);
                    }
                    state.add(SUM+j* NUMOFFIELDS, trainingSet[i][j]);
                    state.add(SUMSQuare+j* NUMOFFIELDS, trainingSet[i][j]*trainingSet[i][j]);
                }
            }
            //Global counter
            state.add(NUMOFFIELDS *meta.origin().inputs().length,1);
        }
    }



    @Override
    public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {
        return getAvgAll(origin,meta);
    }

    public double[] getAvgAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[meta.origin().inputs().length];
        for(int i=0; i<meta.origin().inputs().length;i++){
            result[i]=getAvg(i,origin,meta);
        }
        return result;
    }

    public double[] getMinAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[meta.origin().inputs().length];
        for(int i=0; i<meta.origin().inputs().length;i++){
            result[i]=getMin(i, origin, meta);
        }
        return result;
    }

    public double[] getMaxAll(KObject origin, KMetaDependencies meta){
        double[] result = new double[meta.origin().inputs().length];
        for(int i=0; i<meta.origin().inputs().length;i++){
            result[i]=getMax(i, origin, meta);
        }
        return result;
    }

    public double[] getVarianceAll(KObject origin, KMetaDependencies meta, double[] avgs){
        double[] result = new double[meta.origin().inputs().length];
        for(int i=0; i<meta.origin().inputs().length;i++){
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
