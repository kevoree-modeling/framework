package org.kevoree.modeling.memory.strategy;

import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;

public interface KMemoryStrategy {

    KChunkSpace newSpace();

    KChunkSpaceManager newSpaceManager(KChunkSpace space);

}
