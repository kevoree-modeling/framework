package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class ManualChunkSpaceManager extends AbstractCountingChunkSpaceManager {

    public ManualChunkSpaceManager(KChunkSpace p_storage) {
        super(p_storage);
    }

    @Override
    public void register(KObject object) {
        //NOOP, objects are managed manually
    }

    @Override
    public void registerAll(KObject[] objects) {
        //NOOP, objects are managed manually
    }

}
