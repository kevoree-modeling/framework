package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.space.KChunkIterator;

public class HeapChunkIterator implements KChunkIterator {

    private final int[] _indexes;
    private final int _indexEnd;
    private final KChunk[] _backend;

    private int currentIndex = 0;

    public HeapChunkIterator(int[] p_indexes, int p_indexEnd, KChunk[] p_backend) {
        this._indexes = p_indexes;
        this._indexEnd = p_indexEnd;
        this._backend = p_backend;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < _indexEnd;
    }

    @Override
    public KChunk next() {
        KChunk current = _backend[this._indexes[this.currentIndex]];
        this.currentIndex++;
        return current;
    }

    @Override
    public int size() {
        return this._indexEnd;
    }
}
