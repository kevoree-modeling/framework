package org.kevoree.modeling.memory;

import org.kevoree.modeling.meta.KMetaModel;

import java.util.concurrent.atomic.AtomicReference;

public interface KMemoryElement {

    boolean isDirty();

    void setClean(KMetaModel metaModel);

    void setDirty();

    long getFlags();

    void setFlags(long flagsToEnable, long flagsToDisable);

    KMemoryElement next();

    void insertInto(AtomicReference<KMemoryElement> list);

    /**
     * format: definition repeat all entry ...
     * KTree: ]=>red right [=>red left }=>black right {=>black left
     * KLongTree: size,root_index ... SEP key,parent_index ...]
     * KLongLongTree: size,root_index[... SEP key,parent_index,value ...]
     * KMemorySegment: {... ,"name":value ...}
     * KUniverseOrderMap: className, size{... ,"key":value ...}
     */
    String serialize(KMetaModel metaModel);

    void init(String payload, KMetaModel metaModel, int metaClassIndex);

    int counter();

    void inc();

    void dec();

    void free(KMetaModel metaModel);

    short type();
}
