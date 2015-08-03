
package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;

/**
 * @native ts
 * private _counter = 0;
 * private _className : string;
 * constructor(initalCapacity: number, loadFactor : number, p_className : string) { super(initalCapacity,loadFactor);this._className = p_className; }
 * public metaClassName(){ return this._className; }
 * public counter():number { return this._counter; }
 * public inc():void { this._counter++; }
 * public dec():void { this._counter--; }
 * public free():void {  }
 * public size():number { return Object.keys(this).length-3; }
 * public serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string {
 * var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
 * if (this._className != null) {buffer.append(this._className);buffer.append(',');}
 * org.kevoree.modeling.util.maths.Base64.encodeIntToBuffer(this.size(),buffer);
 * buffer.append('/');
 * var isFirst: boolean = true;
 * for (var propKey in this) { if(this.hasOwnProperty(propKey) && propKey[0] != '_'){
 * if (!isFirst) {buffer.append(",");} isFirst = false;
 * org.kevoree.modeling.util.maths.Base64.encodeLongToBuffer(propKey, buffer);
 * buffer.append(":");
 * org.kevoree.modeling.util.maths.Base64.encodeLongToBuffer(this[propKey], buffer);
 * }}
 * return buffer.toString();
 * }
 * public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
 * if (payload == null || payload.length == 0) { return; }
 * var initPos: number = 0; var cursor: number = 0;
 * while (cursor < payload.length && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '/'){ cursor++; }
 * if (cursor >= payload.length) { return; }
 * if (payload.charAt(cursor) == ',') { this._className = payload.substring(initPos, cursor);cursor++;initPos = cursor;}
 * while (cursor < payload.length && payload.charAt(cursor) != '/'){cursor++;}
 * var nbElement: number = java.lang.Integer.parseInt(payload.substring(initPos, cursor));
 * while (cursor < payload.length){
 * cursor++;
 * var beginChunk: number = cursor;
 * while (cursor < payload.length && payload.charAt(cursor) != ':'){cursor++;}
 * var middleChunk: number = cursor;
 * while (cursor < payload.length && payload.charAt(cursor) != ','){cursor++;}
 * var loopKey: number = org.kevoree.modeling.util.maths.Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
 * var loopVal: number = org.kevoree.modeling.util.maths.Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
 * this[loopKey] = loopVal;
 * }
 * }
 * public type(): number { return org.kevoree.modeling.memory.storage.MemoryElementTypes.LONG_LONG_MAP; }
 */
public class ArrayLongLongMap implements KLongLongMap {

    protected volatile int elementCount;

    protected volatile int droppedCount;

    protected volatile InternalState state = null;

    protected int threshold;

    protected boolean _isDirty = false;

    private final int initialCapacity;

    private final float loadFactor;

    private int _metaClassIndex = -1;

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
    public int metaClassIndex() {
        return 0;
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
        this._isDirty = true;
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


    private volatile int _counter = 0;

    @Override
    public final int counter() {
        return this._counter;
    }

    @Override
    public final void inc() {
        internal_counter(true);
    }

    @Override
    public final void dec() {
        internal_counter(false);
    }

    private synchronized void internal_counter(boolean inc) {
        if (inc) {
            this._counter++;
        } else {
            this._counter--;
        }
    }

    @Override
    public boolean isDirty() {
        return _isDirty;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        _isDirty = false;
    }

    @Override
    public void setDirty() {
        this._isDirty = true;
    }

    /* warning: this method is not thread safe */
    @Override
    public void init(String payload, KMetaModel metaModel, int metaClassIndex) {
        _metaClassIndex = metaClassIndex;
        if (payload == null || payload.length() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '/') {
            cursor++;
        }
        if (cursor >= payload.length()) {
            return;
        }
        if (payload.charAt(cursor) == ',') {//className to parse
            _metaClassIndex = metaModel.metaClassByName(payload.substring(initPos, cursor)).index();
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != '/') {
            cursor++;
        }
        int nbElement = Base64.decodeToIntWithBounds(payload, initPos, cursor);
        rehashCapacity(nbElement);
        while (cursor < payload.length()) {
            cursor++;
            int beginChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
                cursor++;
            }
            int middleChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                cursor++;
            }
            long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
            long loopVal = Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % state.elementDataSize;
            //insert K/V
            int newIndex = this.elementCount;
            state.elementKV[newIndex * 2] = loopKey;
            state.elementKV[newIndex * 2 + 1] = loopVal;
            int currentHashedIndex = state.elementHash[index];
            if (currentHashedIndex != -1) {
                state.elementNext[newIndex] = currentHashedIndex;
            } else {
                state.elementNext[newIndex] = -2; //special char to tag used values
            }
            state.elementHash[index] = newIndex;
            this.elementCount++;
        }
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        final StringBuilder buffer = new StringBuilder(elementCount * 8);//roughly approximate init size
        if (_metaClassIndex != -1) {
            buffer.append(metaModel.metaClass(_metaClassIndex).metaName());
            buffer.append(',');
        }
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        ArrayLongLongMap.InternalState internalState = state;
        for (int i = 0; i < internalState.elementNext.length; i++) {
            if (internalState.elementNext[i] != -1) { //there is a real value
                long loopKey = internalState.elementKV[i * 2];
                long loopValue = internalState.elementKV[i * 2 + 1];
                if (!isFirst) {
                    buffer.append(",");
                }
                isFirst = false;
                Base64.encodeLongToBuffer(loopKey, buffer);
                buffer.append(":");
                Base64.encodeLongToBuffer(loopValue, buffer);
            }
        }
        return buffer.toString();
    }

    @Override
    public void free(KMetaModel metaModel) {
        clear();
    }

    @Override
    public short type() {
        return KMemoryElementTypes.LONG_LONG_MAP;
    }

}



