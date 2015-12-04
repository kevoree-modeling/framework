package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.Distribution;
import org.kevoree.modeling.util.maths.structure.KArray1D;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

/**
 * Created by assaad on 08/07/15.
 */
public class GaussianClassifierAlg implements KInferAlg {

    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQUARE = 3;
    //to keep updated
    private static int NUMOFFIELDS = 4;


    private int getIndex(int input, int output, int field, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * meta.origin().inputs().length + 1) + NUMOFFIELDS * input + field;
    }

    private int getCounter(int output, KMetaDependencies meta) {
        return output * (NUMOFFIELDS * meta.origin().inputs().length + 1) + NUMOFFIELDS * meta.origin().inputs().length;
    }


    public double[] getAvg(int output, KArray1D state, KMetaDependencies meta) {
        double[] avg = new double[meta.origin().inputs().length];
        double total = state.get(getCounter(output, meta));
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                avg[i] = state.get(getIndex(i, output, SUM, meta)) / total;
            }
        }
        return avg;
    }

    public double[] getVariance(int output, KArray1D state, double[] avg, KMetaDependencies meta) {
        double[] variances = new double[meta.origin().inputs().length];
        double total = state.get(getCounter(output, meta));
        if (total != 0) {
            for (int i = 0; i < meta.origin().inputs().length; i++) {
                variances[i] = state.get(getIndex(i, output, SUMSQUARE, meta)) / total - avg[i] * avg[i]; // x count/ (count-1)
            }
        }
        return variances;
    }

    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
        int outType = origin.metaClass().outputs()[0].attributeTypeId();
        int maxOutput = (origin.manager().model().metaModel().metaTypes()[outType]).literals().length;
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        int size = (maxOutput + 1) * (origin.metaClass().inputs().length * NUMOFFIELDS + 1);
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, 0, origin.metaClass());
            }
        }

        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());

        //update the state
        for (int i = 0; i < trainingSet.rows(); i++) {
            int output = (int) expectedResultSet.get(i, 0);
            for (int j = 0; j < origin.metaClass().inputs().length; j++) {
                //If this is the first datapoint
                if (state.get(getCounter(output, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j, output, MIN, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, output, MAX, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, output, SUM, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i, j) * trainingSet.get(i, j));

                } else {
                    if (trainingSet.get(i, j) < state.get(getIndex(j, output, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, output, MIN, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    }
                    if (trainingSet.get(i, j) > state.get(getIndex(j, output, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, output, MAX, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    }
                    state.add(getIndex(j, output, SUM, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.add(getIndex(j, output, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i, j) * trainingSet.get(i, j));
                }

                //update global stat
                if (state.get(getCounter(maxOutput, origin.metaClass().dependencies())) == 0) {
                    state.set(getIndex(j, maxOutput, MIN, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, maxOutput, MAX, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, maxOutput, SUM, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.set(getIndex(j, maxOutput, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i, j) * trainingSet.get(i, j));
                } else {
                    if (trainingSet.get(i, j) < state.get(getIndex(j, maxOutput, MIN, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, maxOutput, MIN, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    }
                    if (trainingSet.get(i, j) > state.get(getIndex(j, maxOutput, MAX, origin.metaClass().dependencies()))) {
                        state.set(getIndex(j, maxOutput, MAX, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    }
                    state.add(getIndex(j, maxOutput, SUM, origin.metaClass().dependencies()), trainingSet.get(i, j));
                    state.add(getIndex(j, maxOutput, SUMSQUARE, origin.metaClass().dependencies()), trainingSet.get(i, j) * trainingSet.get(i, j));
                }
            }

            //Update Global counters
            state.add(getCounter(output, origin.metaClass().dependencies()), 1);
            state.add(getCounter(maxOutput, origin.metaClass().dependencies()), 1);
        }
    }


    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        int outType = origin.metaClass().outputs()[0].attributeTypeId();
        int maxOutput = (origin.manager().model().metaModel().metaTypes()[outType]).literals().length;
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //check if chunk is empty
        int size = (maxOutput + 1) * (origin.metaClass().inputs().length * NUMOFFIELDS + 1);
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        KArray1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        KArray2D result = new NativeArray2D(features.rows(), 1);

        for (int j = 0; j < features.rows(); j++) {
            double maxprob = 0;
            double prob = 0;
            for (int output = 0; output < maxOutput; output++) {
                prob = getProba(features, j, output, state, origin.metaClass().dependencies());
                if (prob > maxprob) {
                    maxprob = prob;
                    result.set(j, 0, output);
                }
            }
        }
        return result;
    }

    public double getProba(KArray2D features, int row, int output, KArray1D state, KMetaDependencies meta) {
        double prob = 0;
        double[] avg = getAvg(output, state, meta);
        double[] variance = getVariance(output, state, avg, meta);
        prob = Distribution.gaussianArray(features, row, avg, variance);
        return prob;
    }

    public double[] getAllProba(double[] features, Array1D state, KMetaDependencies meta, int maxOutput) {
        double[] results = new double[maxOutput];
        for (int i = 0; i < maxOutput; i++) {
            double[] avg = getAvg(i, state, meta);
            double[] variance = getVariance(i, state, avg, meta);
            results[i] = Distribution.gaussian(features, avg, variance);
        }
        return results;
    }
}
