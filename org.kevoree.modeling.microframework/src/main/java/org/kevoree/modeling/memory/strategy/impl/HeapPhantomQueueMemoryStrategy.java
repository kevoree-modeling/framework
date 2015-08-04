package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.PhantomQueueChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.impl.HeapChunkSpace2;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;

public class HeapPhantomQueueMemoryStrategy implements KMemoryStrategy {

    @Override
    public KChunkSpace newStorage() {
        return new HeapChunkSpace2();
    }

    @Override
    public KChunkSpaceManager newCache(KChunkSpace p_storage) {
        return new PhantomQueueChunkSpaceManager(p_storage);
    }

}
