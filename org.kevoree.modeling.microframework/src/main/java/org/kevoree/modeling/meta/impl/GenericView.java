package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.AbstractKView;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

class GenericView extends AbstractKView {

    protected GenericView(long p_universe, long _time, KInternalDataManager p_manager) {
        super(p_universe, _time, p_manager);
    }

}
