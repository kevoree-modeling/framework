package org.kevoree.modeling;

public interface KPreparedLookup {

    void addLookupOperation(long universe, long time, long uuid);

    long[] flatLookup();

}
