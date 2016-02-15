package org.kevoree.modeling.memory.space.impl;

/* TODO this class is not thread safe */
public class FixedSizeLinkedList {

    int[] _previous;
    int[] _next;
    int head;

    public FixedSizeLinkedList(int max) {
        this._previous = new int[max];
        this._next = new int[max];
        head = -1;
    }

    public void pushHead(int index) {
        if (head == -1) {
            this._next[index] = index;
            this._previous[index] = index;
            head = index;
        } else {
            int currentHead = this.head;
            int currentPrevious = this._previous[head];
            head = index;
            //chain previous
            this._previous[index] = currentPrevious;
            this._next[currentPrevious] = index;

            this._previous[currentHead] = index;
            this._next[index] = currentHead;
        }
    }

    public int popTail() {
        int currentHead = head;
        if (currentHead != -1) {
            //circular ring, take previous
            int tail = this._previous[currentHead];

            int previous = this._previous[tail];

            this._next[previous] = head;
            this._previous[head] = previous;

            return tail;
        } else {
            return -1;
        }
    }

    public void promoteToHead(int m) {
        if (m != head) {
            //first extract the key
            int currentNext = this._next[m];
            int currentPrevious = this._previous[m];
            this._next[currentPrevious] = currentNext;
            this._previous[currentNext] = currentPrevious;
            //pushHead
            pushHead(m);
        }
    }

}
