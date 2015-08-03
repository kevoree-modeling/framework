package org.kevoree.modeling.memory.chunk;

/**
 * Created by duke on 04/03/15.
 */
public interface KLongMapCallBack<V> {

    public void on(long key, V value);

}
