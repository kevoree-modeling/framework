
package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.Base64;
import org.kevoree.modeling.util.PrimitiveHelper;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of KLongLongMap: all fields are long (8 byte) fields:
 * http://mail.openjdk.java.net/pipermail/hotspot-compiler-dev/2015-July/018383.html
 * -
 * - memory structure:  | object token | magic | initial capacity | threshold | meta class idx | elem count |
 * -                    | dropped count | flags | elem data size | counter | back |
 * - back:              | key | value | next | hash |
 * -
 */
public class OffHeapLongLongMap implements KLongLongMap, KOffHeapChunk {
    protected static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private PressOffHeapChunkSpace _space;
    private long _universe, _time, _obj;

    private volatile long _start_address;
    private float loadFactor;

    // constants for off-heap memory layout
    private static final long LEN_OBJECT_TOKEN = 8;
    private static final long LEN_MAGIC = 8;
    private static final long LEN_INITIAL_CAPACITY = 8;
    private static final long LEN_THRESHOLD = 8;
    private static final long LEN_META_CLASS_INDEX = 8;
    private static final long LEN_ELEM_COUNT = 8;
    private static final long LEN_DROPPED_COUNT = 8;
    private static final long LEN_FLAGS = 8;
    private static final long LEN_ELEM_DATA_SIZE = 8;
    private static final long LEN_COUNTER = 8;

    private static final long LEN_KEY = 8;
    private static final long LEN_VALUE = 8;
    private static final long LEN_NEXT = 8;
    private static final long LEN_HASH = 8;

    private static final long BASE_SEGMENT_LEN =
            LEN_OBJECT_TOKEN + LEN_MAGIC + LEN_INITIAL_CAPACITY + LEN_THRESHOLD + LEN_META_CLASS_INDEX + LEN_ELEM_COUNT + LEN_DROPPED_COUNT +
                    LEN_FLAGS + LEN_ELEM_DATA_SIZE + LEN_COUNTER;
    private static final long BACK_ELEM_ENTRY_LEN = LEN_KEY + LEN_VALUE + LEN_NEXT + LEN_HASH;

    private static final long OFFSET_STARTADDRESS_OBJECT_TOKEN = 0;
    private static final long OFFSET_STARTADDRESS_MAGIC = OFFSET_STARTADDRESS_OBJECT_TOKEN + LEN_OBJECT_TOKEN;
    private static final long OFFSET_STARTADDRESS_INITIAL_CAPACITY = OFFSET_STARTADDRESS_MAGIC + LEN_MAGIC;
    private static final long OFFSET_STARTADDRESS_THRESHOLD = OFFSET_STARTADDRESS_INITIAL_CAPACITY + LEN_INITIAL_CAPACITY;
    private static final long OFFSET_STARTADDRESS_META_CLASS_INDEX = OFFSET_STARTADDRESS_THRESHOLD + LEN_THRESHOLD;
    private static final long OFFSET_STARTADDRESS_ELEM_COUNT = OFFSET_STARTADDRESS_META_CLASS_INDEX + LEN_META_CLASS_INDEX;
    private static final long OFFSET_STARTADDRESS_DROPPED_COUNT = OFFSET_STARTADDRESS_ELEM_COUNT + LEN_ELEM_COUNT;
    private static final long OFFSET_STARTADDRESS_FLAGS = OFFSET_STARTADDRESS_DROPPED_COUNT + LEN_DROPPED_COUNT;
    private static final long OFFSET_STARTADDRESS_ELEM_DATA_SIZE = OFFSET_STARTADDRESS_FLAGS + LEN_FLAGS;
    private static final long OFFSET_STARTADDRESS_COUNTER = OFFSET_STARTADDRESS_ELEM_DATA_SIZE + LEN_ELEM_DATA_SIZE;
    private static final long OFFSET_STARTADDRESS_BACK = OFFSET_STARTADDRESS_COUNTER + LEN_COUNTER;

    private static final long OFFSET_BACK_KEY = 0;
    private static final long OFFSET_BACK_VALUE = OFFSET_BACK_KEY + LEN_KEY;
    private static final long OFFSET_BACK_NEXT = OFFSET_BACK_VALUE + LEN_VALUE;
    private static final long OFFSET_BACK_HASH = OFFSET_BACK_NEXT + LEN_NEXT;

