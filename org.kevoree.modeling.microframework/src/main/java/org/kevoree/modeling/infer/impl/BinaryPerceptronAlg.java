package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

import java.util.Random;


public class BinaryPerceptronAlg implements KInferAlg {
    private int iterations = 5;

    //TODO to replace by meta-learning parameters
    private double alpha = 1; //learning rate

    private Random rand = new Random();

    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.preciseChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), ((AbstractKObject) origin).previousResolved());
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial chunk if empty
        int size = origin.metaClass().inputs().length + 1;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendDoubleArray(origin.metaClass().dependencies().index(), size, origin.metaClass());
            for (int i = 0; i < size; i++) {
                ks.setDoubleArrayElem(dependenciesIndex, i, rand.nextDouble() * 0.1, origin.metaClass());
            }
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        for (int iter = 0; iter < iterations; iter++) {
            for (int row = 0; row < trainingSet.rows(); row++) {
                double h = sigmoid(trainingSet,row, state);
                // double error = alpha *h*(1-h)* (expectedResultSet[row][0] - h);
                double error = alpha * (expectedResultSet.get(row,0) - h);
                for (int j = 0; j < origin.metaClass().inputs().length; j++) {
                    state.add(j, error * trainingSet.get(row,j)); //update for the wi
                }
                state.add(origin.metaClass().inputs().length, error);//for the bias

            }
        }
    }

    private double addUp(KArray2D features, int row, Array1D state) {
        double res = 0;
        for (int i = 0; i < features.columns(); i++) {
            res = res + state.get(i) * features.get(row,i);
        }
        res = res + state.get(features.columns());
        return res;
    }

    private double sigmoid(KArray2D features, int row, Array1D state) {
        return 1 / (1 + Math.exp(-addUp(features, row, state)));
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
        KArray2D result = new NativeArray2D(features.rows(),1);
        for (int inst = 0; inst < features.rows(); inst++) {
            if (sigmoid(features,inst, state) >= 0.5) {
                result.set(inst,0,1.0);
            } else {
                result.set(inst,0,0);
            }
        }
        return result;
    }
}
