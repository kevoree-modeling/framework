package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KModelContext;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractKModelContext implements KModelContext {

    private AtomicLong _originTime;

    private AtomicLong _originUniverse;

    private AtomicLong _maxTime;

    private AtomicLong _maxUniverse;

    private AtomicReference<KCallback[]> _callbacks;

    public AbstractKModelContext() {
        this._originTime = new AtomicLong(KConfig.NULL_LONG);
        this._originUniverse = new AtomicLong(KConfig.NULL_LONG);
        this._maxTime = new AtomicLong(KConfig.NULL_LONG);
        this._maxUniverse = new AtomicLong(KConfig.NULL_LONG);
        this._callbacks = new AtomicReference<KCallback[]>();
    }

    @Override
    public void set(long p_originTime, long p_maxTime, long p_originUniverse, long p_maxUniverse) {
        this._originTime.set(p_originTime);
        this._maxTime.set(p_maxTime);
        this._originUniverse.set(p_originUniverse);
        this._maxUniverse.set(p_maxUniverse);
        KCallback[] currentStateListeners = this._callbacks.get();
        for (int i = 0; i < currentStateListeners.length; i++) {
            if (currentStateListeners[i] != null) {
                currentStateListeners[i].on(null);
            }
        }
    }

    @Override
    public long originTime() {
        return this._originTime.get();
    }

    @Override
    public long originUniverse() {
        return this._originUniverse.get();
    }

    @Override
    public long maxTime() {
        return this._maxTime.get();
    }

    @Override
    public long maxUniverse() {
        return this._maxUniverse.get();
    }

    @Override
    public void listen(KCallback new_callback) {
        KCallback[] previous;
        KCallback[] next;
        do {
            previous = _callbacks.get();
            int previousSize = 0;
            if (previous != null) {
                previousSize = previous.length;
            }
            next = new KCallback[previousSize + 1];
            next[previousSize] = new_callback;
        } while (!_callbacks.compareAndSet(previous, next));
    }

}
