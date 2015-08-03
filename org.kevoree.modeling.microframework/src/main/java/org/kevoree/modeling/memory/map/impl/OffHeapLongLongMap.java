
package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.storage.impl.OffHeapMemoryStorage;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of KLongLongMap
 * - memory structure:  | elem count (4) | dropped count (4) | dirty (1) | elem data size (4) | back (elem data size * 28) |
 * - back:              | key (8)   | value (8) | next (4) | hash (4) |
 */
public class OffHeapLongLongMap implements KLongLongMap, KOffHeapMemoryElement {
    protected static final Unsafe UNSAFE = getUnsafe();


    private volatile int _counter = 0;
    private int _metaClassIndex = -1;

    private OffHeapMemoryStorage storage;
    private long universe, time, obj;

    protected int threshold;

    private int initialCapacity;

    private float loadFactor;

    protected volatile long _start_address;

    protected static final int ATT_ELEM_COUNT_LEN = 4;
    protected static final int ATT_DROPPED_COUNT_LEN = 4;
    protected static final int ATT_DIRTY_LEN = 1;
    protected static final int ATT_ELEM_DATA_SIZE_LEN = 4;

    protected static final int ATT_KEY_LEN = 8;
    protected static final int ATT_VALUE_LEN = 8;
    protected static final int ATT_NEXT_LEN = 8;
    protected static final int ATT_HASH_LEN = 4;

    protected static final int BASE_SEGMENT_LEN = ATT_ELEM_COUNT_LEN + ATT_DROPPED_COUNT_LEN + ATT_DIRTY_LEN + ATT_ELEM_DATA_SIZE_LEN;
    protected static final int BACK_ELEM_ENTRY_LEN = ATT_KEY_LEN + ATT_VALUE_LEN + ATT_NEXT_LEN + ATT_HASH_LEN;

    protected static final int OFFSET_STARTADDRESS_ELEM_COUNT = 0;
    protected static final int OFFSET_STARTADDRESS_DROPPED_COUNT = OFFSET_STARTADDRESS_ELEM_COUNT + 4;
    protected static final int OFFSET_STARTADDRESS_DIRTY = OFFSET_STARTADDRESS_DROPPED_COUNT + 4;
    protected static final int OFFSET_STARTADDRESS_ELEM_DATA_SIZE = OFFSET_STARTADDRESS_DIRTY + 1;
    protected static final int OFFSET_STARTADDRESS_BACK = OFFSET_STARTADDRESS_ELEM_DATA_SIZE + 4;

    protected static final int OFFSET_BACK_KEY = 0;
    protected static final int OFFSET_BACK_VALUE = OFFSET_BACK_KEY + 8;
    protected static final int OFFSET_BACK_NEXT = OFFSET_BACK_VALUE + 8;
    protected static final int OFFSET_BACK_HASH = OFFSET_BACK_NEXT + 4;

