
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * <p/>
 * - memory structure:
 * - root      | elem count (4) | elem data size (4) | dirty (1) | ... | key (8) | value (8) | next (8) | entry ptr (8) |....
 * - entry     | key (8) | value (8) | next (8) | entry ptr (8) |
 */
public class OffHeapLongLongMap implements KLongLongMap {
    protected static final Unsafe UNSAFE = getUnsafe();

    private long _start_address;
    private int _allocated_segments = 0;

    protected int _threshold;
    private final float _loadFactor;
    private final int _initalCapacity;

    private static final int POS_KEY = 0;
    private static final int POS_VALUE = 1;
    private static final int POS_NEXT = 2;
    private static final int POS_ENTRY_PTR = 3;

    private static final int ATT_ELEM_COUNT_LEN = 4;
    private static final int ATT_ELEM_DATA_SIZE_LEN = 4;
    private static final int ATT_DIRTY_LEN = 1;

    private static final int OFFSET_ELEM_COUNT = 0;
    private static final int OFFSET_ELEM_DATA_SIZE = OFFSET_ELEM_COUNT + ATT_ELEM_COUNT_LEN;
    private static final int OFFSET_DIRTY = OFFSET_ELEM_DATA_SIZE + ATT_ELEM_DATA_SIZE_LEN;
    private static final int OFFSET_ELEM_DATA = OFFSET_DIRTY + ATT_DIRTY_LEN;

    private static final int BASE_SEGMENT_SIZE = ATT_ELEM_COUNT_LEN + ATT_ELEM_DATA_SIZE_LEN + ATT_DIRTY_LEN;

    private static final int BYTE = 8;


    private long internal_ptr_elem_data_idx(int index) {
        return internal_ptr_elem_data_idx(_start_address + OFFSET_DIRTY + 1, index);
    }

    private long internal_ptr_elem_data_idx(long startAddress, int index) {
        return startAddress + index * BYTE * 3;
    }

    private int internal_size_data_segment(int capacity) {
        return capacity * BYTE * 3;
    }


    public OffHeapLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this._initalCapacity = p_initalCapacity;
        this._loadFactor = p_loadFactor;

