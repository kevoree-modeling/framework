package org.kevoree.modeling.infer;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.util.maths.structure.KArray2D;

public interface KInferAlg {

    void train(KArray2D trainingSet, KArray2D expectedResultSet, KObject currentInferObject, KInternalDataManager manager);

    KArray2D infer(KArray2D features, KObject currentInferObject, KInternalDataManager manager);

}
