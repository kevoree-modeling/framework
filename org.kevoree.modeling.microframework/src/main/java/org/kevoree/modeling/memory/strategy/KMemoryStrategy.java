package org.kevoree.modeling.memory.strategy;

import org.kevoree.modeling.memory.cache.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;

public interface KMemoryStrategy {

    KChunkSpace newStorage();

    KChunkSpaceManager newCache(KChunkSpace storage);

}
