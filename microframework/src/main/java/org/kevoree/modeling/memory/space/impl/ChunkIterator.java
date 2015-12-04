package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class ChunkIterator implements KChunkIterator {

    private final long[] _dirties;
    private final KChunkSpace _origin;
    private int currentIndex = 0;
    private int maxIndex = 0;
    private final long[] tempKeys;

    public ChunkIterator(long[] p_dirties, KChunkSpace p_origin) {
        this._dirties = p_dirties;
        this._origin = p_origin;
        this.maxIndex = p_dirties.length / 3;
        this.tempKeys = new long[3];
    }

    @Override
    public boolean hasNext() {
        return currentIndex < maxIndex;
    }

    @Override
    public long[] next() {
        if (currentIndex < maxIndex) {
            tempKeys[0] = _dirties[currentIndex * 3];
            tempKeys[1] = _dirties[currentIndex * 3 + 1];
            tempKeys[2] = _dirties[currentIndex * 3 + 2];
        }
        this.currentIndex++;
        return tempKeys;
    }

    @Override
    public int size() {
        return this.maxIndex;
    }
}