        int bytes = BASE_SEGMENT_SIZE + internal_size_data_segment(p_initalCapacity);
        _start_address = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_start_address, bytes, (byte) -1);

        UNSAFE.putInt(_start_address + OFFSET_DIRTY, 0);
        UNSAFE.putInt(_start_address + OFFSET_ELEM_DATA_SIZE, _initalCapacity);

        computeMaxSize();
    }

    public final void clear() {
        if (UNSAFE.getInt(_start_address + OFFSET_DIRTY) > 0) {
            UNSAFE.putInt(_start_address + OFFSET_DIRTY, 0);

            long bytes = BASE_SEGMENT_SIZE + _initalCapacity * BYTE;
            _start_address = UNSAFE.reallocateMemory(_start_address, bytes);

            UNSAFE.putInt(_start_address + OFFSET_ELEM_DATA_SIZE, _initalCapacity);
        }
    }

    private void computeMaxSize() {
        _threshold = (int) (UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE) * _loadFactor);
    }

    @Override
    public final boolean contains(long key) {
        if (UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE) == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE);
        long m = findNonNullKeyEntry(key, index);

        return m != KConfig.NULL_LONG;
    }

    @Override
    public final long get(long key) {
        if (UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE) == 0) {
            return KConfig.NULL_LONG;
        }
        long m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE);
        m = findNonNullKeyEntry(key, index);
        if (m != KConfig.NULL_LONG) {
            return UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_VALUE * BYTE);
        }
        return KConfig.NULL_LONG;
    }

    public final long findNonNullKeyEntry(long key, int index) {
        long m = UNSAFE.getLong(internal_ptr_elem_data_idx(index));
        while (m != -1) {
            if (key == UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_KEY * BYTE)) {
                return m;
            }
            m = UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_NEXT * BYTE);
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public final void each(KLongLongMapCallBack callback) {
        for (int i = 0; i < UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE); i++) {
            if (UNSAFE.getLong(internal_ptr_elem_data_idx(i)) != -1) {
                long current_ptr = internal_ptr_elem_data_idx(i);

                long current_key = UNSAFE.getLong(current_ptr + POS_KEY * BYTE);
                long current_value = UNSAFE.getLong(current_ptr + POS_VALUE * BYTE);
                long current_next = UNSAFE.getLong(current_ptr + POS_NEXT * BYTE);

                callback.on(current_key, current_value);
                while (current_next == KConfig.NULL_LONG) {
                    current_ptr = current_next;
                    current_key = UNSAFE.getLong(current_ptr + POS_KEY * BYTE);
                    current_value = UNSAFE.getLong(current_ptr + POS_VALUE * BYTE);

                    callback.on(current_key, current_value);
                }
            }
        }
    }

    @Override
    public final synchronized void put(long key, long value) {
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 1);

        long entry = KConfig.NULL_LONG;
        int index = -1;
        int hash = (int) (key);
        if (UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE) != 0) {
            index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE);
            entry = findNonNullKeyEntry(key, index);
        }
        // TODO handle conflicts!
        if (entry == KConfig.NULL_LONG) {
            UNSAFE.putInt(_start_address + OFFSET_DIRTY, UNSAFE.getInt(_start_address + OFFSET_DIRTY) + 1);
            if (UNSAFE.getInt(_start_address + OFFSET_DIRTY) > _threshold) {
                rehash();
                index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE);
            }
            entry = createHashedEntry(key, index);
        }

        UNSAFE.putLong(entry + POS_VALUE * BYTE, value);
    }

    public final long createHashedEntry(long key, int index) {
        long entry_pos = internal_ptr_elem_data_idx(index);
        UNSAFE.putLong(entry_pos + POS_KEY * BYTE, key);
        UNSAFE.putLong(entry_pos + POS_VALUE * BYTE, KConfig.NULL_LONG);

//        if (index >= 1) {
//            long prev_pos = internal_ptr_elem_data_idx(index - 1);
//            UNSAFE.putLong(prev_pos + POS_NEXT * 8, entry_pos);
//        }

        return entry_pos;
    }

    public final void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);

        long bytes = internal_size_data_segment(length);
        long _new_data_start = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_new_data_start, bytes, (byte) -1);

        for (int i = 0; i < UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE); i++) {
            long entry_pos = internal_ptr_elem_data_idx(i);
            while (entry_pos != -1) {
                long entry_key = UNSAFE.getLong(entry_pos + POS_KEY * BYTE);
                int index = ((int) entry_key & 0x7FFFFFFF) % length;

                long next = UNSAFE.getLong(entry_pos + POS_NEXT * BYTE);
                UNSAFE.putLong(entry_pos + POS_NEXT * BYTE, UNSAFE.getLong(internal_ptr_elem_data_idx(_new_data_start, index)));
                UNSAFE.putLong(internal_ptr_elem_data_idx(_new_data_start, index), entry_pos);

                entry_pos = next;
            }
        }

        _start_address = UNSAFE.reallocateMemory(_start_address, BASE_SEGMENT_SIZE + bytes);
        UNSAFE.copyMemory(_new_data_start, _start_address + OFFSET_ELEM_DATA, bytes);
        UNSAFE.freeMemory(_new_data_start);
        _allocated_segments--;

        UNSAFE.putInt(_start_address + OFFSET_ELEM_DATA_SIZE, length);
        computeMaxSize();
    }

    public final void rehash() {
        rehashCapacity(UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE));
    }

    public final void remove(long key) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_ELEM_DATA_SIZE);
        if (elementDataSize == 0) {
            return;
        }
        int index = 0;
        long entry_pos;
        long last_pos = KConfig.NULL_LONG;
        int hash = (int) (key);
        index = (hash & 0x7FFFFFFF) % elementDataSize;

        entry_pos = internal_ptr_elem_data_idx(index);
        long entry_key = UNSAFE.getLong(entry_pos + POS_KEY * BYTE);
        while (entry_pos != KConfig.NULL_LONG && !(/*((int)segment.key) == hash &&*/ key == entry_key)) {
            last_pos = entry_pos;
            entry_pos = UNSAFE.getLong(entry_pos + POS_NEXT * BYTE);
        }
        if (entry_pos == KConfig.NULL_LONG) {
            return;
        }
        if (last_pos == KConfig.NULL_LONG) {
            UNSAFE.putLong(internal_ptr_elem_data_idx(index), UNSAFE.getLong(entry_pos + POS_NEXT * BYTE));
        } else {
            UNSAFE.putLong(last_pos + POS_NEXT * BYTE, UNSAFE.getLong(entry_pos + POS_NEXT * BYTE));
        }
        UNSAFE.putInt(_start_address + OFFSET_DIRTY, UNSAFE.getInt(_start_address + OFFSET_DIRTY) - 1);
    }

    public final int size() {
        return UNSAFE.getInt(_start_address + OFFSET_DIRTY);
    }


    @SuppressWarnings("restriction")
    protected static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }
}



