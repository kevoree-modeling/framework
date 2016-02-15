package org.kevoree.modeling.memory.space.impl.press;

public interface PressFIFO {

    void pushHead(int index);

    int popTail();

    void promoteToHead(int m);

}
