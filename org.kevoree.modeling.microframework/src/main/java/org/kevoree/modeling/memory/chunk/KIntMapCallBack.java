package org.kevoree.modeling.memory.chunk;

/**
 * Created by duke on 04/03/15.
 */
public interface KIntMapCallBack<V> {

    void on(int key, V value);

}