    public OffHeapLongLongMap(long p_mem_addr, long p_universe, long p_time, long p_obj, PressOffHeapChunkSpace p_space) {
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;

        if (p_mem_addr == -1) {
            allocate(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_MAGIC, PrimitiveHelper.rand());
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_OBJECT_TOKEN, -1);
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_COUNTER, 0);
        } else {
            this._start_address = p_mem_addr;
        }

    }

    // TODO this methods are maybe a bottleneck if they are not inlined
    protected long hash(long p_baseAddress, long p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    protected void setHash(long p_baseAddress, long p_index, long p_hash) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), p_hash);
    }

    protected long key(long p_baseAddress, long p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY));
    }

    protected void setKey(long p_baseAddress, long p_index, long p_key) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY), p_key);
    }

    protected long value(long p_baseAddress, long p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE));
    }

    protected void setValue(long p_baseAddress, long p_index, long p_value) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE), p_value);
    }

    protected long next(long p_baseAddress, long p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    protected void setNext(long p_baseAddress, long p_index, long p_next) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), p_next);
    }

    protected void allocate(long p_initalCapacity, float p_loadFactor) {
        this.loadFactor = p_loadFactor;

        long elementDataSize = p_initalCapacity;
        long bytes = BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN;
        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(this._start_address, bytes, (byte) 0);

        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_INITIAL_CAPACITY, p_initalCapacity);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, -1);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, elementDataSize);

        for (long i = 0; i < p_initalCapacity; i++) {
            // next
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), -1);
            // hash
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), -1);
        }

        long threshold = (long) (elementDataSize * loadFactor);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_THRESHOLD, threshold);

        // don't notify for creation! otherwise the value pointer will be set to the newly created objects for get methods instead to the old one
