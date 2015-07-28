
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @ignore ts
 * <p/>
 * - memory structures:
 * - valuesSegment : | elementCount (4) | elementDataSize (4) | threshold (4) | hashSegmentPtr (8) | elemKey (8) | elemValue (8) | nextElemIndex(8) | ... | elemKey (8) | elemValue (8) | nextElemIndex(8)|
 * - hashSegment : | elemIndex (8) | ... | elemIndex (8) |
 */
public class OffHeapLongLongMap implements KLongLongMap {

    /*
    protected long[] elementKV;

    protected int[] elementNext;

    protected int[] elementHash;

    protected int elementCount;

    protected int elementDataSize;

    protected int threshold;
    /* above has to go to offheap */

    private final int initalCapacity;

    private final float loadFactor;

    protected boolean _isDirty = false;

    protected static final Unsafe UNSAFE = getUnsafe();

    private long _start_address;

    private long _start_address_hash;

    public final int size() {
        return UNSAFE.getInt(this._start_address);
    }

    private static final int _offset_values = 4 + 4 + 4 + 8;

    public OffHeapLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initalCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;

        /* allocate ValueSegment */
        long valueSegmentSize = _offset_values + (8 + 8 + 8) * initalCapacity;
        _start_address = UNSAFE.allocateMemory(valueSegmentSize);

        UNSAFE.putInt(_start_address, 0); /*setElementCount */
        UNSAFE.putInt(_start_address + 4, initalCapacity); /*setElementDataSize */
        UNSAFE.putInt(_start_address + 4 + 4, (int) (initalCapacity * loadFactor)); /*setThreshold */
        long hashSegmentSize = 8 * initalCapacity;
        _start_address_hash = UNSAFE.allocateMemory(hashSegmentSize);
        UNSAFE.putLong(_start_address + 4 + 4 + 4, _start_address_hash); /* setHashSegmentBegin */
        for (int i = 0; i < initalCapacity; i++) {
            UNSAFE.putLong(_start_address_hash + (i * 8), -1); /* setHash(i) = -1 */
            UNSAFE.putLong(_start_address + _offset_values + (i * (8 + 8 + 8)) + 8 + 8, -1); /* setNext(i) = -1 */
        }
    }

    public void clear() {
        if (UNSAFE.getInt(_start_address) > 0) { /* getElementCount */
            UNSAFE.freeMemory(_start_address_hash);
            UNSAFE.freeMemory(_start_address);
            /* allocate ValueSegment */
            long valueSegmentSize = _offset_values + (8 + 8 + 8) * initalCapacity;
            _start_address = UNSAFE.allocateMemory(valueSegmentSize);

            UNSAFE.putInt(_start_address, 0); /*setElementCount */
            UNSAFE.putInt(_start_address + 4, initalCapacity); /*setElementDataSize */
            UNSAFE.putInt(_start_address + 4 + 4, (int) (initalCapacity * loadFactor)); /*setThreshold */
            long hashSegmentSize = 8 * initalCapacity;
            _start_address_hash = UNSAFE.allocateMemory(hashSegmentSize);
            UNSAFE.putLong(_start_address + 4 + 4 + 4, _start_address_hash); /* setHashSegmentBegin */
            for (int i = 0; i < initalCapacity; i++) {
                UNSAFE.putLong(_start_address_hash + (i * 8), -1); /* setHash(i) = -1 */
                UNSAFE.putLong(_start_address + _offset_values + (i * (8 + 8 + 8)) + 8 + 8, -1); /* setNext(i) = -1 */
            }
        }
    }

    void rehashCapacity(int capacity) {
        /*
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementKV = new long[length * 2];
        System.arraycopy(this.elementKV, 0, newElementKV, 0, this.elementCount * 2);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < this.elementNext.length; i++) {
            if (this.elementNext[i] != -1) { //there is a real value
                int index = ((int) this.elementKV[i * 2] & 0x7FFFFFFF) % length;
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
        this.elementKV = newElementKV;
        this.elementHash = newElementHash;
        this.elementNext = newElementNext;
        this.elementDataSize = length;
        this.threshold = (int) (elementDataSize * loadFactor);
        */
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        /*
        for (int i = 0; i < this.elementNext.length; i++) {
            if (this.elementNext[i] != -1) { //there is a real value
                callback.on(this.elementKV[i * 2], this.elementKV[i * 2 + 1]);
            }
        }
        */
    }

    @Override
    public boolean contains(long key) {
        int elementDataSize = UNSAFE.getInt(_start_address + 4);
        if (elementDataSize == 0) {  /*getElementDataSize*/
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        int m = findNonNullKeyEntry(key, index);
        return m != -1;
    }

    @Override
    public long get(long key) {
        /*
        int elementDataSize = UNSAFE.getInt(_start_address + 4);
        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        int m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        m = findNonNullKeyEntry(key, index);
        if (m != -1) {
            return this.elementKV[(m * 2) + 1];
        }
        return KConfig.NULL_LONG;
    */
        return -1;
    }

    final int findNonNullKeyEntry(long key, int index) {
        /*
        int m = this.elementHash[index];
        while (m >= 0) {
            if (key == this.elementKV[m * 2]) {
                return m;
            }
            m = this.elementNext[m];
        }
        return -1;
    */
        return -1;
    }

    @Override
    public synchronized void put(long key, long value) {
        /*
        this._isDirty = true;
        int entry = -1;
        int index = -1;
        int hash = (int) (key);
        int elementDataSize = UNSAFE.getInt(_start_address + 4);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(elementDataSize);
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }
            int newIndex = (this.elementCount - 1);
            this.elementKV[newIndex * 2] = key;
            this.elementKV[newIndex * 2 + 1] = value;
            int currentHashedIndex = this.elementHash[index];
            if (currentHashedIndex != -1) {
                this.elementNext[newIndex] = currentHashedIndex;
            } else {
                this.elementNext[newIndex] = -2;
            }
            this.elementHash[hash] = newIndex;
        } else {
            this.elementKV[entry + 1] = value;
        }
    */
    }

    public void remove(long key) {
        /*
        if (elementDataSize == 0) {
            return;
        }
        int index;
        int entry;
        int last = -1;
        int hash = (int) (key);
        index = (hash & 0x7FFFFFFF) % elementDataSize;
        entry = this.elementHash[index];
        while (entry != -1 && !(key == entry.key)) {
            last = entry;
            entry = this.elementNext[entry];
        }
        if (entry == -1) {
            return;
        }
        if (last == -1) {


            elementData[index] = entry.next;
        } else {
            this.elementNext[]
            this.elementHash[]

            last.next = entry.next;
        }
        elementCount--;
        */
    }

    @SuppressWarnings("restriction")
    static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }
    
}



