package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.PhantomQueueChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;

/**
 * @ignore ts
 */
public class OffHeapMemoryStrategy implements KMemoryStrategy {

    @Override
    public KChunkSpace newSpace() {
        return new OffHeapChunkSpace();
    }

    @Override
    public KChunkSpaceManager newSpaceManager(KChunkSpace p_storage) {
        return new PhantomQueueChunkSpaceManager(p_storage);
    }

}
