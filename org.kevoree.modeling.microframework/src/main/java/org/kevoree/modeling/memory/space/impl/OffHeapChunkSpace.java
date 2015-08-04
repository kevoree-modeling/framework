package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.*;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * OffHeap implementation of KChunkSpaceManager
 * - memory structure:  | elementCount (4) | droppedCount (4) | elementDataSize (4) | back (elem data size * 40) |
 * - back:              | universe_key (8)  | time_key (8) | obj_key (8) | next (4) | hash (4) | value_ptr (8) | type (2) |
 */
public class OffHeapChunkSpace implements KChunkSpace {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    protected volatile long _start_address;

    private int threshold;
    private final float loadFactor;

    // constants for off-heap memory layout
    private static final int ATT_ELEMENT_COUNT_LEN = 4;
    private static final int ATT_DROPPED_COUNT_LEN = 4;
    private static final int ATT_ELEMENT_DATA_SIZE_LEN = 4;

    private static final int ATT_UNIVERSE_KEY_LEN = 8;
    private static final int ATT_TIME_KEY_LEN = 8;
    private static final int ATT_OBJ_KEY_LEN = 8;
    private static final int ATT_NEXT_LEN = 4;
    private static final int ATT_HASH_LEN = 4;
    private static final int ATT_VALUE_PTR_LEN = 8;
    private static final int ATT_TYPE_LEN = 2;

    private static final int OFFSET_STARTADDRESS_ELEMENT_COUNT = 0;
    private static final int OFFSET_STARTADDRESS_DROPPED_COUNT = OFFSET_STARTADDRESS_ELEMENT_COUNT + ATT_ELEMENT_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE = OFFSET_STARTADDRESS_DROPPED_COUNT + ATT_DROPPED_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_BACK = OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE + ATT_ELEMENT_DATA_SIZE_LEN;

    private static final int OFFSET_BACK_UNIVERSE_KEY = 0;
    private static final int OFFSET_BACK_TIME_KEY = OFFSET_BACK_UNIVERSE_KEY + ATT_UNIVERSE_KEY_LEN;
    private static final int OFFSET_BACK_OBJ_KEY = OFFSET_BACK_TIME_KEY + ATT_TIME_KEY_LEN;
    private static final int OFFSET_BACK_NEXT = OFFSET_BACK_OBJ_KEY + ATT_OBJ_KEY_LEN;
    private static final int OFFSET_BACK_HASH = OFFSET_BACK_NEXT + ATT_NEXT_LEN;
    private static final int OFFSET_BACK_VALUE_PTR = OFFSET_BACK_HASH + ATT_HASH_LEN;
    private static final int OFFSET_BACK_TYPE = OFFSET_BACK_VALUE_PTR + ATT_VALUE_PTR_LEN;

    protected static final int BASE_SEGMENT_LEN = ATT_ELEMENT_COUNT_LEN + ATT_DROPPED_COUNT_LEN + ATT_ELEMENT_DATA_SIZE_LEN;
    protected static final int BACK_ELEM_ENTRY_LEN = ATT_UNIVERSE_KEY_LEN + ATT_TIME_KEY_LEN + ATT_OBJ_KEY_LEN + ATT_NEXT_LEN + ATT_HASH_LEN + ATT_VALUE_PTR_LEN + ATT_TYPE_LEN;


    public OffHeapChunkSpace() {
        int initialCapacity = KConfig.CACHE_INIT_SIZE;
        this.loadFactor = KConfig.CACHE_LOAD_FACTOR;

        long address = UNSAFE.allocateMemory(BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN);
        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            setNext(address, i, -1);
            setHash(address, i, -1);
        }

