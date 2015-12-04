package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

import java.util.Random;


public class LinearRegressionAlg implements KInferAlg {

    //TODO to replace by meta-learning parameters
    private double alpha = 0.005; //learning rate
    private double gamma = 0.000; //regularization parameter
    private int iterations = 10; //iterations

    private static Random rand = new Random();

    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        int size = origin.metaClass().inputs().length + 1;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, rand.nextDouble(), origin.metaClass());
            }
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());

        for (int i = 0; i < iterations; i++) {
            for (int row = 0; row < trainingSet.rows(); row++) {
                double h = estimate(trainingSet,row, state);
                double error = -alpha * (h - expectedResultSet.get(row,0));

                for (int feature = 0; feature < origin.metaClass().inputs().length; feature++) {
                    state.set(feature, state.get(feature) * (1 - alpha * gamma) + error * trainingSet.get(row,feature));
                }
                state.add(origin.metaClass().inputs().length, error);
            }
        }
    }

    private double estimate(KArray2D training, int row, Array1D state) {
        double result = 0;
        for (int i = 0; i < training.columns(); i++) {
            result = result + training.get(row,i) * state.get(i);
        }
        result = result + state.get(training.columns());
        return result;
    }

    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int size = origin.metaClass().inputs().length + 1;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());

        KArray2D results = new NativeArray2D(features.rows(),1);
        for (int i = 0; i < features.rows(); i++) {
            results.set(i,0, estimate(features,i, state));
        }
        return results;
    }
}