    // TODO this methods are maybe a bottleneck if they are not inlined
    protected int internal_getHash(long startAddress, int index) {
        return UNSAFE.getInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH));
    }

    protected void internal_setHash(long startAddress, int index, int hash) {
        UNSAFE.putInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), hash);
    }

    protected long internal_getKey(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY));
    }

    protected void internal_setKey(long startAddress, int index, long key) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_KEY), key);
    }

    protected long internal_getValue(long startAddress, int index) {
        return UNSAFE.getLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE));
    }

    protected void internal_setValue(long startAddress, int index, long value) {
        UNSAFE.putLong(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_VALUE), value);
    }

    protected int internal_getNext(long startAddress, int index) {
        return UNSAFE.getInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT));
    }

    protected void internal_setNext(long startAddress, int index, int next) {
        UNSAFE.putInt(startAddress + OFFSET_STARTADDRESS_BACK + (index * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), next);
    }


    public OffHeapLongLongMap(int p_initalCapacity, float p_loadFactor) {
        internal_allocate(p_initalCapacity, p_loadFactor);
    }

    protected void internal_allocate(int p_initalCapacity, float p_loadFactor) {
        this.initialCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;

        int elementDataSize = p_initalCapacity;
        long bytes = BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN;
        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);

        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, elementDataSize);

        for (int i = 0; i < initialCapacity; i++) {
            // next
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_NEXT), -1);
            // hash
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_BACK + (i * BACK_ELEM_ENTRY_LEN + OFFSET_BACK_HASH), -1);
        }

        this.threshold = (int) (elementDataSize * loadFactor);

        if (this.storage != null) {
            storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
        }

    }

    public void clear() {
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);

        if (elementCount > 0) {

            long bytes = BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN;
            long newAddress = UNSAFE.allocateMemory(bytes);

            UNSAFE.copyMemory(_start_address, newAddress, BASE_SEGMENT_LEN + initialCapacity * BACK_ELEM_ENTRY_LEN);

            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
            UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, initialCapacity);

            for (int i = 0; i < initialCapacity; i++) {
                internal_setNext(newAddress, i, -1);
                internal_setHash(newAddress, i, -1);
            }

            long oldAddress = _start_address;
            _start_address = newAddress;
            UNSAFE.freeMemory(oldAddress);

            long elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
            this.threshold = (int) (elementDataSize * loadFactor);

            if (this.storage != null) {
                storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
            }
        }
    }

    protected void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);

        long bytes = BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN;
        long newAddress = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(newAddress, bytes, (byte) 0);

        long elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.copyMemory(_start_address, newAddress, BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN);

        for (int i = 0; i < length; i++) {
            internal_setNext(newAddress, i, -1);
            internal_setHash(newAddress, i, -1);
        }
        //rehashEveryThing
        for (int i = 0; i < elementDataSize; i++) {
            if (internal_getNext(_start_address, i) != -1) { //there is a real value
                int index = ((int) internal_getKey(_start_address, i) & 0x7FFFFFFF) % length;
                int currentHashedIndex = internal_getHash(newAddress, index);
                if (currentHashedIndex != -1) {
                    internal_setNext(newAddress, i, currentHashedIndex);
                } else {
                    internal_setNext(newAddress, i, -2);
                }
                internal_setHash(newAddress, index, i);
            }
        }

        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        long oldAddress = _start_address;
        _start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        this.threshold = (int) (length * loadFactor);

        if (this.storage != null) {
            storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
        }

    }

    @Override
    public final void each(KLongLongMapCallBack callback) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        for (int i = 0; i < elementDataSize; i++) {
            if (internal_getNext(_start_address, i) != -1) { //there is a real value
                callback.on(internal_getKey(_start_address, i), internal_getValue(_start_address, i));
            }
        }
    }

    @Override
    public int metaClassIndex() {
        return 0;
    }

    @Override
    public final boolean contains(long key) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;

        int m = internal_getHash(_start_address, index);
        while (m >= 0) {
            long k = internal_getKey(_start_address, m);
            if (key == k) {
                return m != -1;
            }
            m = internal_getNext(_start_address, m);
        }
        return m != -1;
    }

    @Override
    public final long get(long key) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % elementDataSize;

        int m = internal_getHash(_start_address, index);
        while (m >= 0) {
            long k = internal_getKey(_start_address, m);
            if (key == k) {
                long v = internal_getValue(_start_address, m);
                return v;
            } else {
                m = internal_getNext(_start_address, m);
            }
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public final synchronized void put(long key, long value) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 1);
        int entry = -1;
        int index = -1;
        int hash = (int) (key);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }

        if (entry == -1) {
            // increase elem count
            int oldElementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

            int droppedCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);

            if (elementCount > threshold) {
                rehashCapacity(elementDataSize);

                int newElementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
                index = (hash & 0x7FFFFFFF) % newElementDataSize;
            }
            int newIndex = (elementCount + droppedCount - 1);
            internal_setKey(_start_address, newIndex, key);
            internal_setValue(_start_address, newIndex, value);
            int currentHashedIndex = internal_getHash(_start_address, index);
            if (currentHashedIndex != -1) {
                internal_setNext(_start_address, newIndex, currentHashedIndex);
            } else {
                internal_setNext(_start_address, newIndex, -2);//special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            internal_setHash(_start_address, index, newIndex);
        } else {
            internal_setValue(_start_address, entry, value);
        }
    }

    final int findNonNullKeyEntry(long key, int index) {
        int m = internal_getHash(_start_address, index);
        while (m >= 0) {
            if (key == internal_getKey(_start_address, m)) {
                return m;
            }
            m = internal_getNext(_start_address, m);
        }
        return -1;
    }

    //TODO check intersection of remove and put
    @Override
    public synchronized final void remove(long key) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        if (elementDataSize == 0) {
            return;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % elementDataSize;
        int m = internal_getHash(_start_address, index);
        int last = -1;
        while (m >= 0) {
            if (key == internal_getKey(_start_address, m)) {
                break;
            }
            last = m;
            m = internal_getNext(_start_address, m);
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (internal_getNext(_start_address, m) > 0) {
                internal_setHash(_start_address, index, m);
            } else {
                internal_setHash(_start_address, index, -1);
            }
        } else {
            internal_setNext(_start_address, last, internal_getNext(_start_address, m));
        }
        internal_setNext(_start_address, m, -1); //flag to dropped value
        // decrease elem count
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        elementCount--;
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);

        // increase dropped count
        int droppedCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT);
        droppedCount++;
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, droppedCount);
    }

    public final int size() {
        return UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
    }

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
        return UNSAFE.getByte(_start_address + OFFSET_STARTADDRESS_DIRTY) != 0;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 0);
    }

    @Override
    public void setDirty() {
        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 1);
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

        long newAddress = UNSAFE.allocateMemory(BASE_SEGMENT_LEN + length * BACK_ELEM_ENTRY_LEN);
        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
        for (int i = 0; i < length; i++) {
            internal_setNext(newAddress, i, -1);
            internal_setHash(newAddress, i, -1);
        }

        //setPrimitiveType value for all
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
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % UNSAFE.getInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
            //insert K/V
            int newIndex = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            internal_setKey(newAddress, newIndex, loopKey);
            internal_setValue(newAddress, newIndex, loopVal);

            int currentHashedIndex = internal_getHash(newAddress, index);
            if (currentHashedIndex != -1) {
                internal_setNext(newAddress, newIndex, currentHashedIndex);
            } else {
                internal_setNext(newAddress, newIndex, -2);
            }
            internal_setHash(newAddress, index, newIndex);

            int oldElementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);
        }

        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, nbElement);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);

        int newElemDataSize = UNSAFE.getInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, newElemDataSize);
        _start_address = UNSAFE.reallocateMemory(_start_address, BASE_SEGMENT_LEN + newElemDataSize * BACK_ELEM_ENTRY_LEN);
        UNSAFE.copyMemory(newAddress + OFFSET_STARTADDRESS_BACK, _start_address + OFFSET_STARTADDRESS_BACK, newElemDataSize * BACK_ELEM_ENTRY_LEN);
        this.threshold = (int) (length * loadFactor);

        UNSAFE.freeMemory(newAddress);

    }


