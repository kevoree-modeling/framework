package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.OffHeapMemoryChunk;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongTree;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * OffHeap implementation of KCache
 * - memory structure:  | elementCount (4) | droppedCount (4) | elementDataSize (4) | back (elem data size * 40) |
 * - back:              | universe_key (8)  | time_key (8) | obj_key (8) | next (4) | hash (4) | value_ptr (8) | type (2) |
 */
public class OffHeapMemoryStorage implements KMemoryStorage {
    private static final Unsafe UNSAFE = getUnsafe();

    protected volatile long _start_address;

    private int _threshold;
    private final float _loadFactor;

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


    // TODO this methods are maybe a bottleneck if they are not inlined
    private int internal_getHash(long startAddress, int index) {
        return UNSAFE.getInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    private void internal_setHash(long startAddress, int index, int hash) {
        UNSAFE.putInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), hash);
    }

    private int internal_getNext(long startAddress, int index) {
        return UNSAFE.getInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    private void internal_setNext(long startAddress, int index, int next) {
        UNSAFE.putInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), next);
    }

    private void internal_setUniverseKey(long startAddress, int index, long universeKey) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY), universeKey);
    }

    private long internal_getUniverseKey(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_UNIVERSE_KEY));
    }

    private void internal_setTimeKey(long startAddress, int index, long timeKey) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY), timeKey);
    }

    private long internal_getTimeKey(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TIME_KEY));
    }

    private void internal_setObjKey(long startAddress, int index, long objKey) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY), objKey);
    }

    private long internal_getObjKey(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_OBJ_KEY));
    }

    private long internal_getValuePointer(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR));
    }

    private void internal_setValuePointer(long startAddress, int index, long valuePointer) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE_PTR), valuePointer);
    }

    private void internal_setType(long startAddress, int index, short type) {
        UNSAFE.putShort(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE), type);
    }

    private short internal_getType(long startAddress, int index) {
        return UNSAFE.getShort(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_TYPE));
    }

    private KOffHeapMemoryElement internal_getMemoryElement(long startAddress, int index) {
        KMemoryElement elem = internal_createElement(internal_getType(startAddress, index));

        if (!(elem instanceof KOffHeapMemoryElement)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapMemoryElement offheapElem = (KOffHeapMemoryElement) elem;
        offheapElem.setMemoryAddress(internal_getValuePointer(startAddress, index));

        return offheapElem;
    }

    public OffHeapMemoryStorage() {
        int initialCapacity = KConfig.CACHE_INIT_SIZE;
        this._loadFactor = KConfig.CACHE_LOAD_FACTOR;

        long address = UNSAFE.allocateMemory(BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN);
        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            internal_setNext(address, i, -1);
            internal_setHash(address, i, -1);
        }

        _start_address = address;
        this._threshold = (int) (initialCapacity * this._loadFactor);
    }

    private void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.copyMemory(_start_address, newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

        for (int i = 0; i < length; i++) {
            internal_setNext(newAddress, i, -1);
            internal_setHash(newAddress, i, -1);
        }

        //rehashEveryThing
        for (int i = 0; i < elementDataSize; i++) {
            if (internal_getValuePointer(newAddress, i) != 0) { //there is a real value
                int hash = (int) (internal_getUniverseKey(newAddress, i) ^ internal_getTimeKey(newAddress, i) ^ internal_getObjKey(newAddress, i));
                int index = (hash & 0x7FFFFFFF) % length;
                internal_setNext(newAddress, i, internal_getNext(newAddress, index));
                internal_setHash(newAddress, index, i);
            }
        }

        //setPrimitiveType value for all
        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, length);

        long oldAddress = _start_address;
        _start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        this._threshold = (int) (length * this._loadFactor);
    }

    public final int getIndex(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return -1;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = internal_getHash(_start_address, index);
        while (m != -1) {
            if (universe == internal_getUniverseKey(_start_address, m) && time == internal_getTimeKey(_start_address, m) && obj == internal_getObjKey(_start_address, m)) {
                return m;
            } else {
                m = internal_getNext(_start_address, m);
            }
        }
        return -1;
    }

   @Override
    public final KMemoryElement get(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return null;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = internal_getHash(_start_address, index);
        while (m != -1) {
            if (universe == internal_getUniverseKey(_start_address, m) && time == internal_getTimeKey(_start_address, m) && obj == internal_getObjKey(_start_address, m)) {
                return internal_getMemoryElement(_start_address, m); /* getValue */
            } else {
                m = internal_getNext(_start_address, m);
            }
        }
        return null;
    }


    @Override
    public final KMemoryElement create(long universe, long time, long obj, short type) {
        KOffHeapMemoryElement newElement = internal_createElement(type);
        newElement.setStorage(this, universe, time, obj);

        return internal_put(universe, time, obj, newElement, type);
    }

    public final void notifyRealloc(long newAddress, long universe, long time, long obj) {
        int index = getIndex(universe, time, obj);

        long currentAddress = internal_getValuePointer(_start_address, index);
        if (currentAddress != newAddress) {
            internal_setValuePointer(_start_address, index, newAddress);
        }
    }

    private KOffHeapMemoryElement internal_createElement(short type) {
        switch (type) {
            case KMemoryElementTypes.CHUNK:
                return new OffHeapMemoryChunk();

            case KMemoryElementTypes.LONG_TREE:
                return new OffHeapLongTree();

            case KMemoryElementTypes.LONG_LONG_TREE:
                return new OffHeapLongLongTree();

            case KMemoryElementTypes.LONG_LONG_MAP:
                return new OffHeapUniverseOrderMap();
        }
        return null;
    }


    @Override
    public final KMemoryChunk clone(KMemoryChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel) {
        return (KMemoryChunk) internal_put(newUniverse, newTime, newObj, previousElement.clone(metaModel), KMemoryElementTypes.CHUNK);
    }

    private synchronized KMemoryElement internal_put(long universe, long time, long p_obj, KMemoryElement payload, short type) {
        if (!(payload instanceof KOffHeapMemoryElement)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapMemoryElement memoryElement = (KOffHeapMemoryElement) payload;

        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int entry = -1;
        int index = -1;
        int hash = (int) (universe ^ time ^ p_obj);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(universe, time, p_obj, index);
        }
        if (entry == -1) {
            int oldElementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT, elementCount);

            if (elementCount > _threshold) {
                rehashCapacity(elementDataSize);
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }

            int droppedCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
            int newIndex = (elementCount - 1 + droppedCount);

            internal_setUniverseKey(_start_address, newIndex, universe);
            internal_setTimeKey(_start_address, newIndex, time);
            internal_setObjKey(_start_address, newIndex, p_obj);
            internal_setValuePointer(_start_address, newIndex, memoryElement.getMemoryAddress());
            internal_setType(_start_address, newIndex, type);

            internal_setNext(_start_address, newIndex, internal_getNext(_start_address, index));
            //now the object is reachable to other thread everything should be ready
            internal_setHash(_start_address, index, newIndex);
            return payload;

        } else {
            return internal_getMemoryElement(_start_address, entry);
        }
    }

    final int findNonNullKeyEntry(long universe, long time, long obj, int index) {
        int m = internal_getHash(_start_address, index);
        while (m >= 0) {
            if (universe == internal_getUniverseKey(_start_address, m)
                    && time == internal_getTimeKey(_start_address, m)
                    && obj == internal_getObjKey(_start_address, m)) {
                return m;
            }
            m = internal_getNext(_start_address, m);
        }
        return -1;
    }

