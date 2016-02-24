package org.kevoree.modeling.memory.space.impl.press;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedHeapFIFO implements PressFIFO {

    private int[] _next;
    private AtomicInteger _head;
    private AtomicInteger _tail;

    public FixedHeapFIFO(int maxElem) {
        this._next = new int[maxElem];
        this._head = new AtomicInteger(-1);
        this._tail = new AtomicInteger(-1);
        for (int i = 0; i < maxElem; i++) {
            if (i != maxElem - 1) {
                _next[i] = i + 1;
            }
        }
        _head.set(0);
        _tail.set(maxElem - 1);
    }

    @Override
    public int dequeue() {
        int currentHead;
        int currentHeadNext;
        do {
            currentHead = this._head.get();
            currentHeadNext = this._next[currentHead];
        } while (!this._head.compareAndSet(currentHead, currentHeadNext));
        //cleanup link to avoid pollution
        return currentHead;
    }

    @Override
    public void enqueue(int index) {
        int currentTail;
        do {
            currentTail = this._tail.get();
        } while (!this._tail.compareAndSet(currentTail, index));
        this._next[index] = currentTail;
    }

    @Override
    public void reenqueue(int index) {
        //todo
        throw new RuntimeException("NotImplementedException");
    }
}
