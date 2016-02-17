package org.kevoree.modeling.memory.space.impl.press;

public interface PressFIFO {

    void enqueue(int index);

    int dequeue();

}