        this._start_address = address;
        this.threshold = (int) (initialCapacity * this.loadFactor);
    }

    // TODO this methods are maybe a bottleneck if they are not inlined
    private int hash(long p_baseAddress, int p_index) {
        return UNSAFE.getInt(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    private void setHash(long p_baseAddress, int p_index, int p_hash) {
        UNSAFE.putInt(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), p_hash);
    }

    private int next(long p_baseAddress, int p_index) {
        return UNSAFE.getInt(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    private void setNext(long p_baseAddress, int p_index, int p_next) {
        UNSAFE.putInt(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), p_next);
    }

    private void setUniverse(long p_baseAddress, int p_index, long p_universeKey) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY), p_universeKey);
    }

    private long universe(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY));
    }

    private void setTime(long p_baseAddress, int p_index, long p_timeKey) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY), p_timeKey);
    }

    private long time(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY));
    }

    private void setObj(long p_baseAddress, int p_index, long p_objKey) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY), p_objKey);
    }

    private long obj(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY));
    }

    private long valuePointer(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR));
    }

    private void setValuePointer(long p_baseAddress, int p_index, long p_valuePointer) {
        UNSAFE.putLong(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR), p_valuePointer);
    }

    private void setType(long p_baseAddress, int p_index, short p_type) {
        UNSAFE.putShort(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE), p_type);
    }

    private short type(long p_baseAddress, int p_index) {
        return UNSAFE.getShort(p_baseAddress + OFFSET_STARTADDRESS_BACK + (p_index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE));
    }

    private KOffHeapChunk internal_getMemoryElement(long p_universe, long p_time, long p_obj, long p_baseAddress, int p_index) {
        KChunk elem = internal_createElement(p_universe, p_time, p_obj, type(p_baseAddress, p_index));

        if (!(elem instanceof KOffHeapChunk)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapChunk offheapElem = (KOffHeapChunk) elem;
        offheapElem.setMemoryAddress(valuePointer(p_baseAddress, p_index));

        return offheapElem;
    }

    private void rehashCapacity(int p_capacity) {
        int length = (p_capacity == 0 ? 1 : p_capacity << 1);
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

        for (int i = 0; i < length; i++) {
            setNext(newAddress, i, -1);
            setHash(newAddress, i, -1);
        }

        //rehashEveryThing
        for (int i = 0; i < elementDataSize; i++) {
            if (valuePointer(newAddress, i) != 0) { //there is a real value
                int hash = (int) (universe(newAddress, i) ^ time(newAddress, i) ^ obj(newAddress, i));
                int index = (hash & 0x7FFFFFFF) % length;
                setNext(newAddress, i, next(newAddress, index));
                setHash(newAddress, index, i);
            }
        }

        //setPrimitiveType value for all
        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, length);

        long oldAddress = this._start_address;
        this._start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        this.threshold = (int) (length * this.loadFactor);
    }

    public final int getIndex(long p_universe, long p_time, long p_obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return -1;
        }
        int index = (((int) (p_universe ^ p_time ^ p_obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address, index);
        while (m != -1) {
            if (p_universe == universe(this._start_address, m) && p_time == time(this._start_address, m) && p_obj == obj(this._start_address, m)) {
                return m;
            } else {
                m = next(this._start_address, m);
            }
        }
        return -1;
    }

    @Override
    public final KChunk get(long p_universe, long p_time, long p_obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return null;
        }
        int index = (((int) (p_universe ^ p_time ^ p_obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address, index);
        while (m != -1) {
            if (p_universe == universe(this._start_address, m) && p_time == time(this._start_address, m) && p_obj == obj(this._start_address, m)) {
                return internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address, m); /* getValue */
            } else {
                m = next(this._start_address, m);
            }
        }
        return null;
    }


    @Override
    public final KChunk create(long p_universe, long p_time, long p_obj, short p_type) {
        KOffHeapChunk newElement = internal_createElement(p_universe, p_time, p_obj, p_type);
        return internal_put(p_universe, p_time, p_obj, newElement, p_type);
    }

    public final void notifyRealloc(long newAddress, long universe, long time, long obj) {
        int index = getIndex(universe, time, obj);
        if (index != -1) {
            long currentAddress = valuePointer(this._start_address, index);
            if (currentAddress != newAddress) {
                setValuePointer(this._start_address, index, newAddress);
            }
        }
    }

    private KOffHeapChunk internal_createElement(long p_universe, long p_time, long p_obj, short p_type) {
        switch (p_type) {
            case KChunkTypes.CHUNK:
                return new OffHeapObjectChunk(this, p_universe, p_time, p_obj);

            case KChunkTypes.LONG_TREE:
                return new OffHeapLongTree(this, p_universe, p_time, p_obj);

            case KChunkTypes.LONG_LONG_TREE:
                return new OffHeapLongLongTree(this, p_universe, p_time, p_obj);

            case KChunkTypes.LONG_LONG_MAP:
                return new OffHeapLongLongMap(this, p_universe, p_time, p_obj);
        }
        return null;
    }


    @Override
    public final KObjectChunk clone(KObjectChunk p_previousElement, long p_newUniverse, long p_newTime, long p_newObj, KMetaModel p_metaModel) {
        return (KObjectChunk) internal_put(p_newUniverse, p_newTime, p_newObj,
                p_previousElement.clone(p_newUniverse, p_newTime, p_newObj, p_metaModel), KChunkTypes.CHUNK);
    }

    private synchronized KChunk internal_put(long p_universe, long p_time, long p_obj, KChunk p_payload, short p_type) {
        if (!(p_payload instanceof KOffHeapChunk)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapChunk memoryElement = (KOffHeapChunk) p_payload;

        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int entry = -1;
        int index = -1;
        int hash = (int) (p_universe ^ p_time ^ p_obj);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(p_universe, p_time, p_obj, index);
        }
        if (entry == -1) {
            int oldElementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT, elementCount);

            if (elementCount > this.threshold) {
                rehashCapacity(elementDataSize);
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }

            int droppedCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
            int newIndex = (elementCount - 1 + droppedCount);

            setUniverse(this._start_address, newIndex, p_universe);
            setTime(this._start_address, newIndex, p_time);
            setObj(this._start_address, newIndex, p_obj);
            setValuePointer(this._start_address, newIndex, memoryElement.memoryAddress());
            setType(this._start_address, newIndex, p_type);

            setNext(this._start_address, newIndex, next(this._start_address, index));
            //now the object is reachable to other thread everything should be ready
            setHash(this._start_address, index, newIndex);
            return p_payload;

        } else {
            return internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address, entry);
        }
    }

    final int findNonNullKeyEntry(long p_universe, long p_time, long p_obj, int p_index) {
        int m = hash(this._start_address, p_index);
        while (m >= 0) {
            if (p_universe == universe(this._start_address, m)
                    && p_time == time(this._start_address, m)
                    && p_obj == obj(this._start_address, m)) {
                return m;
            }
            m = next(this._start_address, m);
        }
        return -1;
    }

    @Override
    public final int size() {
        return UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
    }

    @Override
    public KChunkIterator detachDirties() {
        // TODO
        return null;
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {
        // TODO
    }

    @Override
    public void remove(long p_universe, long p_time, long p_obj, KMetaModel p_metaModel) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int hash = (int) (p_universe ^ p_time ^ p_obj);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        if (elementDataSize == 0) {
            return;
        }
        int m = hash(this._start_address, index);
        int last = -1;
        while (m >= 0) {
            if (p_universe == universe(this._start_address, m) && p_time == time(this._start_address, m) && p_obj == obj(_start_address, m)) {
                break;
            }
            last = m;
            m = next(this._start_address, m);
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (next(this._start_address, m) != -1) {
                setHash(this._start_address, index, m);
            } else {
                setHash(this._start_address, index, -1);
            }
        } else {
            setNext(this._start_address, last, next(this._start_address, m));
        }
        setNext(this._start_address, m, -1);//flag to dropped value
        internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address, m).free(p_metaModel);
        setValuePointer(this._start_address, m, 0);

        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT, elementCount - 1);
        int droppedCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
        UNSAFE.putInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, droppedCount + 1);

        if (droppedCount > this.threshold * this.loadFactor) {
            compact();
        }
    }

    private void compact() {
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
        int droppedCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);

        if (droppedCount > 0) {
            int length = (elementCount == 0 ? 1 : elementCount << 1); //take the next size of element count
            int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);
            UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN);

            int currentIndex = 0;
            for (int i = 0; i < length; i++) {
                setNext(newAddress, i, -1);
                setHash(newAddress, i, -1);
            }

            for (int i = 0; i < elementDataSize; i++) {
                if (valuePointer(this._start_address, i) != 0) {
                    long l_uni = universe(this._start_address, i);
                    long l_time = time(this._start_address, i);
                    long l_obj = obj(this._start_address, i);
                    short l_type = type(this._start_address, i);

                    KOffHeapChunk loopElement = internal_getMemoryElement(l_uni, l_time, l_obj, this._start_address, i);

                    setValuePointer(newAddress, currentIndex, loopElement.memoryAddress());
                    setUniverse(newAddress, currentIndex, l_uni);
                    setTime(newAddress, currentIndex, l_time);
                    setObj(newAddress, currentIndex, l_obj);
                    setType(newAddress, currentIndex, l_type);

                    int hash = (int) (l_uni ^ l_time ^ l_obj);
                    int index = (hash & 0x7FFFFFFF) % length;
                    setNext(newAddress, currentIndex, hash(newAddress, index));
                    setHash(newAddress, index, currentIndex);
                    currentIndex++;

                }
            }

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, length);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, currentIndex);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

            long oldAddress = this._start_address;
            this._start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            this.threshold = (int) (length * this.loadFactor);
        }
    }

    @Override
    public final void clear(KMetaModel p_metaModel) {
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);

        if (elementCount > 0) {
            int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            for (int i = 0; i < elementDataSize; i++) {
                if (valuePointer(this._start_address, i) != 0) {
                    long universe = universe(this._start_address, i);
                    long time = time(this._start_address, i);
                    long obj = obj(this._start_address, i);
                    internal_getMemoryElement(universe, time, obj, this._start_address, i).free(p_metaModel);
                }
            }
            int initialCapacity = KConfig.CACHE_INIT_SIZE;
            long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);
            UNSAFE.setMemory(newAddress, bytes, (byte) 0);

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, initialCapacity);
            for (int i = 0; i < initialCapacity; i++) {
                setNext(newAddress, i, -1);
                setHash(newAddress, i, -1);
            }

            long oldAddress = this._start_address;
            this._start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            this.threshold = (int) (elementDataSize * loadFactor);

        }
    }

    @Override
    public void delete(KMetaModel p_metaModel) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long oldAddress = this._start_address;
        this._start_address = -1; //this object should not be used anymore

        for (int i = 0; i < elementDataSize; i++) {
            if (valuePointer(oldAddress, i) != 0) {
                long universe = universe(oldAddress, i);
                long time = time(oldAddress, i);
                long obj = obj(oldAddress, i);
                internal_getMemoryElement(universe, time, obj, oldAddress, i).free(p_metaModel);
            }
        }

        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        this.threshold = 0;
    }

}

