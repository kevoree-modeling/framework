package org.kevoree.modeling.memory.space.impl.press;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixedSizeLinkedList implements PressFIFO {

    private int[] _previous;
    private int[] _next;
    private int _head; //youngest

    AtomicBoolean _lock;

    public FixedSizeLinkedList(int max) {
        this._previous = new int[max];
        this._next = new int[max];
        _head = 0;
        _lock = new AtomicBoolean(false);

        for (int i = 0; i < max; i++) {
            _next[i] = (i + 1) % max;
            _previous[i] = ((i - 1) % max + max) % max;
        }
    }

    private void lock() { //we count on JVM inlining
        while (!_lock.compareAndSet(false, true)) ;
    }

    private void unlock() { //should be invoke after lock succeed
        _lock.compareAndSet(true, false);
    }

    @Override
    public void enqueue(int index) {
        lock();

        int currentHead = this._head;
        int currentPrevious = this._previous[_head];
        _head = index;
        this._previous[index] = currentPrevious;
        this._next[currentPrevious] = index;

        this._previous[currentHead] = index;
        this._next[index] = currentHead;

        unlock();
    }

    @Override
    public int dequeue() {
        lock();

        int currentHead = _head;
        int tail = this._previous[currentHead];
        int previous = this._previous[tail];
        this._next[previous] = _head;
        this._previous[_head] = previous;

        unlock();
        return tail;
    }

    @Override
    public void reenqueue(int index) {
        if(_previous[_next[index]] != index) {//the element has been detached
            return;
        }
        lock();
        //detach the value to reenqueue
        _next[_previous[index]] = _next[index];
        _previous[_next[index]] = _previous[index];

        _previous[index] = _previous[_head];
        _previous[_head] = index;

        _next[_previous[index]] = index;
        _next[index] = _head;

        _head = index;
        unlock();
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("_head=").append(_head).append("\n")
                .append("_next=").append(Arrays.toString(_next)).append("\n")
                .append("_prev=").append(Arrays.toString(_previous));

        return toReturn.toString();
    }

}
