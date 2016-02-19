
package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.Base64;
import org.kevoree.modeling.util.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ArrayLongLongMap implements KLongLongMap {

    protected volatile int elementCount;

    protected volatile int droppedCount;

    protected volatile InternalState state = null;

    protected int threshold;

    private final int initialCapacity = 16;

    private static final float loadFactor = ((float) 75 / (float) 100);

    private final AtomicLong _flags;

    private final AtomicInteger _counter;

    private final KChunkSpace _space;

    private final long _universe;

    private final long _time;

    private final long _obj;

    private int _metaClassIndex = -1;

    private volatile long _magic;

    private final AtomicInteger _objectToken;

    public ArrayLongLongMap(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._objectToken = new AtomicInteger(-1);
        this._space = p_space;
        this.elementCount = 0;
        this.droppedCount = 0;
        InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
        for (int i = 0; i < initialCapacity; i++) {
            newstate.elementNext[i] = -1;
            newstate.elementHash[i] = -1;
        }
        this.state = newstate;
        this.threshold = (int) (newstate.elementDataSize * loadFactor);
        this._magic = PrimitiveHelper.rand();
    }

    final class InternalState {

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

    @Override
    public final int counter() {
        return this._counter.get();
    }

    @Override
    public final int inc() {
        int i = this._counter.incrementAndGet();
        System.out.println("counter inc: " + i);
        return i;
    }

    @Override
    public final int dec() {
        int i = this._counter.decrementAndGet();
        System.out.println("counter dec: " + i);
        return i;
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

    @Override
    public long magic() {
        return this._magic;
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
        return this._metaClassIndex;
    }

    @Override
    public boolean tokenCompareAndSwap(int previous, int next) {
        return this._objectToken.compareAndSet(previous, next);
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
                return true;
            }
            m = internalState.elementNext[m];
        }
        return false;
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
            internal_set_dirty();
            this._magic = PrimitiveHelper.rand();
        } else {
            if (state.elementKV[entry + 1] != value) {
                //setValue
                state.elementKV[entry + 1] = value;
                internal_set_dirty();
                this._magic = PrimitiveHelper.rand();
            }
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
        //reset the map
        int length = (nbElement == 0 ? 1 : nbElement << 1);
        long[] newElementKV = new long[length * 2];
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //setPrimitiveType value for all
        InternalState temp_state = new InternalState(length, newElementKV, newElementNext, newElementHash);
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
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % temp_state.elementDataSize;
            //insert K/V
            int newIndex = this.elementCount;
            temp_state.elementKV[newIndex * 2] = loopKey;
            temp_state.elementKV[newIndex * 2 + 1] = loopVal;
            int currentHashedIndex = temp_state.elementHash[index];
            if (currentHashedIndex != -1) {
                temp_state.elementNext[newIndex] = currentHashedIndex;
            } else {
                temp_state.elementNext[newIndex] = -2; //special char to tag used values
            }
            temp_state.elementHash[index] = newIndex;
            this.elementCount++;
        }
        this.elementCount = nbElement;
        this.droppedCount = 0;
        this.state = temp_state;//TODO check with CnS
        this.threshold = (int) (length * loadFactor);

    }

    @Override
    public String serialize(KMetaModel metaModel) {
        final StringBuilder buffer = new StringBuilder();//roughly approximate init size
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
        return KChunkTypes.LONG_LONG_MAP;
    }

    @Override
    public KChunkSpace space() {
        return _space;
    }

    private void internal_set_dirty() {
        this._magic = PrimitiveHelper.rand();
        if (_space != null) {
            if ((_flags.get() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
    }

    @Override
    public long getFlags() {
        return _flags.get();
    }

    @Override
    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
    }

    @Override
    public long universe() {
        return this._universe;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long obj() {
        return this._obj;
    }

}



