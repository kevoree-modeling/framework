package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.Distribution;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class GaussianProfiler implements KInferAlg {

    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQUARE = 3;
    //to keep updated
    private static int NUMOFFIELDS = 4;

    int maxTimeSlots = 24; // divide time into 24 hours, 1 gaussian profile every hour

    private int getIndex(int input, int output, int field, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * (meta.origin().inputs().length-1) + 1) + NUMOFFIELDS * input + field;
    }

    private int getCounter(int output, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * (meta.origin().inputs().length-1) + 1) + NUMOFFIELDS * (meta.origin().inputs().length-1);
    }


    public double[] getAvg(int output, Array1D state, KMetaDependencies meta) {
        double[] avg = new double[meta.origin().inputs().length];
        double total = state.get(getCounter(output, meta));
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                avg[i] = state.get(getIndex(i, output, SUM, meta)) / total;
            }
        }
        return avg;
    }

    public double[] getVariance(int output, Array1D state, double[] avg, KMetaDependencies meta) {
        double[] variances = new double[meta.origin().inputs().length];
        double total = state.get(getCounter(output, meta));
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                variances[i] = state.get(getIndex(i, output, SUMSQUARE, meta)) / total - avg[i] * avg[i]; // x count/ (count-1)
            }
        }
        return variances;
    }

    //in the trainingset, first value is time needs to be preprocessed into int 0-23, other values are electrical features, expectedResult is null
    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResult, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        int size = (maxTimeSlots + 1) * ((origin.metaClass().inputs().length - 1) * NUMOFFIELDS + 1);
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, 0, origin.metaClass());
            }
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        //update the state
        for (int i = 0; i < trainingSet.rows(); i++) {
            int output = (int) trainingSet.get(i,0);
            for (int j = 1; j < origin.metaClass().inputs().length; j++) {
                //If this is the first datapoint
                if (state.get(getCounter(output, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j-1, output, MIN, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, output, MAX, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, output, SUM, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i,j) * trainingSet.get(i,j));

                } else {
                    if (trainingSet.get(i,j) < state.get(getIndex(j-1, output, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j-1, output, MIN, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    }
                    if (trainingSet.get(i,j) > state.get(getIndex(j-1, output, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j-1, output, MAX, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    }
                    state.add(getIndex(j-1, output, SUM, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.add(getIndex(j-1, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i,j) * trainingSet.get(i,j));
                }
                //update global stat
                if (state.get(getCounter(maxTimeSlots, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j-1, maxTimeSlots, MIN, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, maxTimeSlots, MAX, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, maxTimeSlots, SUM, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.set(getIndex(j-1, maxTimeSlots, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i,j) * trainingSet.get(i,j));
                } else {
                    if (trainingSet.get(i,j) < state.get(getIndex(j-1, maxTimeSlots, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j-1, maxTimeSlots, MIN, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    }
                    if (trainingSet.get(i,j) > state.get(getIndex(j-1, maxTimeSlots, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j-1, maxTimeSlots, MAX, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    }
                    state.add(getIndex(j-1, maxTimeSlots, SUM, origin.metaClass().dependencies()), trainingSet.get(i,j));
                    state.add(getIndex(j-1, maxTimeSlots, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i,j) * trainingSet.get(i,j));
                }
            }
            //Update Global counters
            state.add(getCounter(output, origin.metaClass().dependencies()), 1);
            state.add(getCounter(maxTimeSlots, origin.metaClass().dependencies()), 1);
        }
    }


    // features: first element is time, other elements are electrical features
    //result is the probability of every point of the profiler needs to be averaged afterward - threshold here is not defined.

    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //check if chunk is empty
        int size = (maxTimeSlots + 1) * ((origin.metaClass().inputs().length - 1) * NUMOFFIELDS + 1);
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        KArray2D result = new NativeArray2D(features.rows(),1);

        for (int j = 0; j < features.rows(); j++) {
            int output = (int) features.get(j,0);

            double[] values = new double[features.columns() - 1];
            for (int i = 0; i < features.columns() - 1; i++) {
                values[i] = features.get(j,i + 1);
            }
            result.set(j,0,getProba(values, output, state, origin.metaClass().dependencies()));
        }
        return result;
    }

    public double getProba(double[] features, int output, Array1D state, KMetaDependencies meta) {
        double prob = 0;
        double[] avg = getAvg(output, state, meta);
        double[] variance = getVariance(output, state, avg, meta);
        prob = Distribution.gaussian(features, avg, variance);
        return prob;
    }


}
