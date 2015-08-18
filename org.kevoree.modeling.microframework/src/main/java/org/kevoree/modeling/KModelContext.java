package org.kevoree.modeling;

public interface KModelContext {

    void set(long originTime, long maxTime, long originUniverse, long maxUniverse);

    long originTime();

    long originUniverse();

    long maxTime();

    long maxUniverse();

    void listen(KCallback<long[]> callback);

}
