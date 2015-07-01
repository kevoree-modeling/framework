
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


    private long internal_ptr_elem_count() {
        return _start_address;
    }

    private long internal_ptr_elem_data_size() {
        return internal_ptr_elem_count() + 4;
    }

    private long internal_ptr_dirty() {
        return internal_ptr_elem_data_size() + 4;
    }

    private long internal_ptr_elem_data() {
        return internal_ptr_dirty() + 1;
    }

    private long internal_ptr_elem_data_idx(int index) {
        return internal_ptr_elem_data_idx(internal_ptr_dirty() + 1, index);
    }

    private long internal_ptr_elem_data_idx(long startAddress, int index) {
        return startAddress + index * 8 * 3;
    }

    private int internal_size_base_segment() {
        return 4 + 4 + 1;
    }

    private int internal_size_data_segment(int capacity) {
        return capacity * 8 * 3;
    }


    public OffHeapLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this._initalCapacity = p_initalCapacity;
        this._loadFactor = p_loadFactor;

        int bytes = internal_size_base_segment() + internal_size_data_segment(p_initalCapacity);
        _start_address = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_start_address, bytes, (byte) -1);

        UNSAFE.putInt(internal_ptr_elem_count(), 0);
        UNSAFE.putInt(internal_ptr_elem_data_size(), _initalCapacity);

        computeMaxSize();
    }

    public void clear() {
        if (UNSAFE.getInt(internal_ptr_elem_count()) > 0) {
            UNSAFE.putInt(internal_ptr_elem_count(), 0);

            long bytes = internal_size_base_segment() + _initalCapacity * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, bytes);

            UNSAFE.putInt(internal_ptr_elem_data_size(), _initalCapacity);
        }
    }

    private void computeMaxSize() {
        _threshold = (int) (UNSAFE.getInt(internal_ptr_elem_data_size()) * _loadFactor);
    }

    @Override
    public boolean contains(long key) {
        if (UNSAFE.getInt(internal_ptr_elem_data_size()) == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(internal_ptr_elem_data_size());
        long m = findNonNullKeyEntry(key, index);

        return m != KConfig.NULL_LONG;
    }

    @Override
    public long get(long key) {
        if (UNSAFE.getInt(internal_ptr_elem_data_size()) == 0) {
            return KConfig.NULL_LONG;
        }
        long m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(internal_ptr_elem_data_size());
        m = findNonNullKeyEntry(key, index);
        if (m != KConfig.NULL_LONG) {
            return UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_VALUE * 8);
        }
        return KConfig.NULL_LONG;
    }

    final long findNonNullKeyEntry(long key, int index) {
        long m = UNSAFE.getLong(internal_ptr_elem_data_idx(index));
        while (m != -1) {
            if (key == UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_KEY * 8)) {
                return m;
            }
            m = UNSAFE.getLong(internal_ptr_elem_data_idx(index) + POS_NEXT * 8);
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        for (int i = 0; i < UNSAFE.getInt(internal_ptr_elem_data_size()); i++) {
            if (UNSAFE.getLong(internal_ptr_elem_data_idx(i)) != -1) {
                long current_ptr = internal_ptr_elem_data_idx(i);

                long current_key = UNSAFE.getLong(current_ptr + POS_KEY * 8);
                long current_value = UNSAFE.getLong(current_ptr + POS_VALUE * 8);
                long current_next = UNSAFE.getLong(current_ptr + POS_NEXT * 8);

                callback.on(current_key, current_value);
                while (current_next == KConfig.NULL_LONG) {
                    current_ptr = current_next;
                    current_key = UNSAFE.getLong(current_ptr + POS_KEY * 8);
                    current_value = UNSAFE.getLong(current_ptr + POS_VALUE * 8);

                    callback.on(current_key, current_value);
                }
            }
        }
    }

    @Override
    public synchronized void put(long key, long value) {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 1);

        long entry = KConfig.NULL_LONG;
        int index = -1;
        int hash = (int) (key);
        if (UNSAFE.getInt(internal_ptr_elem_data_size()) != 0) {
            index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(internal_ptr_elem_data_size());
            entry = findNonNullKeyEntry(key, index);
        }
        // TODO handle conflicts!
        if (entry == KConfig.NULL_LONG) {
            UNSAFE.putInt(internal_ptr_elem_count(), UNSAFE.getInt(internal_ptr_elem_count()) + 1);
            if (UNSAFE.getInt(internal_ptr_elem_count()) > _threshold) {
                rehash();
                index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(internal_ptr_elem_data_size());
            }
            entry = createHashedEntry(key, index);
        }

        UNSAFE.putLong(entry + POS_VALUE * 8, value);
    }

    long createHashedEntry(long key, int index) {
        long entry_pos = internal_ptr_elem_data_idx(index);
        UNSAFE.putLong(entry_pos + POS_KEY * 8, key);
        UNSAFE.putLong(entry_pos + POS_VALUE * 8, KConfig.NULL_LONG);

//        if (index >= 1) {
//            long prev_pos = internal_ptr_elem_data_idx(index - 1);
//            UNSAFE.putLong(prev_pos + POS_NEXT * 8, entry_pos);
//        }

        return entry_pos;
    }

    void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);

        long bytes = internal_size_data_segment(length);
        long _new_data_start = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_new_data_start, bytes, (byte) -1);

        for (int i = 0; i < UNSAFE.getInt(internal_ptr_elem_data_size()); i++) {
            long entry_pos = internal_ptr_elem_data_idx(i);
            while (entry_pos != -1) {
                long entry_key = UNSAFE.getLong(entry_pos + POS_KEY * 8);
                int index = ((int) entry_key & 0x7FFFFFFF) % length;

                long next = UNSAFE.getLong(entry_pos + POS_NEXT * 8);
                UNSAFE.putLong(entry_pos + POS_NEXT * 8, UNSAFE.getLong(internal_ptr_elem_data_idx(_new_data_start, index)));
                UNSAFE.putLong(internal_ptr_elem_data_idx(_new_data_start, index), entry_pos);

                entry_pos = next;
            }
        }

        _start_address = UNSAFE.reallocateMemory(_start_address, internal_size_base_segment() + bytes);
        UNSAFE.copyMemory(_new_data_start, internal_ptr_elem_data(), bytes);
        UNSAFE.freeMemory(_new_data_start);
        _allocated_segments--;

        UNSAFE.putInt(internal_ptr_elem_data_size(), length);
        computeMaxSize();
    }

    void rehash() {
        rehashCapacity(UNSAFE.getInt(internal_ptr_elem_data_size()));
    }

    public void remove(long key) {
        int elementDataSize = UNSAFE.getInt(internal_ptr_elem_data_size());
        if (elementDataSize == 0) {
            return;
        }
        int index = 0;
        long entry_pos;
        long last_pos = KConfig.NULL_LONG;
        int hash = (int) (key);
        index = (hash & 0x7FFFFFFF) % elementDataSize;

        entry_pos = internal_ptr_elem_data_idx(index);
        long entry_key = UNSAFE.getLong(entry_pos + POS_KEY * 8);
        while (entry_pos != KConfig.NULL_LONG && !(/*((int)segment.key) == hash &&*/ key == entry_key)) {
            last_pos = entry_pos;
            entry_pos = UNSAFE.getLong(entry_pos + POS_NEXT * 8);
        }
        if (entry_pos == KConfig.NULL_LONG) {
            return;
        }
        if (last_pos == KConfig.NULL_LONG) {
            UNSAFE.putLong(internal_ptr_elem_data_idx(index), UNSAFE.getLong(entry_pos + POS_NEXT * 8));
        } else {
            UNSAFE.putLong(last_pos + POS_NEXT * 8, UNSAFE.getLong(entry_pos + POS_NEXT * 8));
        }
        UNSAFE.putInt(internal_ptr_elem_count(), UNSAFE.getInt(internal_ptr_elem_count()) - 1);
    }

    public int size() {
        return UNSAFE.getInt(internal_ptr_elem_count());
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



