package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class PhantomQueueChunkSpaceManager extends AbstractCountingChunkSpaceManager {

    public PhantomQueueChunkSpaceManager(KChunkSpace p_storage) {
        super(p_storage);
    }

    @Override
    public void register(KObject object) {

    }

    @Override
    public void registerAll(KObject[] objects) {

    }
}
