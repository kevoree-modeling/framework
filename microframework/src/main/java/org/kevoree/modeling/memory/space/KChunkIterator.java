package org.kevoree.modeling.memory.space;

public interface KChunkIterator {

    boolean hasNext();

    long[] next();

    int size();

}
