package org.kevoree.modeling.memory.chunk;

public interface KStringMapCallBack<V> {

    void on(String key, V value);

}