//    /* warning: this method is not thread safe */
//    @Override
//    public void init(String payload, KMetaModel metaModel, int metaClassIndex) {
//        _metaClassIndex = metaClassIndex;
//
//        if (payload == null || payload.length() == 0) {
//            return;
//        }
//        int initPos = 0;
//        int cursor = 0;
//        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '/') {
//            cursor++;
//        }
//        if (cursor >= payload.length()) {
//            return;
//        }
//        if (payload.charAt(cursor) == ',') {//className to parse
//            _metaClassIndex = metaModel.metaClassByName(payload.substring(initPos, cursor)).index();
//            cursor++;
//            initPos = cursor;
//        }
//        while (cursor < payload.length() && payload.charAt(cursor) != '/') {
//            cursor++;
//        }
//        int nbElement = Base64.decodeToIntWithBounds(payload, initPos, cursor);
//
//        int length = (nbElement == 0 ? 1 : nbElement << 1);
//
//        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
//        long bytes = BASE_SEGMENT_LEN + elementDataSize * BACK_ELEM_ENTRY_LEN;
//        long newAddress = UNSAFE.allocateMemory(bytes);
//        UNSAFE.setMemory(newAddress, bytes, (byte) 0);
//        UNSAFE.copyMemory(_start_address, newAddress, BASE_SEGMENT_LEN + nbElement * BACK_ELEM_ENTRY_LEN);
//
//        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
//
//        //setPrimitiveType value for all
//        while (cursor < payload.length()) {
//            cursor++;
//            int beginChunk = cursor;
//            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
//                cursor++;
//            }
//            int middleChunk = cursor;
//            while (cursor < payload.length() && payload.charAt(cursor) != ',') {
//                cursor++;
//            }
//            long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
//            long loopVal = Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
//            int index = (((int) (loopKey)) & 0x7FFFFFFF) % UNSAFE.getInt(newAddress + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
//            //insert K/V
//            int newIndex = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
//
//            internal_setKey(newAddress, newIndex, loopKey);
//            internal_setValue(newAddress, newIndex, loopVal);
//            int currentHashedIndex = internal_getHash(newAddress, index);
//            if (currentHashedIndex != -1) {
//                internal_setNext(newAddress, newIndex, currentHashedIndex);
//            } else {
//                internal_setNext(newAddress, newIndex, -2);//special char to tag used values
//            }
//            internal_setHash(newAddress, index, newIndex);
//
//            // increase element count
//            int oldElementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
//            int elementCount = oldElementCount + 1;
//            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);
//        }
//
//        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_ELEM_COUNT, nbElement);
//        UNSAFE.putInt(newAddress + OFFSET_STARTADDRESS_DROPPED_COUNT, 0);
//        long oldAddress = _start_address;
//        _start_address = newAddress;
//        this.threshold = (int) (length * loadFactor);
//
//        UNSAFE.freeMemory(oldAddress);
//    }

    @Override
    public String serialize(KMetaModel metaModel) {
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
        final StringBuilder buffer = new StringBuilder(elementCount * 8);//roughly approximate init size
        if (_metaClassIndex != -1) {
            buffer.append(metaModel.metaClass(_metaClassIndex).metaName());
            buffer.append(',');
        }
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        for (int i = 0; i < elementDataSize; i++) {

            if (internal_getNext(_start_address, i) != -1) { //there is a real value
                long loopKey = internal_getKey(_start_address, i);
                long loopValue = internal_getValue(_start_address, i);
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

    @Override
    public long getMemoryAddress() {
        return this._start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        this._start_address = address;
        if (this.storage != null) {
            storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
        }
    }

    @Override
    public void setStorage(OffHeapMemoryStorage storage, long universe, long time, long obj) {
        this.storage = storage;
        this.universe = universe;
        this.time = time;
        this.obj = obj;
    }

    @SuppressWarnings("restriction")
    protected final static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }
}



