
package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.meta.KMetaModel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @native ts
 * private _isDirty = false;
 * constructor(initialCapacity: number, loadFactor : number) { }
 * public clear():void { for(var p in this){ this._isDirty=true;if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ delete this[p];}} }
 * public get(key:number):number {
 * var resolved = this[key];
 * if(resolved == undefined){ return org.kevoree.modeling.KConfig.NULL_LONG; } else { return resolved; }
 * }
 * public put(key:number, pval : number):void { this._isDirty=true; this[key] = pval;}
 * public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
 * public remove(key:number):number { var tmp = this[key]; delete this[key]; return tmp; }
 * public size():number { return Object.keys(this).length-1; }
 * public each(callback: (p : number, p1 : number) => void): void { for(var p in this){ if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ callback(+p,+this[p]); } } }
 * public isDirty():boolean { return this._isDirty; }
 * public setClean(mm):void { this._isDirty = false; }
 * public setDirty():void { this._isDirty = true; }
 */
public class ArrayLongLongMap implements KLongLongMap {

    protected volatile int elementCount;

    protected volatile int droppedCount;

    protected volatile InternalState state = null;

    protected int threshold;

    private final int initialCapacity;

    private final float loadFactor;
    private AtomicLong _flags = new AtomicLong();


    /**
     * @native ts
     */
    class InternalState {

        public final int elementDataSize;

        public final long[] elementKV;

        public final int[] elementNext;

        public final int[] elementHash;

        public InternalState(int elementDataSize, long[] elementKV, int[] elementNext, int[] elementHash) {
            this.elementDataSize = elementDataSize;
            this.elementKV = elementKV;
            this.elementNext = elementNext;
            this.elementHash = elementHash;
        }
    }

    public ArrayLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initialCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        this.elementCount = 0;
        this.droppedCount = 0;
        InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
        for (int i = 0; i < initialCapacity; i++) {
            newstate.elementNext[i] = -1;
            newstate.elementHash[i] = -1;
        }
        this.state = newstate;
        this.threshold = (int) (newstate.elementDataSize * loadFactor);
    }

    public final void clear() {
        if (elementCount > 0) {
            this.elementCount = 0;
            this.droppedCount = 0;
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
            for (int i = 0; i < initialCapacity; i++) {
                newstate.elementNext[i] = -1;
                newstate.elementHash[i] = -1;
            }
            this.state = newstate;
            this.threshold = (int) (newstate.elementDataSize * loadFactor);
        }
    }

    protected final void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementKV = new long[length * 2];
        System.arraycopy(state.elementKV, 0, newElementKV, 0, state.elementKV.length);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < state.elementNext.length; i++) {
            if (state.elementNext[i] != -1) { //there is a real value
                int index = ((int) state.elementKV[i * 2] & 0x7FFFFFFF) % length;
                int currentHashedIndex = newElementHash[index];
                if (currentHashedIndex != -1) {
                    newElementNext[i] = currentHashedIndex;
                } else {
                    newElementNext[i] = -2; //special char to tag used values
                }
                newElementHash[index] = i;
            }
        }
        //setPrimitiveType value for all
        state = new InternalState(length, newElementKV, newElementNext, newElementHash);
        this.threshold = (int) (length * loadFactor);
    }

    @Override
    public final void each(KLongLongMapCallBack callback) {
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementNext.length; i++) {
            if (internalState.elementNext[i] != -1) { //there is a real value
                callback.on(internalState.elementKV[i * 2], internalState.elementKV[i * 2 + 1]);
            }
        }
    }

    @Override
    public final boolean contains(long key) {
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2] /* getKey */) {
                return m != -1;
            }
            m = internalState.elementNext[m];
        }
        return m != -1;
    }

    @Override
    public final long get(long key) {
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2] /* getKey */) {
                return internalState.elementKV[(m * 2) + 1]; /* getValue */
            } else {
                m = internalState.elementNext[m];
            }
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public final synchronized void put(long key, long value) {
        setDirty();
        int entry = -1;
        int index = -1;
        int hash = (int) (key);
        if (state.elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(state.elementDataSize);
                index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            }
            int newIndex = (this.elementCount + this.droppedCount - 1);
            state.elementKV[newIndex * 2] = key;
            state.elementKV[newIndex * 2 + 1] = value;
            int currentHashedIndex = state.elementHash[index];
            if (currentHashedIndex != -1) {
                state.elementNext[newIndex] = currentHashedIndex;
            } else {
                state.elementNext[newIndex] = -2; //special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            state.elementHash[index] = newIndex;
        } else {
            state.elementKV[entry + 1] = value;/*setValue*/
        }
    }

    final int findNonNullKeyEntry(long key, int index) {
        int m = state.elementHash[index];
        while (m >= 0) {
            if (key == state.elementKV[m * 2] /* getKey */) {
                return m;
            }
            m = state.elementNext[m];
        }
        return -1;
    }

    //TODO check intersection of remove and put
    @Override
    public synchronized final void remove(long key) {
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = state.elementHash[index];
        int last = -1;
        while (m >= 0) {
            if (key == state.elementKV[m * 2] /* getKey */) {
                break;
            }
            last = m;
            m = state.elementNext[m];
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (state.elementNext[m] > 0) {
                state.elementHash[index] = m;
            } else {
                state.elementHash[index] = -1;
            }
        } else {
            state.elementNext[last] = state.elementNext[m];
        }
        state.elementNext[m] = -1;//flag to dropped value
        this.elementCount--;
        this.droppedCount++;
    }

    public final int size() {
        return this.elementCount;
    }

    public boolean isDirty() {
        return (getFlags() & KMemoryElementTypes.DIRTY_BIT) == KMemoryElementTypes.DIRTY_BIT;
    }

    public void setDirty() {
        setFlags(KMemoryElementTypes.DIRTY_BIT, 0);
    }

    public void setClean(KMetaModel metaModel) {
        setFlags(0, KMemoryElementTypes.DIRTY_BIT);
    }

    public long getFlags() {
        return _flags.get();
    }

    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val, nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (_flags.compareAndSet(val, nval));
    }
}



