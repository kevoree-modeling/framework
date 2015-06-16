package org.kevoree.modeling;

public interface KTimeWalker {

    void allTimes(KCallback<long[]> cb);

    void timesBefore(long endOfSearch,KCallback<long[]> cb);

    void timesAfter(long beginningOfSearch,KCallback<long[]> cb);

    void timesBetween(long beginningOfSearch,long endOfSearch,KCallback<long[]> cb);

}
