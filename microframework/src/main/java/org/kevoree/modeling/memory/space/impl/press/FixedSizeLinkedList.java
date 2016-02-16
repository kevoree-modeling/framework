package org.kevoree.modeling.memory.space.impl.press;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/* TODO this class is not thread safe */
public class FixedSizeLinkedList implements PressFIFO {

    private int[] _previous;
    private int[] _next;
    private int _head;
    private Random _random;
    private AtomicInteger _magic;

    public FixedSizeLinkedList(int max) {
        this._previous = new int[max];
        this._next = new int[max];
        _head = -1;
        _random = new Random();
        _magic = new AtomicInteger(-1);
    }

    @Override
    public void pushHead(int index) {

        int localMagic;
        do {
            localMagic = _random.nextInt();
        } while (!_magic.compareAndSet(-1, localMagic));

        if (_head == -1) {
            this._next[index] = index;
            this._previous[index] = index;
            _head = index;
        } else {
            int currentHead = this._head;
            int currentPrevious = this._previous[_head];
            _head = index;
            //chain previous
            this._previous[index] = currentPrevious;
            this._next[currentPrevious] = index;

            this._previous[currentHead] = index;
            this._next[index] = currentHead;
        }

        _magic.compareAndSet(localMagic, -1);

    }

    @Override
    public int popTail() {

        int localMagic;
        do {
            localMagic = _random.nextInt();
        } while (!_magic.compareAndSet(-1, localMagic));

        int currentHead = _head;
        if (currentHead != -1) {
            //circular ring, take previous
            int tail = this._previous[currentHead];
            int previous = this._previous[tail];
            this._next[previous] = _head;
            this._previous[_head] = previous;
            _magic.compareAndSet(localMagic, -1);
            return tail;
        } else {
            _magic.compareAndSet(localMagic, -1);
            return -1;
        }
    }

    @Override
    public void promoteToHead(int index) {
        /*
        if (index != _head) {

            int localMagic;
            do {
                localMagic = _random.nextInt();
            } while (!_magic.compareAndSet(-1, localMagic));

            //first extract the key
            int currentNext = this._next[index];
            int currentPrevious = this._previous[index];
            this._next[currentPrevious] = currentNext;
            this._previous[currentNext] = currentPrevious;

            int currentHead = this._head;
            int currentPreviousHead = this._previous[currentHead];
            _head = index;
            //chain previous of head
            this._previous[index] = currentPreviousHead;
            this._next[currentPreviousHead] = index;
            //chain head
            this._previous[currentHead] = index;
            this._next[index] = currentHead;

            _magic.compareAndSet(localMagic, -1);

        }*/
    }

}
