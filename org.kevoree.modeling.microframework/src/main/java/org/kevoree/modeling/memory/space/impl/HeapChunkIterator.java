package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.space.KChunkIterator;

public class HeapChunkIterator implements KChunkIterator {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public KChunk next() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
