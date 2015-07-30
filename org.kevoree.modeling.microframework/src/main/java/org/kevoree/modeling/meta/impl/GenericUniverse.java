package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KView;
import org.kevoree.modeling.abs.AbstractKUniverse;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

public class GenericUniverse extends AbstractKUniverse {

    GenericUniverse(long p_key, KInternalDataManager p_manager) {
        super(p_key, p_manager);
    }

    @Override
    protected KView internal_create(long timePoint) {
        return new GenericView(_universe, timePoint, _manager);
    }

}
