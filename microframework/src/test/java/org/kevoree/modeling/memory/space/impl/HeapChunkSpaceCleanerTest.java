package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.space.BaseKChunkSpaceCleanerTest;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class HeapChunkSpaceCleanerTest extends BaseKChunkSpaceCleanerTest {

    @Override
    public KDataManager createDataManager() {
        return DataManagerBuilder.create().withScheduler(new DirectScheduler()).build();
    }
}
