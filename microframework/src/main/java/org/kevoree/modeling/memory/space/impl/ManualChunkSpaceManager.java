package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;

public class ManualChunkSpaceManager extends AbstractCountingChunkSpaceManager {

    @Override
    public void register(KObject object) {
        if (_metaModel == null) {
            _metaModel = object.manager().model().metaModel();
        }
        //NOOP, objects are managed manually
    }

    @Override
    public void registerAll(KObject[] objects) {
        //NOOP, objects are managed manually
    }

}
