package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class ChunkIterator implements KChunkIterator {

    private final long[] _dirties;
    private final KChunkSpace _origin;

    private int currentIndex = 0;
    private int maxIndex = 0;

    public ChunkIterator(long[] p_dirties, KChunkSpace p_origin) {
        this._dirties = p_dirties;
        this._origin = p_origin;
        maxIndex = p_dirties.length / 3;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < maxIndex;
    }

    @Override
    public KChunk next() {
        KChunk current = null;
        if (currentIndex < maxIndex) {
            current = _origin.get(_dirties[currentIndex*3], _dirties[currentIndex*3 + 1], _dirties[currentIndex*3 + 2]);
        }
        this.currentIndex++;
        return current;
    }

    @Override
    public int size() {
        return this.maxIndex;
    }
}
