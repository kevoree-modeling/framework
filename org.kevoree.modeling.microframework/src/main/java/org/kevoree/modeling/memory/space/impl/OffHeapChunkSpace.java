package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongLongMap;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongTree;
import org.kevoree.modeling.memory.chunk.impl.OffHeapObjectChunk;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * OffHeap implementation of KChunkSpaceManager
 * - memory structure:  | elementCount (4) | droppedCount (4) | elementDataSize (4) | back (elem data size * 40) |
 * - back:              | universe_key (8)  | time_key (8) | obj_key (8) | next (4) | hash (4) | value_ptr (8) | type (2) |
 */
public class OffHeapChunkSpace implements KChunkSpace {
    private static final Unsafe UNSAFE = getUnsafe();

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

        _start_address = address;
        this.threshold = (int) (initialCapacity * this.loadFactor);
    }

    // TODO this methods are maybe a bottleneck if they are not inlined
    private int hash(long baseAddress, int index) {
        return UNSAFE.getInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    private void setHash(long baseAddress, int index, int hash) {
        UNSAFE.putInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), hash);
    }

    private int next(long baseAddress, int index) {
        return UNSAFE.getInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    private void setNext(long baseAddress, int index, int next) {
        UNSAFE.putInt(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), next);
    }

    private void setUniverse(long baseAddress, int index, long universeKey) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY), universeKey);
    }

    private long universe(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY));
    }

    private void setTime(long baseAddress, int index, long timeKey) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY), timeKey);
    }

    private long time(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY));
    }

    private void setObj(long baseAddress, int index, long objKey) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY), objKey);
    }

    private long obj(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY));
    }

    private long valuePointer(long baseAddress, int index) {
        return UNSAFE.getLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR));
    }

    private void setValuePointer(long baseAddress, int index, long valuePointer) {
        UNSAFE.putLong(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR), valuePointer);
    }

    private void setType(long baseAddress, int index, short type) {
        UNSAFE.putShort(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE), type);
    }

    private short type(long baseAddress, int index) {
        return UNSAFE.getShort(baseAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE));
    }

    private KOffHeapChunk internal_getMemoryElement(long universe, long time, long obj, long baseAddress, int index) {
        KChunk elem = internal_createElement(universe, time, obj, type(baseAddress, index));

        if (!(elem instanceof KOffHeapChunk)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapChunk offheapElem = (KOffHeapChunk) elem;
        offheapElem.setMemoryAddress(valuePointer(baseAddress, index));

        return offheapElem;
    }

    private void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
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

    public final int getIndex(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return -1;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address, index);
        while (m != -1) {
            if (universe == universe(this._start_address, m) && time == time(this._start_address, m) && obj == obj(this._start_address, m)) {
                return m;
            } else {
                m = next(this._start_address, m);
            }
        }
        return -1;
    }

    @Override
    public final KChunk get(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return null;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address, index);
        while (m != -1) {
            if (universe == universe(this._start_address, m) && time == time(this._start_address, m) && obj == obj(this._start_address, m)) {
                return internal_getMemoryElement(universe, time, obj, this._start_address, m); /* getValue */
            } else {
                m = next(this._start_address, m);
            }
        }
        return null;
    }


    @Override
    public final KChunk create(long universe, long time, long obj, short type) {
        KOffHeapChunk newElement = internal_createElement(universe, time, obj, type);
        return internal_put(universe, time, obj, newElement, type);
    }

    public final void notifyRealloc(long newAddress, long universe, long time, long obj) {
        int index = getIndex(universe, time, obj);

        long currentAddress = valuePointer(this._start_address, index);
        if (currentAddress != newAddress) {
            setValuePointer(this._start_address, index, newAddress);
        }
    }

    private KOffHeapChunk internal_createElement(long universe, long time, long obj, short type) {
        switch (type) {
            case KChunkTypes.CHUNK:
                return new OffHeapObjectChunk(this, universe, time, obj);

            case KChunkTypes.LONG_TREE:
                return new OffHeapLongTree(this, universe, time, obj);

            case KChunkTypes.LONG_LONG_TREE:
                return new OffHeapLongLongTree(this, universe, time, obj);

            case KChunkTypes.LONG_LONG_MAP:
                return new OffHeapLongLongMap(this, universe, time, obj);
        }
        return null;
    }


    @Override
    public final KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel) {
        return (KObjectChunk) internal_put(newUniverse, newTime, newObj,
                previousElement.clone(newUniverse, newTime, newObj, metaModel), KChunkTypes.CHUNK);
    }

    private synchronized KChunk internal_put(long universe, long time, long p_obj, KChunk payload, short type) {
        if (!(payload instanceof KOffHeapChunk)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapChunk memoryElement = (KOffHeapChunk) payload;

        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int entry = -1;
        int index = -1;
        int hash = (int) (universe ^ time ^ p_obj);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(universe, time, p_obj, index);
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

            setUniverse(this._start_address, newIndex, universe);
            setTime(this._start_address, newIndex, time);
            setObj(this._start_address, newIndex, p_obj);
            setValuePointer(this._start_address, newIndex, memoryElement.memoryAddress());
            setType(this._start_address, newIndex, type);

            setNext(this._start_address, newIndex, next(this._start_address, index));
            //now the object is reachable to other thread everything should be ready
            setHash(this._start_address, index, newIndex);
            return payload;

        } else {
            return internal_getMemoryElement(universe, time, p_obj, this._start_address, entry);
        }
    }

    final int findNonNullKeyEntry(long universe, long time, long obj, int index) {
        int m = hash(this._start_address, index);
        while (m >= 0) {
            if (universe == universe(this._start_address, m)
                    && time == time(this._start_address, m)
                    && obj == obj(this._start_address, m)) {
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
    public void remove(long universe, long time, long obj, KMetaModel p_metaModel) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int hash = (int) (universe ^ time ^ obj);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        if (elementDataSize == 0) {
            return;
        }
        int m = hash(this._start_address, index);
        int last = -1;
        while (m >= 0) {
            if (universe == universe(this._start_address, m) && time == time(this._start_address, m) && obj == obj(_start_address, m)) {
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
        internal_getMemoryElement(universe, time, obj, this._start_address, m).free(p_metaModel);
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
    public final void clear(KMetaModel metaModel) {
        int elementCount = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);

        if (elementCount > 0) {
            int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            for (int i = 0; i < elementDataSize; i++) {
                if (valuePointer(this._start_address, i) != 0) {
                    long universe = universe(this._start_address, i);
                    long time = time(this._start_address, i);
                    long obj = obj(this._start_address, i);
                    internal_getMemoryElement(universe, time, obj, this._start_address, i).free(metaModel);
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
    public void delete(KMetaModel metaModel) {
        int elementDataSize = UNSAFE.getInt(this._start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long oldAddress = this._start_address;
        this._start_address = -1; //this object should not be used anymore

        for (int i = 0; i < elementDataSize; i++) {
            if (valuePointer(oldAddress, i) != 0) {
                long universe = universe(oldAddress, i);
                long time = time(oldAddress, i);
                long obj = obj(oldAddress, i);
                internal_getMemoryElement(universe, time, obj, oldAddress, i).free(metaModel);
            }
        }

        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        this.threshold = 0;
    }


    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }

}

