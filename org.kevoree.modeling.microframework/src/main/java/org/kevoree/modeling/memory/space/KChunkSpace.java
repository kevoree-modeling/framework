package org.kevoree.modeling.memory.space;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaModel;

public interface KChunkSpace {

    KChunk get(long universe, long time, long obj);

    KChunk create(long universe, long time, long obj, short type);

    KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel);

    void clear(KMetaModel metaModel);

    void delete(KMetaModel metaModel);

    void remove(long universe, long time, long obj, KMetaModel metaModel);

    int size();


}
