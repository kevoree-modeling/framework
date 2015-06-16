package org.kevoree.modeling.memory;

import org.kevoree.modeling.meta.KMetaModel;

public interface KMemoryElement {

    boolean isDirty();

    void setClean(KMetaModel metaModel);

    void setDirty();

    String serialize(KMetaModel metaModel);

    void init(String payload, KMetaModel metaModel) throws Exception;

    int counter();

    void inc();

    void dec();

    void free(KMetaModel metaModel);

}
