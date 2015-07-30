package org.kevoree.modeling.memory.map;

/**
 * Created by duke on 04/03/15.
 */
public interface KStringMapCallBack<V> {

    void on(String key, V value);

}
