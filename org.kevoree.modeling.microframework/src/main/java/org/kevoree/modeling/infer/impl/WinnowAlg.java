package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

import java.util.Random;

public class WinnowAlg implements KInferAlg {
    //TODO to replace by meta-learning parameters
    private double alpha = 2; // the reward parameter
    private double beta = 2; //the penalty parameter
    private int iterations = 1;

    private Random rand = new Random();


    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), (((AbstractKObject) origin).previousResolved()));
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        int size = origin.metaClass().inputs().length;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, rand.nextDouble(), origin.metaClass());
            }
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());

        for (int iter = 0; iter < iterations; iter++) {
            for (int inst = 0; inst < trainingSet.length; inst++) {
                if (calculate(trainingSet[inst], state) == expectedResultSet[inst][0]) {
                    continue;
                }
                //Else update the weights
                if (expectedResultSet[inst][0] == 0) {
                    for (int i = 0; i < size; i++) {
                        state.set(i, state.get(i) / beta);
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        state.set(i, state.get(i) * alpha);
                    }
                }

            }
        }
    }

    private double calculate(double[] features, Array1D state) {
        double result = 0;
        for (int i = 0; i < features.length; i++) {
            result += state.get(i) * features[i];
        }
        if (result >= features.length) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public double[][] infer(double[][] features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), (((AbstractKObject) origin).previousResolved()));
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int size = origin.metaClass().inputs().length;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        double[][] result = new double[features.length][1];
        for (int inst = 0; inst < features.length; inst++) {
            result[inst] = new double[1];
            result[inst][0] = calculate(features[inst], state);
        }
        return result;
    }
}
