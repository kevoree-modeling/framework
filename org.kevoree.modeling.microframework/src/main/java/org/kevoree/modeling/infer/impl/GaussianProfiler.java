package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.Distribution;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

public class GaussianProfiler implements KInferAlg {

    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQUARE = 3;
    //to keep updated
    private static int NUMOFFIELDS = 4;

    int maxTimeSlots = 24; // divide time into 24 hours, 1 gaussian profile every hour

    private int getIndex(int input, int output, int field, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * meta.origin().inputs().length + 1) + NUMOFFIELDS * input + field;
    }

    private int getCounter(int output, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * meta.origin().inputs().length + 1) + NUMOFFIELDS * meta.origin().inputs().length;
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
    public void train(double[][] trainingSet, double[][] expectedResult, KObject origin, KInternalDataManager manager) {
        KMemoryChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
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
        for (int i = 0; i < trainingSet.length; i++) {
            int output = (int) trainingSet[i][0];
            for (int j = 1; j < origin.metaClass().inputs().length; j++) {
                //If this is the first datapoint
                if (state.get(getCounter(output, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j, output, MIN, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, output, MAX, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, output, SUM, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet[i][j] * trainingSet[i][j]);

                } else {
                    if (trainingSet[i][j] < state.get(getIndex(j, output, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, output, MIN, origin.metaClass().dependencies()), trainingSet[i][j]);
                    }
                    if (trainingSet[i][j] > state.get(getIndex(j, output, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, output, MAX, origin.metaClass().dependencies()), trainingSet[i][j]);
                    }
                    state.add(getIndex(j, output, SUM, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.add(getIndex(j, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet[i][j] * trainingSet[i][j]);
                }
                //update global stat
                if (state.get(getCounter(maxTimeSlots, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j, maxTimeSlots, MIN, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, maxTimeSlots, MAX, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, maxTimeSlots, SUM, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.set(getIndex(j, maxTimeSlots, SUMSQUARE, origin.metaClass().dependencies()), trainingSet[i][j] * trainingSet[i][j]);
                } else {
                    if (trainingSet[i][j] < state.get(getIndex(j, maxTimeSlots, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, maxTimeSlots, MIN, origin.metaClass().dependencies()), trainingSet[i][j]);
                    }
                    if (trainingSet[i][j] > state.get(getIndex(j, maxTimeSlots, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, maxTimeSlots, MAX, origin.metaClass().dependencies()), trainingSet[i][j]);
                    }
                    state.add(getIndex(j, maxTimeSlots, SUM, origin.metaClass().dependencies()), trainingSet[i][j]);
                    state.add(getIndex(j, maxTimeSlots, SUMSQUARE, origin.metaClass().dependencies()), trainingSet[i][j] * trainingSet[i][j]);
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
    public double[][] infer(double[][] features, KObject origin, KInternalDataManager manager) {
        KMemoryChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //check if chunk is empty
        int size = (maxTimeSlots + 1) * ((origin.metaClass().inputs().length - 1) * NUMOFFIELDS + 1);
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        double[][] result = new double[features.length][1];

        for (int j = 0; j < features.length; j++) {
            result[j] = new double[1];
            int output = (int) features[j][0];

            double[] values = new double[features[j].length - 1];
            for (int i = 0; i < features[j].length - 1; i++) {
                values[i] = features[j][i + 1];
            }
            result[j][0] = getProba(values, output, state, origin.metaClass().dependencies());
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
