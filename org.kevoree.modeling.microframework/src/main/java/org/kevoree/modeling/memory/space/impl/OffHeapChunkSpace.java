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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @ignore ts
 * OffHeap implementation of KChunkSpaceManager
 * - memory structure:  | threshold (4) | elementCount (4) | valuesIndex (4) | dirtyListPtr (8) | elementDataSize (4) | back (elem data size * 42) |
 * - back:              | universe_key (8)  | time_key (8) | obj_key (8) | next (4) | hash (4) | value_ptr (8) | type (2) |
 * - dirtyList:         | dirtyListLength (4) | dirtyListIndex (4) | dirtyList (dirtyListLength * (univ_len + time_len + obj_len) |
 */
public class OffHeapChunkSpace implements KChunkSpace {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    // chunk spaces always have unique start addresses, that is why they can be used for compare and swap operations
    // this isn't the case for chunks since several chunks can share the same start address
    private final AtomicLong _start_address;
    private float loadFactor;

    // constants for off-heap memory layout
    private static final int ATT_THRESHOLD_LEN = 4;
    private static final int ATT_ELEMENT_COUNT_LEN = 4;
    private static final int ATT_VALUES_INDEX_LEN = 4;
    private static final int ATT_DIRTY_LIST_PTR_LEN = 8;
    private static final int ATT_ELEMENT_DATA_SIZE_LEN = 4;

    private static final int ATT_DIRTY_LIST_LENGTH_LEN = 4;
    private static final int ATT_DIRTY_LIST_INDEX_LEN = 4;

    private static final int ATT_UNIVERSE_KEY_LEN = 8;
    private static final int ATT_TIME_KEY_LEN = 8;
    private static final int ATT_OBJ_KEY_LEN = 8;
    private static final int ATT_NEXT_LEN = 4;
    private static final int ATT_HASH_LEN = 4;
    private static final int ATT_VALUE_PTR_LEN = 8;
    private static final int ATT_TYPE_LEN = 2;

    private static final int OFFSET_STARTADDRESS_THRESHOLD = 0;
    private static final int OFFSET_STARTADDRESS_ELEMENT_COUNT = OFFSET_STARTADDRESS_THRESHOLD + ATT_THRESHOLD_LEN;
    private static final int OFFSET_STARTADDRESS_VALUES_INDEX = OFFSET_STARTADDRESS_ELEMENT_COUNT + ATT_ELEMENT_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_DIRTY_LIST_PTR = OFFSET_STARTADDRESS_VALUES_INDEX + ATT_VALUES_INDEX_LEN;
    private static final int OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE = OFFSET_STARTADDRESS_DIRTY_LIST_PTR + ATT_DIRTY_LIST_PTR_LEN;
    private static final int OFFSET_STARTADDRESS_BACK = OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE + ATT_ELEMENT_DATA_SIZE_LEN;

    private static final int OFFSET_DIRTYLIST_LENGTH = 0;
    private static final int OFFSET_DIRTYLIST_INDEX = OFFSET_DIRTYLIST_LENGTH + ATT_DIRTY_LIST_LENGTH_LEN;
    private static final int OFFSET_DIRTYLIST_ELEMS = OFFSET_DIRTYLIST_INDEX + ATT_DIRTY_LIST_INDEX_LEN;
    private static final int OFFSET_DIRTYLIST_UNIVERSE_KEY = OFFSET_DIRTYLIST_ELEMS + 0;
    private static final int OFFSET_DIRTYLIST_TIME_KEY = OFFSET_DIRTYLIST_UNIVERSE_KEY + ATT_UNIVERSE_KEY_LEN;
    private static final int OFFSET_DIRTYLIST_OBJ_KEY = OFFSET_DIRTYLIST_TIME_KEY + ATT_TIME_KEY_LEN;

    private static final int OFFSET_BACK_UNIVERSE_KEY = 0;
    private static final int OFFSET_BACK_TIME_KEY = OFFSET_BACK_UNIVERSE_KEY + ATT_UNIVERSE_KEY_LEN;
    private static final int OFFSET_BACK_OBJ_KEY = OFFSET_BACK_TIME_KEY + ATT_TIME_KEY_LEN;
    private static final int OFFSET_BACK_NEXT = OFFSET_BACK_OBJ_KEY + ATT_OBJ_KEY_LEN;
    private static final int OFFSET_BACK_HASH = OFFSET_BACK_NEXT + ATT_NEXT_LEN;
    private static final int OFFSET_BACK_VALUE_PTR = OFFSET_BACK_HASH + ATT_HASH_LEN;
    private static final int OFFSET_BACK_TYPE = OFFSET_BACK_VALUE_PTR + ATT_VALUE_PTR_LEN;

    private static final int BASE_SEGMENT_LEN =
            ATT_THRESHOLD_LEN + ATT_ELEMENT_COUNT_LEN + ATT_VALUES_INDEX_LEN + ATT_DIRTY_LIST_INDEX_LEN + ATT_ELEMENT_DATA_SIZE_LEN;
    private static final int BACK_ELEM_ENTRY_LEN = ATT_UNIVERSE_KEY_LEN + ATT_TIME_KEY_LEN + ATT_OBJ_KEY_LEN + ATT_NEXT_LEN + ATT_HASH_LEN + ATT_VALUE_PTR_LEN + ATT_TYPE_LEN;
    private static final int DIRTY_LIST_BASE_LEN = ATT_DIRTY_LIST_LENGTH_LEN + ATT_DIRTY_LIST_INDEX_LEN;
    private static final int DIRTY_LIST_ENTRY_LEN = ATT_UNIVERSE_KEY_LEN + ATT_TIME_KEY_LEN + ATT_OBJ_KEY_LEN;


    public OffHeapChunkSpace() {
        this._start_address = new AtomicLong(0);
        allocate();
    }

    private void allocate() {
        int initialCapacity = KConfig.CACHE_INIT_SIZE;
        this.loadFactor = KConfig.CACHE_LOAD_FACTOR;

        long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
        long address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(address, bytes, (byte) 0);

        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(address + OFFSET_STARTADDRESS_VALUES_INDEX, 0);

        UNSAFE.putInt(address + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            setNext(address, i, -1);
            setHash(address, i, -1);
        }

        this._start_address.set(address);

        // dirty list
        long dirtyListAddress = allocateDirties(KConfig.CACHE_INIT_SIZE);
        // link
        UNSAFE.putLong(this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR, dirtyListAddress);

        int threshold = (int) (initialCapacity * this.loadFactor);
        UNSAFE.putInt(this._start_address.get() + OFFSET_STARTADDRESS_THRESHOLD, threshold);
    }

    private long allocateDirties(int length) {
        long dirtyListAddress = UNSAFE.allocateMemory(DIRTY_LIST_BASE_LEN + length * DIRTY_LIST_ENTRY_LEN);
        UNSAFE.putInt(dirtyListAddress + OFFSET_DIRTYLIST_LENGTH, length);
        UNSAFE.putInt(dirtyListAddress + OFFSET_DIRTYLIST_INDEX, 0);

        return dirtyListAddress;
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

    private long dirtyListUniverse(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_UNIVERSE_KEY));
    }

    private long dirtyListTime(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_TIME_KEY));
    }

    private long dirtyListObj(long p_baseAddress, int p_index) {
        return UNSAFE.getLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_OBJ_KEY));
    }

    private void setDirtyListElem(long p_baseAddress, int p_index, long p_universe, long p_time, long p_obj) {
        UNSAFE.putLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_UNIVERSE_KEY), p_universe);
        UNSAFE.putLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_TIME_KEY), p_time);
        UNSAFE.putLong(p_baseAddress + OFFSET_DIRTYLIST_ELEMS + (p_index * DIRTY_LIST_ENTRY_LEN + OFFSET_DIRTYLIST_OBJ_KEY), p_obj);
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
        int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.copyMemory(this._start_address.get(), newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

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

        long oldAddress = this._start_address.get();
        this._start_address.set(newAddress);
        UNSAFE.freeMemory(oldAddress);

        int threshold = (int) (length * this.loadFactor);
        UNSAFE.putInt(this._start_address.get() + OFFSET_STARTADDRESS_THRESHOLD, threshold);
    }

    public final int getIndex(long p_universe, long p_time, long p_obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return -1;
        }
        int index = (((int) (p_universe ^ p_time ^ p_obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address.get(), index);
        while (m != -1) {
            if (p_universe == universe(this._start_address.get(), m) && p_time == time(this._start_address.get(), m) && p_obj == obj(this._start_address.get(), m)) {
                return m;
            } else {
                m = next(this._start_address.get(), m);
            }
        }
        return -1;
    }

    @Override
    public final KChunk get(long p_universe, long p_time, long p_obj) {
        int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        if (elementDataSize == 0) {
            return null;
        }
        int index = (((int) (p_universe ^ p_time ^ p_obj)) & 0x7FFFFFFF) % elementDataSize;
        int m = hash(this._start_address.get(), index);
        while (m != -1) {
            if (p_universe == universe(this._start_address.get(), m) && p_time == time(this._start_address.get(), m) && p_obj == obj(this._start_address.get(), m)) {
                return internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address.get(), m); /* getValue */
            } else {
                m = next(this._start_address.get(), m);
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
            long currentAddress = valuePointer(this._start_address.get(), index);
            if (currentAddress != newAddress) {
                setValuePointer(this._start_address.get(), index, newAddress);
            }
        }
    }

    private KOffHeapChunk internal_createElement(long p_universe, long p_time, long p_obj, short p_type) {
        switch (p_type) {
            case KChunkTypes.OBJECT_CHUNK:
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
                p_previousElement.clone(p_newUniverse, p_newTime, p_newObj, p_metaModel), KChunkTypes.OBJECT_CHUNK);
    }

    private synchronized KChunk internal_put(long p_universe, long p_time, long p_obj, KChunk p_payload, short p_type) {
        if (!(p_payload instanceof KOffHeapChunk)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }
        KOffHeapChunk memoryElement = (KOffHeapChunk) p_payload;

        long currentState;
        long nextState;
        KChunk result;
        int nbTry = 0;
        do {
            int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            currentState = this._start_address.get();
            int entry = -1;
            int index = -1;
            int hash = (int) (p_universe ^ p_time ^ p_obj);
            if (elementDataSize != 0) {
                index = (hash & 0x7FFFFFFF) % elementDataSize;
                entry = findNonNullKeyEntry(p_universe, p_time, p_obj, index);
            }
            if (entry == -1) {

                int nextValueIndex = UNSAFE.getAndAddInt(null, this._start_address.get() + OFFSET_STARTADDRESS_VALUES_INDEX, +1);
                int threshold = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_THRESHOLD);
                if (nextValueIndex > threshold) {
                    return complex_insert(p_universe, p_time, p_obj, p_type, memoryElement, hash, nextValueIndex);
                } else {
                    nextState = currentState;
                }

                setUniverse(nextState, nextValueIndex, p_universe);
                setTime(nextState, nextValueIndex, p_time);
                setObj(nextState, nextValueIndex, p_obj);
                setValuePointer(nextState, nextValueIndex, memoryElement.memoryAddress());
                setType(nextState, nextValueIndex, p_type);

                long hashPtr = nextState + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH);
                setNext(nextState, nextValueIndex, UNSAFE.getAndSetInt(null, hashPtr, nextValueIndex));
                UNSAFE.getAndAddInt(null, nextState + OFFSET_STARTADDRESS_ELEMENT_COUNT, +1);

                result = p_payload;

            } else {
                nextState = currentState;
                result = internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address.get(), entry);
            }
            nbTry++;
            if (nbTry == KConfig.CAS_MAX_TRY) {
                throw new RuntimeException("CompareAndSwap error, failed to converge");
            }
        } while (!this._start_address.compareAndSet(currentState, nextState));

        return result;
    }

    private synchronized KChunk complex_insert(long p_universe, long p_time, long p_obj, short p_type, KOffHeapChunk p_payload, int p_prehash, int p_nextValueIndex) {
        long currentState;
        long nextState;
        do {
            currentState = this._start_address.get();
            int threshold = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_THRESHOLD);
            int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);
            int length = (elementDataSize == 0 ? 1 : elementDataSize << 1);

            if (p_nextValueIndex > threshold) {
                rehashCapacity(length);
                nextState = this._start_address.get(); // start pointer pointer changed after rehash
            } else {
                nextState = currentState;
            }

            elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE); // updated after rehash
            int index = (p_prehash & 0x7FFFFFFF) % elementDataSize;
            setUniverse(nextState, p_nextValueIndex, p_universe);
            setTime(nextState, p_nextValueIndex, p_time);
            setObj(nextState, p_nextValueIndex, p_obj);
            setType(nextState, p_nextValueIndex, p_type);
            setValuePointer(p_nextValueIndex, p_nextValueIndex, p_payload.memoryAddress());

            long hashPtr = nextState + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH);
            setNext(nextState, p_nextValueIndex, UNSAFE.getAndSetInt(null, hashPtr, p_nextValueIndex));

            UNSAFE.getAndAddInt(null, this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_COUNT, 1);

        } while (!this._start_address.compareAndSet(currentState, nextState));
        return p_payload;
    }

    final int findNonNullKeyEntry(long p_universe, long p_time, long p_obj, int p_index) {
        int m = hash(this._start_address.get(), p_index);
        while (m >= 0) {
            if (p_universe == universe(this._start_address.get(), m)
                    && p_time == time(this._start_address.get(), m)
                    && p_obj == obj(this._start_address.get(), m)) {
                return m;
            }
            m = next(this._start_address.get(), m);
        }
        return -1;
    }

    @Override
    public final int size() {
        return UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_COUNT);
    }

    @Override
    public KChunkIterator detachDirties() {
        long newDirtiesAddr = allocateDirties(KConfig.CACHE_INIT_SIZE);
        long detachedDirtiesAddr = UNSAFE.getAndSetLong(null, this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR, newDirtiesAddr);

        int maxIndex = UNSAFE.getInt(detachedDirtiesAddr + OFFSET_DIRTYLIST_INDEX);
        long[] shrinked = new long[maxIndex * 3];
        for (int i = 0; i < maxIndex; i++) {
            shrinked[3 * i] = dirtyListUniverse(detachedDirtiesAddr, i);
            shrinked[3 * i + 1] = dirtyListTime(detachedDirtiesAddr, i);
            shrinked[3 * i + 2] = dirtyListObj(detachedDirtiesAddr, i);
        }

        UNSAFE.freeMemory(detachedDirtiesAddr);

        return new ChunkIterator(shrinked, this);
    }

    @Override
    public void declareDirty(KChunk p_dirtyChunk) {
        long currentDirtiesAddr;
        int nbTry = 0;
        do {
            currentDirtiesAddr = UNSAFE.getLong(this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR);
            internal_declareDirty(currentDirtiesAddr, p_dirtyChunk.universe(), p_dirtyChunk.time(), p_dirtyChunk.obj());

            nbTry++;
            if (nbTry == KConfig.CAS_MAX_TRY) {
                throw new RuntimeException("CompareAndSwap error, failed to converge");
            }
        } while (!UNSAFE.compareAndSwapLong(null,
                this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR, currentDirtiesAddr, currentDirtiesAddr));
    }

    private void internal_declareDirty(long p_dirtiesAddr, long p_universe, long p_time, long p_obj) {
        int nextIndex = UNSAFE.getAndAddInt(null, p_dirtiesAddr + OFFSET_DIRTYLIST_INDEX, 1);
        int length = UNSAFE.getInt(p_dirtiesAddr + OFFSET_DIRTYLIST_LENGTH);
        //simple case
        if (nextIndex < length) {
            setDirtyListElem(p_dirtiesAddr, nextIndex, p_universe, p_time, p_obj);

        } else {
            synchronized (this) {
                int newLength = nextIndex * 2;
                long newDirtiesAddr = allocateDirties(newLength);
                long bytes = DIRTY_LIST_BASE_LEN + length * DIRTY_LIST_ENTRY_LEN;
                UNSAFE.copyMemory(p_dirtiesAddr, newDirtiesAddr, bytes);

                long oldDirtiesAddr = UNSAFE.getLong(this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR);
                UNSAFE.putLong(this._start_address.get() + OFFSET_STARTADDRESS_DIRTY_LIST_PTR, newDirtiesAddr);
                UNSAFE.freeMemory(oldDirtiesAddr);

                setDirtyListElem(newDirtiesAddr, nextIndex, p_universe, p_time, p_obj);
            }
        }
    }

    @Override
    public void remove(long p_universe, long p_time, long p_obj, KMetaModel p_metaModel) {
        long previousState;
        int nbTry = 0;

        do {
            previousState = this._start_address.get();

            int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);
            int hash = (int) (p_universe ^ p_time ^ p_obj);
            int index = (hash & 0x7FFFFFFF) % elementDataSize;
            if (elementDataSize == 0) {
                return;
            }
            int m = hash(this._start_address.get(), index);
            int last = -1;
            while (m >= 0) {
                if (p_universe == universe(this._start_address.get(), m) && p_time == time(this._start_address.get(), m) && p_obj == obj(_start_address.get(), m)) {
                    break;
                }
                last = m;
                m = next(this._start_address.get(), m);
            }
            if (m == -1) {
                return;
            }
            if (last == -1) {
                if (next(this._start_address.get(), m) != -1) {
                    setHash(this._start_address.get(), index, m);
                } else {
                    setHash(this._start_address.get(), index, -1);
                }
            } else {
                setNext(this._start_address.get(), last, next(this._start_address.get(), m));
            }
            setNext(this._start_address.get(), m, -1);//flag to dropped value
            internal_getMemoryElement(p_universe, p_time, p_obj, this._start_address.get(), m).free(p_metaModel);
            setValuePointer(this._start_address.get(), m, 0);

            UNSAFE.getAndAddInt(null, this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_COUNT, -1);

            nbTry++;
            if (nbTry == KConfig.CAS_MAX_TRY) {
                throw new RuntimeException("CompareAndSwap error, failed to converge");
            }
        } while (!this._start_address.compareAndSet(previousState, previousState));
    }

    @Override
    public final void clear(KMetaModel p_metaModel) {
        int elementCount = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_COUNT);

        if (elementCount > 0) {
            int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

            for (int i = 0; i < elementDataSize; i++) {
                if (valuePointer(this._start_address.get(), i) != 0) {
                    long universe = universe(this._start_address.get(), i);
                    long time = time(this._start_address.get(), i);
                    long obj = obj(this._start_address.get(), i);
                    internal_getMemoryElement(universe, time, obj, this._start_address.get(), i).free(p_metaModel);
                }
            }
            int initialCapacity = KConfig.CACHE_INIT_SIZE;
            long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);
            UNSAFE.setMemory(newAddress, bytes, (byte) 0);

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_VALUES_INDEX, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE, initialCapacity);
            for (int i = 0; i < initialCapacity; i++) {
                setNext(newAddress, i, -1);
                setHash(newAddress, i, -1);
            }

            long oldAddress = this._start_address.get();
            this._start_address.set(newAddress);
            UNSAFE.freeMemory(oldAddress);

            int threshold = (int) (elementDataSize * loadFactor);
            UNSAFE.putInt(this._start_address.get() + OFFSET_STARTADDRESS_THRESHOLD, threshold);

        }
    }

    @Override
    public void delete(KMetaModel p_metaModel) {
        // TODO this method is not thread-safe
        int elementDataSize = UNSAFE.getInt(this._start_address.get() + OFFSET_STARTADDRESS_ELEMENT_DATA_SIZE);

        long oldAddress = this._start_address.get();
        this._start_address.set(-1); //this object should not be used anymore

        for (int i = 0; i < elementDataSize; i++) {
            if (valuePointer(oldAddress, i) != 0) {
                long universe = universe(oldAddress, i);
                long time = time(oldAddress, i);
                long obj = obj(oldAddress, i);
                internal_getMemoryElement(universe, time, obj, oldAddress, i).free(p_metaModel);
            }
        }

        long dirtyListPtr = UNSAFE.getLong(oldAddress + OFFSET_STARTADDRESS_DIRTY_LIST_PTR);
        UNSAFE.freeMemory(dirtyListPtr);
        UNSAFE.putLong(oldAddress + OFFSET_STARTADDRESS_DIRTY_LIST_PTR, 0);

        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_ELEMENT_COUNT, 0);
        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_VALUES_INDEX, 0);

        UNSAFE.putInt(oldAddress + OFFSET_STARTADDRESS_THRESHOLD, 0);
    }

}