//        if (this._space != null) {
//            this._space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
//        }

    }

    public void clear() {
        if (this._start_address != 0) {

            long elementCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            long initialCapacity = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_INITIAL_CAPACITY);

            if (elementCount > 0) {
                long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
                long newAddress = UNSAFE.allocateMemory(bytes);

                UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN);

                UNSAFE.putLong(newAddress + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
                UNSAFE.putLong(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
                UNSAFE.putLong(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, initialCapacity);

                for (long i = 0; i < initialCapacity; i++) {
                    setNext(newAddress, i, -1);
                    setHash(newAddress, i, -1);
                }

                long oldAddress = this._start_address;
                this._start_address = newAddress;
                UNSAFE.freeMemory(oldAddress);

                long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
                long threshold = (long) (elementDataSize * this.loadFactor);
                UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_THRESHOLD, threshold);

                if (this._space != null) {
                    this._space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
                }
            }
        }
    }


    @Override
    public long magic() {
        return UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_MAGIC);
    }

    protected void rehashCapacity(long p_capacity) {
        long length = (p_capacity == 0 ? 1 : p_capacity << 1);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(newAddress, bytes, (byte) 0);

        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

        for (long i = 0; i < length; i++) {
            setNext(newAddress, i, -1);
            setHash(newAddress, i, -1);
        }
        //rehashEveryThing
        for (long i = 0; i < elementDataSize; i++) {
            if (next(this._start_address, i) != -1) { //there is a real value
                long index = ((long) key(this._start_address, i) & 0x7FFFFFFF) % length;
                long currentHashedIndex = hash(newAddress, index);
                if (currentHashedIndex != -1) {
                    setNext(newAddress, i, currentHashedIndex);
                } else {
                    setNext(newAddress, i, -2);
                }
                setHash(newAddress, index, i);
            }
        }

        UNSAFE.putLong(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        long oldAddress = this._start_address;
        this._start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        long threshold = (long) (length * this.loadFactor);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_THRESHOLD, threshold);

        if (this._space != null) {
            _space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
        }

    }

    @Override
    public final void each(KLongLongMapCallBack p_callback) {
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        for (long i = 0; i < elementDataSize; i++) {
            if (next(this._start_address, i) != -1) { //there is a real value
                p_callback.on(key(this._start_address, i), value(this._start_address, i));
            }
        }
    }

    @Override
    public int metaClassIndex() {
        return (int) UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX);
    }

    @Override
    public final boolean contains(long p_key) {
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return false;
        }
        long hash = p_key;
        long index = (hash & 0x7FFFFFFF) % elementDataSize;

        long m = hash(this._start_address, index);
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
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        long index = ((long) (p_key) & 0x7FFFFFFF) % elementDataSize;

        long m = hash(this._start_address, index);
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
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        long entry = -1;
        long index = -1;
        long hash = p_key;
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(p_key, index);
        }

        if (entry == -1) {
            // increase elem count
            long oldElementCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            long elementCount = oldElementCount + 1;
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

            long droppedCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);

            long threshold = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_THRESHOLD);
            if (elementCount > threshold) {
                rehashCapacity(elementDataSize);

                long newElementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
                index = (hash & 0x7FFFFFFF) % newElementDataSize;
            }
            long newIndex = (elementCount + droppedCount - 1);
            setKey(this._start_address, newIndex, p_key);
            setValue(this._start_address, newIndex, p_value);
            long currentHashedIndex = hash(this._start_address, index);
            if (currentHashedIndex != -1) {
                setNext(this._start_address, newIndex, currentHashedIndex);
            } else {
                setNext(this._start_address, newIndex, -2);//special char to tag used values
            }
            setHash(this._start_address, index, newIndex);
            internal_set_dirty();
        } else {
            setValue(this._start_address, entry, p_value);
            internal_set_dirty();
        }
    }

    final long findNonNullKeyEntry(long p_key, long p_index) {
        long m = hash(this._start_address, p_index);
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
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return;
        }
        long index = (p_key & 0x7FFFFFFF) % elementDataSize;
        long m = hash(this._start_address, index);
        long last = -1;
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
        long elementCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        elementCount--;
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

        // increase dropped count
        long droppedCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
        droppedCount++;
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, droppedCount);
    }

    @Override
    public final int size() {
        return (int) UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
    }

    @Override
    public final int counter() {
        return (int) UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_COUNTER);
    }

    @Override
    public final int inc() {
        return (int) (UNSAFE.getAndAddLong(null, this._start_address + OFFSET_STARTADDRESS_COUNTER, +1) + 1);
    }

    @Override
    public final int dec() {
        return (int) (UNSAFE.getAndAddLong(null, this._start_address + OFFSET_STARTADDRESS_COUNTER, -1) - 1);
    }


    /* warning: this method is not thread safe */
    @Override
    public void init(String p_payload, KMetaModel p_metaModel, int p_metaClassIndex) {
        // check if we have an old value stored (init not called for the first time)
        if (this._start_address != 0 && p_metaClassIndex == -1) {
            p_metaClassIndex = (int) UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX);
        }
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, p_metaClassIndex);

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
            long idx = p_metaModel.metaClassByName(p_payload.substring(initPos, cursor)).index();
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX, idx);

            cursor++;
            initPos = cursor;
        }
        while (cursor < p_payload.length() && p_payload.charAt(cursor) != '/') {
            cursor++;
        }
        long nbElement = Base64.decodeToLongWithBounds(p_payload, initPos, cursor);
        //reset the map
        long length = (nbElement == 0 ? 1 : nbElement << 1);

        long newAddress = UNSAFE.allocateMemory(BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN);
        UNSAFE.putLong(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        for (long i = 0; i < length; i++) {
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
            long index = (loopKey & 0x7FFFFFFF) % UNSAFE.getLong(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

            long newIndex = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            setKey(newAddress, newIndex, loopKey);
            setValue(newAddress, newIndex, loopVal);

            long currentHashedIndex = hash(newAddress, index);
            if (currentHashedIndex != -1) {
                setNext(newAddress, newIndex, currentHashedIndex);
            } else {
                setNext(newAddress, newIndex, -2);
            }
            setHash(newAddress, index, newIndex);

            long oldElementCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            long elementCount = oldElementCount + 1;
            UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);
        }

        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT, nbElement);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

        long newElemDataSize = UNSAFE.getLong(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, newElemDataSize);
        this._start_address = UNSAFE.reallocateMemory(this._start_address, BASE_SEGMENT_LEN + newElemDataSize * BACK_ELEM_ENTRY_LEN);
        UNSAFE.copyMemory(newAddress + OFFSET_STARTADDRESS_BACK, this._start_address + OFFSET_STARTADDRESS_BACK, newElemDataSize * BACK_ELEM_ENTRY_LEN);

        long threshold = (long) (length * this.loadFactor);
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_THRESHOLD, threshold);

        UNSAFE.freeMemory(newAddress);

    }

    @Override
    public String serialize(KMetaModel p_metaModel) {
        long elementCount = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        final StringBuilder buffer = new StringBuilder((int) (elementCount * 8));//roughly approximate init size

        long metaClassIndex = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_META_CLASS_INDEX);
        if (metaClassIndex != -1) {
            buffer.append(p_metaModel.metaClass((int) metaClassIndex).metaName());
            buffer.append(',');
        }
        Base64.encodeLongToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        long elementDataSize = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        for (long i = 0; i < elementDataSize; i++) {

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
        long val;
        long nval;
        do {
            val = UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_FLAGS);
            nval = val & ~p_bitsToDisable | p_bitsToEnable;
        } while (!UNSAFE.compareAndSwapLong(null, _start_address + OFFSET_STARTADDRESS_FLAGS, val, nval));
    }

    private void internal_set_dirty() {
        UNSAFE.putLong(this._start_address + OFFSET_STARTADDRESS_MAGIC, PrimitiveHelper.rand());

        if (_space != null) {
            if ((UNSAFE.getLong(this._start_address + OFFSET_STARTADDRESS_FLAGS) & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
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

    @Override
    public boolean tokenCompareAndSwap(int previous, int next) {
        return UNSAFE.compareAndSwapLong(null, this._start_address + OFFSET_STARTADDRESS_OBJECT_TOKEN, previous, next);
    }


}