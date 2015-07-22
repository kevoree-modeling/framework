package org.kevoree.modeling;

import java.util.List;

public interface KUniverse<A extends KView, B extends KUniverse, C extends KModel> {

    long key();

    A time(long timePoint);

    C model();

    boolean equals(Object other);

    B diverge();

    B origin();

    List<B> descendants();

    void delete(KCallback cb);

    void lookupAllTimes(long uuid, long[] times, KCallback<KObject[]> cb);

    KListener createListener();

}
