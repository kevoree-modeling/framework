package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.AbstractKView;
import org.kevoree.modeling.memory.manager.KMemoryManager;

class GenericView extends AbstractKView {

    protected GenericView(long p_universe, long _time, KMemoryManager p_manager) {
        super(p_universe, _time, p_manager);
    }

}
