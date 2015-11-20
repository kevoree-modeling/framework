package org.kevoree.modeling;

public interface KObjectIndex extends KObject {

    long get(String key);

    void set(String key, long value);

    long[] values();

}
