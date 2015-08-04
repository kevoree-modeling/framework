
package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of KLongLongMap
 * - memory structure:  | meta class idx (4) | elem count (4) | dropped count (4) | flags (8) | elem data size (4) | counter (4) | back (elem data size * 28) |
 * - back:              | key (8) | value (8) | next (4) | hash (4) |
 */
public class OffHeapLongLongMap implements KLongLongMap, KOffHeapChunk {
    protected static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private OffHeapChunkSpace _space;
    private long _universe, _time, _obj;

    private volatile long _start_address;

    private int initialCapacity;
    private int threshold;
    private float loadFactor;

    // constants for off-heap memory layout
    private static final int ATT_META_CLASS_INDEX_LEN = 4;
    private static final int ATT_ELEM_COUNT_LEN = 4;
    private static final int ATT_DROPPED_COUNT_LEN = 4;
    private static final int ATT_FLAGS_LEN = 8;
    private static final int ATT_ELEM_DATA_SIZE_LEN = 4;
    private static final int ATT_COUNTER_LEN = 4;

    private static final int ATT_KEY_LEN = 8;
    private static final int ATT_VALUE_LEN = 8;
    private static final int ATT_NEXT_LEN = 8;
    private static final int ATT_HASH_LEN = 4;

    private static final int BASE_SEGMENT_LEN =
            ATT_META_CLASS_INDEX_LEN + ATT_ELEM_COUNT_LEN + ATT_DROPPED_COUNT_LEN + ATT_FLAGS_LEN + ATT_ELEM_DATA_SIZE_LEN + ATT_COUNTER_LEN;
    private static final int BACK_ELEM_ENTRY_LEN = ATT_KEY_LEN + ATT_VALUE_LEN + ATT_NEXT_LEN + ATT_HASH_LEN;

    private static final int OFFSET_STARTADDRESS_META_CLASS_INDEX = 0;
    private static final int OFFSET_STARTADDRESS_ELEM_COUNT = OFFSET_STARTADDRESS_META_CLASS_INDEX + ATT_META_CLASS_INDEX_LEN;
    private static final int OFFSET_STARTADDRESS_DROPPED_COUNT = OFFSET_STARTADDRESS_ELEM_COUNT + ATT_ELEM_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_FLAGS = OFFSET_STARTADDRESS_DROPPED_COUNT + ATT_DROPPED_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_ELEM_DATA_SIZE = OFFSET_STARTADDRESS_FLAGS + ATT_FLAGS_LEN;
    private static final int OFFSET_STARTADDRESS_COUNTER = OFFSET_STARTADDRESS_ELEM_DATA_SIZE + ATT_ELEM_DATA_SIZE_LEN;
    private static final int OFFSET_STARTADDRESS_BACK = OFFSET_STARTADDRESS_COUNTER + ATT_COUNTER_LEN;

    private static final int OFFSET_BACK_KEY = 0;
    private static final int OFFSET_BACK_VALUE = OFFSET_BACK_KEY + ATT_KEY_LEN;
    private static final int OFFSET_BACK_NEXT = OFFSET_BACK_VALUE + ATT_VALUE_LEN;
    private static final int OFFSET_BACK_HASH = OFFSET_BACK_NEXT + ATT_NEXT_LEN;

