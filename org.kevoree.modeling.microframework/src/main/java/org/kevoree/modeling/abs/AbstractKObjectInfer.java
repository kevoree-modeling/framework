package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.KMetaClass;

public class AbstractKObjectInfer extends AbstractKObject implements KObjectInfer {

    public AbstractKObjectInfer(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KMemoryManager p_manager) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
    }

    @Override
    public void train(KObject[] dependencies, Object[] expectedOutputs, KCallback callback) {
        
    }

    @Override
    public void trainAll(KObject[][] trainingSet, Object[][] expectedResultSet, KCallback callback) {

    }

    @Override
    public void infer(KObject[] features, KCallback<Object[]> callback) {
        //TODO
    }

    @Override
    public void inferAll(KObject[][] features, KCallback<Object[][]> callback) {

    }

    @Override
    public void resetLearning() {
        //TODO
    }

}
