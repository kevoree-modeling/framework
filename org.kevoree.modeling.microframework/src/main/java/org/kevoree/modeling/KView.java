package org.kevoree.modeling;

import org.kevoree.modeling.format.KModelFormat;
import org.kevoree.modeling.meta.KMetaClass;

public interface KView {

    KObject createByName(String metaClassName);

    KObject create(KMetaClass clazz);

    void select(String query, KCallback<KObject[]> cb);

    void lookup(long key, KCallback<KObject> cb);

    void lookupAll(long[] keys, KCallback<KObject[]> cb);

    long universe();

    long now();

    KModelFormat json();

    KModelFormat xmi();

    boolean equals(Object other);

    void setRoot(KObject elem, KCallback cb);

    void getRoot(KCallback<KObject> cb);

}
