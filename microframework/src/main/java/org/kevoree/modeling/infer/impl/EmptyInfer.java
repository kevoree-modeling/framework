package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class EmptyInfer implements KInferAlg {
    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject currentInferObject, KInternalDataManager manager) {
        //NOOP
    }

    @Override
    public KArray2D infer(KArray2D features, KObject currentInferObject, KInternalDataManager manager) {
        KArray2D res = new NativeArray2D(features.rows(), currentInferObject.metaClass().outputs().length);
        for (int i = 0; i < res.rows() * res.columns(); i++) {
            res.setAtIndex(i, 42);
        }
        return res;
    }
}
