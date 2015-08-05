package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.PhantomQueueChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.impl.HeapChunkSpace;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;

public class HeapPhantomQueueMemoryStrategy implements KMemoryStrategy {

    @Override
    public KChunkSpace newSpace() {
        return new HeapChunkSpace();
    }

    @Override
    public KChunkSpaceManager newSpaceManager(KChunkSpace p_space) {
        return new PhantomQueueChunkSpaceManager(p_space);
    }

}
