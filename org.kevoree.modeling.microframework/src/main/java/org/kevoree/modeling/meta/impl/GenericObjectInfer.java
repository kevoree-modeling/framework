package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.AbstractKObjectInfer;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaClass;

public class GenericObjectInfer extends AbstractKObjectInfer {

    public GenericObjectInfer(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KInternalDataManager p_manager, long currentUniverse, long currentTime) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager,currentUniverse,currentTime);
    }
}
