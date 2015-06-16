package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.KMetaClass;

public class GenericObject extends AbstractKObject {

    public GenericObject(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KMemoryManager p_manager) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
    }
}
