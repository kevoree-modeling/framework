package org.kevoree.modeling;

public interface KUniverse<A extends KView, B extends KUniverse> {

    long key();

    A time(long timePoint);

    B diverge();

    boolean equals(Object other);
    
    void lookupAllTimes(long uuid, long[] times, KCallback<KObject[]> cb);

    KListener createListener();

}
