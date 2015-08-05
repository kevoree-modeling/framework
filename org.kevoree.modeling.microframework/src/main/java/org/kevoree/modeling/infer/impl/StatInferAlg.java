package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class StatInferAlg implements KInferAlg {
    private static int MIN = 0;
    private static int MAX = 1;
    private static int SUM = 2;
    private static int SUMSQuare = 3;
    private static int NUMOFFIELDS = 4;


    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(dependenciesIndex, NUMOFFIELDS * origin.metaClass().inputs().length + 1, origin.metaClass());
            for (int i = 0; i < NUMOFFIELDS * origin.metaClass().inputs().length + 1; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, 0, origin.metaClass());
            }
        }
        Array1D state = new Array1D(NUMOFFIELDS * trainingSet.nbColumns() + 1, 0, dependenciesIndex, ks, origin.metaClass());

        //update the state
        for (int i = 0; i < trainingSet.nbRows(); i++) {
            for (int j = 0; j < origin.metaClass().inputs().length; j++) {
                //If this is the first datapoint
                if (state.get(NUMOFFIELDS * trainingSet.nbColumns()) == 0) {
                    state.set(MIN + j * NUMOFFIELDS, trainingSet.get(i,j));
                    state.set(MAX + j * NUMOFFIELDS, trainingSet.get(i,j));
                    state.set(SUM + j * NUMOFFIELDS, trainingSet.get(i,j));
                    state.set(SUMSQuare + j * NUMOFFIELDS, trainingSet.get(i,j) * trainingSet.get(i,j));
                } else {
                    if (trainingSet.get(i,j) < state.get(MIN + j * NUMOFFIELDS)) {
                        state.set(MIN + j * NUMOFFIELDS, trainingSet.get(i,j));
                    }
                    if (trainingSet.get(i,j) > state.get(MAX + j * NUMOFFIELDS)) {
                        state.set(MAX + j * NUMOFFIELDS, trainingSet.get(i,j));
                    }
                    state.add(SUM + j * NUMOFFIELDS, trainingSet.get(i,j));
                    state.add(SUMSQuare + j * NUMOFFIELDS, trainingSet.get(i,j) * trainingSet.get(i,j));
                }
            }
            //Global counter
            state.add(NUMOFFIELDS * origin.metaClass().inputs().length, 1);
        }
    }


    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        double[] tempres = getAvgAll(ks, origin.metaClass().dependencies());
        KArray2D result = new NativeArray2D(1,tempres.length);
        for(int i=0;i<tempres.length;i++){
            result.set(0,i,tempres[i]);
        }
        return result;
    }

    public double[] getAvgAll(KObjectChunk ks, KMetaDependencies meta) {
        double[] result = new double[meta.origin().inputs().length];
        for (int i = 0; i < meta.origin().inputs().length; i++) {
            result[i] = getAvg(i, ks, meta);
        }
        return result;
    }

    public double[] getMinAll(KObjectChunk ks, KMetaDependencies meta) {
        double[] result = new double[meta.origin().inputs().length];
        for (int i = 0; i < meta.origin().inputs().length; i++) {
            result[i] = getMin(i, ks, meta);
        }
        return result;
    }

    public double[] getMaxAll(KObjectChunk ks, KMetaDependencies meta) {
        double[] result = new double[meta.origin().inputs().length];
        for (int i = 0; i < meta.origin().inputs().length; i++) {
            result[i] = getMax(i, ks, meta);
        }
        return result;
    }

    public double[] getVarianceAll(KObjectChunk ks, KMetaDependencies meta, double[] avgs) {
        double[] result = new double[meta.origin().inputs().length];
        for (int i = 0; i < meta.origin().inputs().length; i++) {
            result[i] = getVariance(i, ks, meta, avgs[i]);
        }
        return result;
    }

    public double getAvg(int featureNum, KObjectChunk ks, KMetaDependencies meta) {

        if (ks.getDoubleArraySize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count = ks.getDoubleArrayElem(meta.index(), ks.getDoubleArraySize(meta.index(), meta.origin()) - 1, meta.origin());
        if (count == 0) {
            return 0;
        }

        return ks.getDoubleArrayElem(meta.index(), featureNum * NUMOFFIELDS + SUM, meta.origin()) / count;
    }

    public double getMin(int featureNum, KObjectChunk ks, KMetaDependencies meta) {
        if (ks.getDoubleArraySize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count = ks.getDoubleArrayElem(meta.index(), ks.getDoubleArraySize(meta.index(), meta.origin()) - 1, meta.origin());
        if (count == 0) {
            return 0;
        }

        return ks.getDoubleArrayElem(meta.index(), featureNum * NUMOFFIELDS + MIN, meta.origin());
    }

    public double getMax(int featureNum, KObjectChunk ks, KMetaDependencies meta) {

        if (ks.getDoubleArraySize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count = ks.getDoubleArrayElem(meta.index(), ks.getDoubleArraySize(meta.index(), meta.origin()) - 1, meta.origin());
        if (count == 0) {
            return 0;
        }

        return ks.getDoubleArrayElem(meta.index(), featureNum * NUMOFFIELDS + MAX, meta.origin());
    }

    public double getVariance(int featureNum, KObjectChunk ks, KMetaDependencies meta, double avg) {
        if (ks.getDoubleArraySize(meta.index(), meta.origin()) == 0) {
            return 0;
        }
        double count = ks.getDoubleArrayElem(meta.index(), ks.getDoubleArraySize(meta.index(), meta.origin()) - 1, meta.origin());
        if (count == 0) {
            return 0;
        }

        return ks.getDoubleArrayElem(meta.index(), featureNum * NUMOFFIELDS + SUMSQuare, meta.origin()) / count - avg * avg;

    }

}