    public OffHeapLongLongMap(OffHeapChunkSpace p_space, long p_universe, long p_time, long p_obj) {
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;

        allocate(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    // TODO this methods are maybe a bottleneck if they are not inlined
    protected int hash(long baseAddress, int index) {
        return UNSAFE.getInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    protected void setHash(long baseAddress, int index, int hash) {
        UNSAFE.putInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), hash);
    }

    protected long key(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY));
    }

    protected void setKey(long baseAddress, int index, long key) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY), key);
    }

    protected long value(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE));
    }

    protected void setValue(long baseAddress, int index, long value) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE), value);
    }

    protected int next(long baseAddress, int index) {
        return UNSAFE.getInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    protected void setNext(long baseAddress, int index, int next) {
        UNSAFE.putInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), next);
    }

    protected void allocate(int p_initalCapacity, float p_loadFactor) {
        this.initialCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;

        int elementDataSize = p_initalCapacity;
        long bytes = BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN;
        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(this._start_address, bytes, (byte) 0);

        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, -1);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, elementDataSize);

        for (int i = 0; i < initialCapacity; i++) {
            // next
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), -1);
            // hash
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), -1);
        }

        this.threshold = (int) (elementDataSize * loadFactor);

        if (this._space != null) {
            this._space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
        }

    }

    public void clear() {
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);

        if (elementCount > 0) {

            long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);

            UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN);

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, initialCapacity);

            for (int i = 0; i < this.initialCapacity; i++) {
                setNext(newAddress, i, -1);
                setHash(newAddress, i, -1);
            }

            long oldAddress = this._start_address;
            this._start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            long elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
            this.threshold = (int) (elementDataSize * this.loadFactor);

            if (this._space != null) {
                this._space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
            }
        }
    }

    protected void rehashCapacity(int p_capacity) {
        int length = (p_capacity == 0 ? 1 : p_capacity << 1);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(newAddress, bytes, (byte) 0);

        long elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

        for (int i = 0; i < length; i++) {
            setNext(newAddress, i, -1);
            setHash(newAddress, i, -1);
        }
        //rehashEveryThing
        for (int i = 0; i < elementDataSize; i++) {
            if (next(this._start_address, i) != -1) { //there is a real value
                int index = ((int) key(this._start_address, i) & 0x7FFFFFFF) % length;
                int currentHashedIndex = hash(newAddress, index);
                if (currentHashedIndex != -1) {
                    setNext(newAddress, i, currentHashedIndex);
                } else {
                    setNext(newAddress, i, -2);
                }
                setHash(newAddress, index, i);
            }
        }

        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        long oldAddress = this._start_address;
        this._start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        this.threshold = (int) (length * this.loadFactor);

        if (this._space != null) {
            _space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
        }

    }

    @Override
    public final void each(KLongLongMapCallBack p_callback) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        for (int i = 0; i < elementDataSize; i++) {
            if (next(this._start_address, i) != -1) { //there is a real value
                p_callback.on(key(this._start_address, i), value(this._start_address, i));
            }
        }
    }

    @Override
    public int metaClassIndex() {
        return UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX);
    }

    @Override
    public final boolean contains(long p_key) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return false;
        }
        int hash = (int) (p_key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;

        int m = hash(this._start_address, index);
        while (m >= 0) {
            long k = key(this._start_address, m);
            if (p_key == k) {
                return m != -1;
            }
            m = next(this._start_address, m);
        }
        return m != -1;
    }

    @Override
    public final long get(long p_key) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        int index = ((int) (p_key) & 0x7FFFFFFF) % elementDataSize;

        int m = hash(this._start_address, index);
        while (m >= 0) {
            long k = key(this._start_address, m);
            if (p_key == k) {
                long v = value(this._start_address, m);
                return v;
            } else {
                m = next(this._start_address, m);
            }
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public final synchronized void put(long p_key, long p_value) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

//        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 1);
        int entry = -1;
        int index = -1;
        int hash = (int) (p_key);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(p_key, index);
        }

        if (entry == -1) {
            // increase elem count
            int oldElementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

            int droppedCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);

            if (elementCount > this.threshold) {
                rehashCapacity(elementDataSize);

                int newElementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
                index = (hash & 0x7FFFFFFF) % newElementDataSize;
            }
            int newIndex = (elementCount + droppedCount - 1);
            setKey(this._start_address, newIndex, p_key);
            setValue(this._start_address, newIndex, p_value);
            int currentHashedIndex = hash(this._start_address, index);
            if (currentHashedIndex != -1) {
                setNext(this._start_address, newIndex, currentHashedIndex);
            } else {
                setNext(this._start_address, newIndex, -2);//special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            setHash(this._start_address, index, newIndex);
        } else {
            setValue(this._start_address, entry, p_value);
        }
    }

    final int findNonNullKeyEntry(long p_key, int p_index) {
        int m = hash(this._start_address, p_index);
        while (m >= 0) {
            if (p_key == key(this._start_address, m)) {
                return m;
            }
            m = next(this._start_address, m);
        }
        return -1;
    }

    //TODO check intersection of remove and put
    @Override
    public synchronized final void remove(long p_key) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return;
        }
        int index = ((int) (p_key) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address, index);
        int last = -1;
        while (m >= 0) {
            if (p_key == key(this._start_address, m)) {
                break;
            }
            last = m;
            m = next(this._start_address, m);
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (next(this._start_address, m) > 0) {
                setHash(this._start_address, index, m);
            } else {
                setHash(this._start_address, index, -1);
            }
        } else {
            setNext(this._start_address, last, next(this._start_address, m));
        }
        setNext(this._start_address, m, -1); //flag to dropped value
        // decrease elem count
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        elementCount--;
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

        // increase dropped count
        int droppedCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
        droppedCount++;
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, droppedCount);
    }

    public final int size() {
        return UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
    }

    @Override
    public final int counter() {
        return UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_COUNTER);
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
        int c = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_COUNTER);
        if (inc) {
            c++;
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_COUNTER, c);
        } else {
            c--;
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_COUNTER, c);
        }
    }

    /* warning: this method is not thread safe */
    @Override
    public void init(String p_payload, KMetaModel p_metaModel, int p_metaClassIndex) {
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, p_metaClassIndex);

        if (p_payload == null || p_payload.length() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < p_payload.length() && p_payload.charAt(cursor) != ',' && p_payload.charAt(cursor) != '/') {
            cursor++;
        }
        if (cursor >= p_payload.length()) {
            return;
        }
        if (p_payload.charAt(cursor) == ',') {//className to parse
            int idx = p_metaModel.metaClassByName(p_payload.substring(initPos, cursor)).index();
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, idx);

            cursor++;
            initPos = cursor;
        }
        while (cursor < p_payload.length() && p_payload.charAt(cursor) != '/') {
            cursor++;
        }
        int nbElement = Base64.decodeToIntWithBounds(p_payload, initPos, cursor);
        //reset the map
        int length = (nbElement == 0 ? 1 : nbElement << 1);

        long newAddress = UNSAFE.allocateMemory(BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN);
        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        for (int i = 0; i < length; i++) {
            setNext(newAddress, i, -1);
            setHash(newAddress, i, -1);
        }

        //setPrimitiveType value for all
        while (cursor < p_payload.length()) {
            cursor++;
            int beginChunk = cursor;
            while (cursor < p_payload.length() && p_payload.charAt(cursor) != ':') {
                cursor++;
            }
            int middleChunk = cursor;
            while (cursor < p_payload.length() && p_payload.charAt(cursor) != ',') {
                cursor++;
            }
            long loopKey = Base64.decodeToLongWithBounds(p_payload, beginChunk, middleChunk);
            long loopVal = Base64.decodeToLongWithBounds(p_payload, middleChunk + 1, cursor);
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % UNSAFE.getInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
            //insert K/V
            int newIndex = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            setKey(newAddress, newIndex, loopKey);
            setValue(newAddress, newIndex, loopVal);

            int currentHashedIndex = hash(newAddress, index);
            if (currentHashedIndex != -1) {
                setNext(newAddress, newIndex, currentHashedIndex);
            } else {
                setNext(newAddress, newIndex, -2);
            }
            setHash(newAddress, index, newIndex);

            int oldElementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);
        }

        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, nbElement);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

        int newElemDataSize = UNSAFE.getInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, newElemDataSize);
        this._start_address = UNSAFE.reallocateMemory(this._start_address, BASE_SEGMENT_LEN + newElemDataSize * BACK_ELEM_ENTRY_LEN);
        UNSAFE.copyMemory(newAddress + OFFSET_STARTADDRESS_BACK, this._start_address + OFFSET_STARTADDRESS_BACK, newElemDataSize * BACK_ELEM_ENTRY_LEN);
        this.threshold = (int) (length * this.loadFactor);

        UNSAFE.freeMemory(newAddress);

    }

    @Override
    public String serialize(KMetaModel p_metaModel) {
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        final StringBuilder buffer = new StringBuilder(elementCount * 8);//roughly approximate init size

        int metaClassIndex = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX);
        if (metaClassIndex != -1) {
            buffer.append(p_metaModel.metaClass(metaClassIndex).metaName());
            buffer.append(',');
        }
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        for (int i = 0; i < elementDataSize; i++) {

            if (next(this._start_address, i) != -1) { //there is a real value
                long loopKey = key(this._start_address, i);
                long loopValue = value(this._start_address, i);
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
    public void free(KMetaModel p_metaModel) {
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

    @Override
    public long getFlags() {
        return UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_FLAGS);
    }

    @Override
    public void setFlags(long p_bitsToEnable, long p_bitsToDisable) {
        long expected;
        long updated;
        do {
            expected = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_FLAGS);
            updated = expected & ~p_bitsToDisable | p_bitsToEnable;
        } while (!UNSAFE.compareAndSwapLong(this, this._start_address + OFFSET_STARTADDRESS_FLAGS, expected, updated));
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

    @Override
    public long memoryAddress() {
        return this._start_address;
    }

    @Override
    public void setMemoryAddress(long p_address) {
        this._start_address = p_address;
        if (this._space != null) {
            _space.notifyRealloc(_start_address, this._universe, this._time, this._obj);
        }
    }
}



