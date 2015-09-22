package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.util.maths.structure.KArray2D;

/**
 * Created by assaad on 22/09/15.
 */
public class EmptyInfer implements KInferAlg {
    @Override
    public void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject currentInferObject, KInternalDataManager manager) {

    }

    @Override
    public KArray2D infer(KArray2D features, KObject currentInferObject, KInternalDataManager manager) {
        return null;
    }
}
