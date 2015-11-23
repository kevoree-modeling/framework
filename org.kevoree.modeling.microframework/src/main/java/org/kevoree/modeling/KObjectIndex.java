package org.kevoree.modeling;

public interface KObjectIndex extends KObject {

    long getIndex(String key);

    void setIndex(String key, long value);

    long[] values();

}
