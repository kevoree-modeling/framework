package org.kevoree.modeling.memory.struct.map;

/**
 * Created by duke on 04/03/15.
 */
public interface KLongMapCallBack<V> {

    public void on(long key, V value);

}
