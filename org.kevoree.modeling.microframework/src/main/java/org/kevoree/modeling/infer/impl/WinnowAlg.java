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

public class WinnowAlg implements KInferAlg {
    //TODO to replace by meta-learning parameters
    private double alpha = 2; // the reward parameter
    private double beta = 2; //the penalty parameter
    private int iterations = 1;

    private Random rand = new Random();


    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject origin, KInternalDataManager manager) {
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
            for (int inst = 0; inst < trainingSet.rows(); inst++) {
                if (calculate(trainingSet,inst, state) == expectedResultSet.get(inst,0)) {
                    continue;
                }
                //Else update the weights
                if (expectedResultSet.get(inst,0) == 0) {
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

    private double calculate(KArray2D features, int row, Array1D state) {
        double result = 0;
        for (int i = 0; i < features.columns(); i++) {
            result += state.get(i) * features.get(row,i);
        }
        if (result >= features.columns()) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public KArray2D infer(KArray2D features, KObject origin, KInternalDataManager manager) {
        KObjectChunk ks = manager.closestChunk(origin.universe(), origin.now(), origin.uuid(), origin.metaClass(), (((AbstractKObject) origin).previousResolved()));
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int size = origin.metaClass().inputs().length;
        if (ks.getDoubleArraySize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size, 0, origin.metaClass().dependencies().index(), ks, origin.metaClass());
        KArray2D result = new NativeArray2D(features.rows(),1);
        for (int inst = 0; inst < features.rows(); inst++) {
            result.set(inst,0, calculate(features,inst, state));
        }
        return result;
    }
}
