package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.abs.AbstractKUniverse;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.cloudmodel.impl.CloudViewImpl;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

/**
 * Created by duke on 10/13/14.
 */
public class CloudUniverse extends AbstractKUniverse<CloudView, CloudUniverse, CloudModel> {

    protected CloudUniverse(long p_universe, KInternalDataManager p_manager) {
        super(p_universe, p_manager);
    }

    @Override
    protected CloudView internal_create(long timePoint) {
        return new CloudViewImpl(_universe, timePoint, _manager);
    }

}