//    @Override
//    public KContentKey[] dirtyKeys() {
//        int nbDirties = 0;
//
//        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);
//        for (int i = 0; i < elementDataSize; i++) {
//            if (internal_getValuePointer(_start_address, i) != 0) {
//                if (internal_getMemoryElement(_start_address, i).isDirty()) {
//                    nbDirties++;
//                }
//            }
//        }
//        KContentKey[] collectedDirties = new KContentKey[nbDirties];
//        nbDirties = 0;
//        for (int i = 0; i < elementDataSize; i++) {
//            if (internal_getValuePointer(_start_address, i) != 0) {
//                if (internal_getMemoryElement(_start_address, i).isDirty()) {
//                    collectedDirties[nbDirties] = new KContentKey(internal_getUniverseKey(_start_address, i),
//                            internal_getTimeKey(_start_address, i),
//                            internal_getTimeKey(_start_address, i));
//                    nbDirties++;
//                }
//            }
//        }
//        return collectedDirties;
//    }


    @Override
    public final int size() {
        return UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
    }

    @Override
    public void remove(long universe, long time, long obj, KMetaModel p_metaModel) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        int hash = (int) (universe ^ time ^ obj);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        if (elementDataSize == 0) {
            return;
        }
        int m = internal_getHash(_start_address, index);
        int last = -1;
        while (m >= 0) {
            if (universe == internal_getUniverseKey(_start_address, m) && time == internal_getTimeKey(_start_address, m) && obj == internal_getObjKey(_start_address, m)) {
                break;
            }
            last = m;
            m = internal_getNext(_start_address, m);
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (internal_getNext(_start_address, m) != -1) {
                internal_setHash(_start_address, index, m);
            } else {
                internal_setHash(_start_address, index, -1);
            }
        } else {
            internal_setNext(_start_address, last, internal_getNext(_start_address, m));
        }
        internal_setNext(_start_address, m, -1);//flag to dropped value
        internal_getMemoryElement(_start_address, m).free(p_metaModel);
        internal_setValuePointer(_start_address, m, 0);

        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT, elementCount - 1);
        int droppedCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, droppedCount + 1);

        if (droppedCount > this._threshold * this._loadFactor) {
            compact();
        }
    }

    private void compact() {
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);
        int droppedCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);

        if (droppedCount > 0) {
            int length = (elementCount == 0 ? 1 : elementCount << 1); //take the next size of element count
            int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);
            UNSAFE.copyMemory(_start_address, newAddress, BASE_SEGMENT_LEN);

            int currentIndex = 0;
            for (int i = 0; i < length; i++) {
                internal_setNext(newAddress, i, -1);
                internal_setHash(newAddress, i, -1);
            }

            for (int i = 0; i < elementDataSize; i++) {
                KOffHeapMemoryElement loopElement = internal_getMemoryElement(_start_address, i);
                if (loopElement != null) {
                    long l_uni = internal_getUniverseKey(_start_address, i);
                    long l_time = internal_getTimeKey(_start_address, i);
                    long l_obj = internal_getObjKey(_start_address, i);
                    short l_type = internal_getType(_start_address, i);

                    internal_setValuePointer(newAddress, currentIndex, loopElement.getMemoryAddress());
                    internal_setUniverseKey(newAddress, currentIndex, l_uni);
                    internal_setTimeKey(newAddress, currentIndex, l_time);
                    internal_setObjKey(newAddress, currentIndex, l_obj);
                    internal_setType(newAddress, currentIndex, l_type);

                    int hash = (int) (l_uni ^ l_time ^ l_obj);
                    int index = (hash & 0x7FFFFFFF) % length;
                    internal_setNext(newAddress, currentIndex, internal_getHash(newAddress, index));
                    internal_setHash(newAddress, index, currentIndex);
                    currentIndex++;
                }
            }

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, length);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, currentIndex);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

            long oldAddress = _start_address;
            _start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            this._threshold = (int) (length * this._loadFactor);
        }
    }

    @Override
    public final void clear(KMetaModel metaModel) {
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_COUNT);

        if (elementCount > 0) {
            int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            for (int i = 0; i < elementDataSize; i++) {
                if (internal_getValuePointer(_start_address, i) != 0) {
                    internal_getMemoryElement(_start_address, i).free(metaModel);
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
                internal_setNext(newAddress, i, -1);
                internal_setHash(newAddress, i, -1);
            }

            long oldAddress = _start_address;
            _start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            this._threshold = (int) (elementDataSize * _loadFactor);
        }
    }

    @Override
    public void delete(KMetaModel metaModel) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long oldAddress = _start_address;
        _start_address = -1; //this object should not be used anymore

        for (int i = 0; i < elementDataSize; i++) {
            if (internal_getValuePointer(oldAddress, i) != 0) {
                internal_getMemoryElement(oldAddress, i).free(metaModel);
            }
        }

        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        this._threshold = 0;
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

