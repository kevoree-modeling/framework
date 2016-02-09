package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.AbstractKObjectIndex;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

public class GenericObjectIndex extends AbstractKObjectIndex {

    public GenericObjectIndex(long p_universe, long p_time, long p_uuid, KInternalDataManager p_manager, long currentUniverse, long currentTime, long currentUniverseMagic, long currentTimeMagic) {
        super(p_universe, p_time, p_uuid, p_manager, currentUniverse, currentTime, currentUniverseMagic, currentTimeMagic);
    }
}
