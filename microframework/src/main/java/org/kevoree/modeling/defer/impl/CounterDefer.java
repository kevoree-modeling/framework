package org.kevoree.modeling.defer.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.defer.KCounterDefer;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterDefer implements KCounterDefer {

    private final AtomicInteger _nb_down;

    private final int _counter;

    private KCallback _end;

    public CounterDefer(int nb) {
        this._counter = nb;
        this._nb_down = new AtomicInteger(0);
    }

    @Override
    public void countDown() {
        int previous;
        int next;
        do {
            previous = this._nb_down.get();
            next = previous + 1;
        } while (!this._nb_down.compareAndSet(previous, next));
        if (next == _counter) {
            if (_end != null) {
                _end.on(null);
            }
        }
    }

    @Override
    public void then(KCallback p_callback) {
        this._end = p_callback;
        if (this._nb_down.get() == _counter) {
            if (p_callback != null) {
                p_callback.on(null);
            }
        }
    }
}
