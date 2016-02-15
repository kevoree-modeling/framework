package org.kevoree.modeling.memory.space;

import org.kevoree.modeling.memory.KChunk;

public interface KChunkIterator {

    boolean hasNext();

    KChunk next();
    //long[] next();

    int size();

}
