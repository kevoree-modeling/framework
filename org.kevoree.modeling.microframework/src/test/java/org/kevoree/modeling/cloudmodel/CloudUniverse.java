package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.abs.AbstractKUniverse;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.cloudmodel.impl.CloudViewImpl;

/**
 * Created by duke on 10/13/14.
 */
public class CloudUniverse extends AbstractKUniverse<CloudView, CloudUniverse, CloudModel> {

    protected CloudUniverse(long p_universe, KMemoryManager p_manager) {
        super(p_universe, p_manager);
    }

    @Override
    protected CloudView internal_create(long timePoint) {
        return new CloudViewImpl(_universe, timePoint, _manager);
    }

}
