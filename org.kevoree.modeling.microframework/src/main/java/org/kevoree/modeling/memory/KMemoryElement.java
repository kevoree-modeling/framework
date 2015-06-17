package org.kevoree.modeling.memory;

import org.kevoree.modeling.meta.KMetaModel;

public interface KMemoryElement {

    boolean isDirty();

    void setClean(KMetaModel metaModel);

    void setDirty();

    /**
     * format: definition repeat all entry ...
     * KLongTree: size,root_index[... ,left_index,right_index, parent_index, key, color ...]
     * KLongLongTree: size,root_index[... ,left_index,right_index, parent_index, key, color (black==0), value ...]
     * KMemorySegment: {... ,"name":value ...}
     * KUniverseOrderMap: className, size{... ,"key":value ...}
     * */
    String serialize(KMetaModel metaModel);

    void init(String payload, KMetaModel metaModel);

    int counter();

    void inc();

    void dec();

    void free(KMetaModel metaModel);

}
