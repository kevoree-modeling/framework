package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModelContext;

import java.util.concurrent.atomic.AtomicReference;

public class AbstractKModelContext implements KModelContext {

    public static final int ORIGIN_TIME = 0;

    public static final int MAX_TIME = 1;

    public static final int ORIGIN_UNIVERSE = 2;

    public static final int MAX_UNIVERSE = 3;

    public static final int NB_ELEM = 4;

    private AtomicReference<KCallback[]> _callbacks;

    private AtomicReference<long[]> _bounds;

    public AbstractKModelContext() {
        this._bounds = new AtomicReference<long[]>();
        this._callbacks = new AtomicReference<KCallback[]>();
    }

    @Override
    public void set(long p_originTime, long p_maxTime, long p_originUniverse, long p_maxUniverse) {
        long[] newBounds = new long[]{p_originTime, p_maxTime, p_originUniverse, p_maxUniverse};
        this._bounds.set(newBounds);
        KCallback[] currentStateListeners = this._callbacks.get();
        if(currentStateListeners != null){
            for (int i = 0; i < currentStateListeners.length; i++) {
                if (currentStateListeners[i] != null) {
                    currentStateListeners[i].on(newBounds);
                }
            }
        }
    }

    @Override
    public long originTime() {
        return this._bounds.get()[ORIGIN_TIME];
    }

    @Override
    public long originUniverse() {
        return this._bounds.get()[ORIGIN_UNIVERSE];
    }

    @Override
    public long maxTime() {
        return this._bounds.get()[MAX_TIME];
    }

    @Override
    public long maxUniverse() {
        return this._bounds.get()[MAX_UNIVERSE];
    }

    @Override
    public void listen(KCallback<long[]> new_callback) {
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
