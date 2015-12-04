package org.kevoree.modeling.memory.strategy;

import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.scheduler.KScheduler;

public interface KMemoryStrategy {

    KChunkSpace newSpace();

    KChunkSpaceManager newSpaceManager(KChunkSpace space, KScheduler scheduler);

}
