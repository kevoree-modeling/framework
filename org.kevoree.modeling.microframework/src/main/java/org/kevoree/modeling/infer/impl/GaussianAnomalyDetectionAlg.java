package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.Distribution;
import org.kevoree.modeling.util.maths.structure.KArray1D;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

/**
 * Created by assaad on 27/08/15.
 */
public class GaussianAnomalyDetectionAlg implements KInferAlg {

    private double _alpha = 0.5; //default learning rate

    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQUARE = 3;
    //to keep updated
    private static int NUMOFFIELDS = 4;


    public double getProba(KArray2D features, int row, KArray1D state, KMetaDependencies meta) {
        double prob = 0;
        double[] avg = getAvg(state, meta);
        double[] variance = getVariance(state, avg, meta);
        prob = Distribution.gaussianArray(features, row, avg, variance);
        return prob;
    }

    public double[] getAvg(KArray1D state, KMetaDependencies meta) {
        double[] avg = new double[meta.origin().inputs().length];
        double total = state.get(meta.origin().inputs().length*NUMOFFIELDS);
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                avg[i] = state.get(NUMOFFIELDS*i+SUM) / total;
            }
        }
        return avg;
    }

    public double[] getVariance(KArray1D state, double[] avg, KMetaDependencies meta) {
        double[] variances = new double[meta.origin().inputs().length];
        double total = state.get(meta.origin().inputs().length*NUMOFFIELDS);
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                variances[i] = state.get(NUMOFFIELDS*i+SUMSQUARE) / total - avg[i] * avg[i]; // x count/ (count-1)
            }
        }
        return variances;
    }

    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();

        Double alpha=(Double)origin.getByName("alpha");
        if(alpha==null){
            alpha=_alpha;
        }

        int length=origin.metaClass().inputs().length;
        //Create initial chunk if empty
        int size = (length * NUMOFFIELDS + 2); //N for gaussians, 1 for the total counter, and the last for epsilon
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, 0, origin.metaClass());
            }
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());

        //update the state
        for (int i = 0; i < trainingSet.nbRows(); i++) {
            int output = (int) expectedResultSet.get(i, 0); //0: normal, 1:anomaly
            if (output == 0) {
                for (int j = 0; j < origin.metaClass().inputs().length; j++) {
                    if (state.get(length * NUMOFFIELDS) == 0) {
                        state.set(j * NUMOFFIELDS + MIN, trainingSet.get(i, j));
                        state.set(j * NUMOFFIELDS + MAX, trainingSet.get(i, j));
                        state.set(j * NUMOFFIELDS + SUM, trainingSet.get(i, j));
                        state.set(j * NUMOFFIELDS + SUMSQUARE, trainingSet.get(i, j) * trainingSet.get(i, j));

                    } else {
                        if (trainingSet.get(i, j) < state.get(j * NUMOFFIELDS + MIN)) {
                            state.set(j * NUMOFFIELDS + MIN, trainingSet.get(i, j));
                        }
                        if (trainingSet.get(i, j) > state.get(j * NUMOFFIELDS + MAX)) {
                            state.set(j * NUMOFFIELDS + MAX, trainingSet.get(i, j));
                        }
                        state.add(j * NUMOFFIELDS + SUM, trainingSet.get(i, j));
                        state.add(j * NUMOFFIELDS + SUMSQUARE, trainingSet.get(i, j) * trainingSet.get(i, j));
                    }
                }
                state.add(length * NUMOFFIELDS, 1); //update counter
                double newEpsilon=getProba(trainingSet,i,state,origin.metaClass().dependencies());
                double epsilon=state.get(length * NUMOFFIELDS+1);
                if(newEpsilon<epsilon){
                    state.set(length * NUMOFFIELDS+1,epsilon+alpha*(newEpsilon-epsilon));
                }
            }
            else{
                double newEpsilon=getProba(trainingSet,i,state,origin.metaClass().dependencies());
                double epsilon=state.get(length * NUMOFFIELDS+1);
                if(newEpsilon>epsilon){
                    state.set(length * NUMOFFIELDS+1,epsilon+alpha*(newEpsilon-epsilon));
                }

            }
        }
    }

    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int length=origin.metaClass().inputs().length;
        //Create initial chunk if empty
        int size = (length * NUMOFFIELDS + 2); //N for gaussians, 1 for the total counter, and the last for epsilon
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        KArray2D result = new NativeArray2D(features.nbRows(),1);

        double epsilon=state.get(length * NUMOFFIELDS+1);
        for (int i = 0; i < features.nbRows(); i++) {

            if(getProba(features,i, state, origin.metaClass().dependencies())>=epsilon) {
                result.set(i, 0, 0);
            }
            else{
                result.set(i, 0, 1);
            }
        }
        return result;
    }
}
