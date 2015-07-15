package org.kevoree.modeling.memory.struct.cache.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.manager.impl.ResolutionHelper;
import org.kevoree.modeling.memory.struct.OffHeapMemoryFactory;
import org.kevoree.modeling.memory.struct.segment.impl.OffHeapMemorySegment;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** @ignore ts
 * OffHeap implementation of KCache
 * - memory structure:  |elem count |elem data size |elem data              |
 * -                    |(4 byte)   |(4 byte)       |(x * num of elem byte) |
 * -
 * - elem data:         |entry ptr  |...    |...    |
 * -                    |(8 byte)   |...    |...    |
 * -
 * - entry:             |next ptr   |universe   |time       |obj        |segment ptr    |
 * -                    |(8 byte)   |(8 byte)   |(8 byte)   |(8 byte)   |(8 byte)       |
 */
public class OffHeapMemoryCache implements KCache {
    private static final Unsafe UNSAFE = getUnsafe();

    // base segment
    private static final int ATT_ELEM_COUNT_LEN = 4;
    private static final int ATT_ELEM_DATA_SIZE_LEN = 4;
    // element data segment
    private static final int ATT_ENTRY_PTR_LEN = 8;
    // entry segment
    private static final int ATT_NEXT_PTR_LEN = 8;
    private static final int ATT_UNIVERSE_LEN = 8;
    private static final int ATT_TIME_LEN = 8;
    private static final int ATT_OBJ_LEN = 8;
    private static final int ATT_SEGMENT_PTR_LEN = 8;

    // relative to start address
    private static final int OFFSET_STARTADDRESS_ELEM_COUNT = 0;
    private static final int OFFSET_STARTADDRESS_ELEM_DATA_SIZE = OFFSET_STARTADDRESS_ELEM_COUNT + ATT_ELEM_COUNT_LEN;
    private static final int OFFSET_STARTADDRESS_ELEM_DATA = OFFSET_STARTADDRESS_ELEM_DATA_SIZE + ATT_ELEM_DATA_SIZE_LEN;
    // relative to entry ptr
    private static final int OFFSET_ENTRYPTR_NEXT_PTR = 0;
    private static final int OFFSET_ENTRYPTR_UNIVERSE = OFFSET_ENTRYPTR_NEXT_PTR + ATT_NEXT_PTR_LEN;
    private static final int OFFSET_ENTRYPTR_TIME = OFFSET_ENTRYPTR_UNIVERSE + ATT_UNIVERSE_LEN;
    private static final int OFFSET_ENTRYPTR_OBJ = OFFSET_ENTRYPTR_TIME + ATT_TIME_LEN;
    private static final int OFFSET_ENTRYPTR_SEGMENT_PTR = OFFSET_ENTRYPTR_OBJ + ATT_OBJ_LEN;

    private static final int BYTE = 8;

    OffHeapMemoryFactory factory = new OffHeapMemoryFactory();

    private final float _loadFactor;
    private final int _initalCapacity;
    private int _threshold;

    private KObjectWeakReference rootReference = null;

    private long _start_address;
    private int _allocated_segments = 0;

    public OffHeapMemoryCache() {
        this._initalCapacity = KConfig.CACHE_INIT_SIZE;
        this._loadFactor = KConfig.CACHE_LOAD_FACTOR;

        long size = internal_size_base(KConfig.CACHE_INIT_SIZE);
        this._start_address = UNSAFE.allocateMemory(size);
        UNSAFE.setMemory(_start_address, size, (byte) 0);
        this._allocated_segments++;

        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, 0);
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, _initalCapacity);

        this._threshold = (int) (UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE) * this._loadFactor);
    }

    private int internal_size_base(int length) {
        return ATT_ELEM_COUNT_LEN + ATT_ELEM_DATA_SIZE_LEN + length * BYTE;
    }

    private long internal_ptr_elementData(int index) {
        return _start_address + OFFSET_STARTADDRESS_ELEM_DATA + index * BYTE;
    }

    private long internal_ptr_entry_next(long entry_ptr) {
        return entry_ptr + OFFSET_ENTRYPTR_NEXT_PTR;
    }

    private long internal_ptr_entry_universe(long entry_ptr) {
        return entry_ptr + OFFSET_ENTRYPTR_UNIVERSE;
    }

    private long internal_ptr_entry_time(long entry_ptr) {
        return entry_ptr + OFFSET_ENTRYPTR_TIME;
    }

    private long internal_ptr_entry_obj(long entry_ptr) {
        return entry_ptr + OFFSET_ENTRYPTR_OBJ;
    }

    private long internal_ptr_entry_value(long entry_ptr) {
        return entry_ptr + OFFSET_ENTRYPTR_SEGMENT_PTR;
    }

    private int internal_inc_elementCount() {
        int c = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT) + 1;
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, c);
        return c;
    }

    private int internal_dec_elementCount() {
        int c = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT) - 1;
        UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, c);
        return c;
    }

    @Override
    public KMemoryElement get(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        if (elementDataSize == 0) {
            return null;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % elementDataSize;
        long m_entry_ptr = UNSAFE.getLong(internal_ptr_elementData(index));
        while (m_entry_ptr != 0) {
            long m_universe = UNSAFE.getLong(internal_ptr_entry_universe(m_entry_ptr));
            long m_time = UNSAFE.getLong(internal_ptr_entry_time(m_entry_ptr));
            long m_obj = UNSAFE.getLong(internal_ptr_entry_obj(m_entry_ptr));
            if (m_universe == universe && m_time == time && m_obj == obj) {

                KMemoryElement elem = factory.newFromKey(m_universe, m_time, m_obj);
                if (!(elem instanceof KOffHeapMemoryElement)) {
                    throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
                }
                KOffHeapMemoryElement offHeapElem = (KOffHeapMemoryElement) elem;
                offHeapElem.setMemoryAddress(UNSAFE.getLong(internal_ptr_entry_value(m_entry_ptr)));
                return elem;
            }
            m_entry_ptr = UNSAFE.getLong(internal_ptr_entry_next(m_entry_ptr));
        }
        return null;
    }

    @Override
    public void put(long universe, long time, long obj, KMemoryElement payload) {
        if (!(payload instanceof KOffHeapMemoryElement)) {
            throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
        }

        long entry_ptr = 0;

        int hash = (int) (universe ^ time ^ obj);
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        if (elementDataSize != 0) {
            long m_entry_ptr = UNSAFE.getLong(internal_ptr_elementData(index));
            while (m_entry_ptr != 0) {
                long m_universe = UNSAFE.getLong(internal_ptr_entry_universe(m_entry_ptr));
                long m_time = UNSAFE.getLong(internal_ptr_entry_time(m_entry_ptr));
                long m_obj = UNSAFE.getLong(internal_ptr_entry_obj(m_entry_ptr));
                if (m_universe == universe && m_time == time && m_obj == obj) {
                    entry_ptr = m_entry_ptr;
                    break;
                }
                m_entry_ptr = UNSAFE.getLong(internal_ptr_entry_next(m_entry_ptr));
            }
        }
        if (entry_ptr == 0) {
            entry_ptr = complex_insert(index, hash, universe, time, obj);
        }

        KOffHeapMemoryElement memoryElement = (KOffHeapMemoryElement) payload;
        UNSAFE.putLong(internal_ptr_entry_value(entry_ptr), memoryElement.getMemoryAddress());
    }

    private synchronized long complex_insert(int previousIndex, int hash, long universe, long time, long obj) {
        int index = previousIndex;
        int newElementCount = internal_inc_elementCount();

        if (newElementCount > this._threshold) {
            int length = (UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE == 0 ? 1 : UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE) << 1));
            int size = internal_size_base(length);
            this._start_address = UNSAFE.reallocateMemory(_start_address, size);

            for (int i = 0; i < UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE); i++) {
                long entry_ptr = internal_ptr_elementData(i);

                while (entry_ptr != 0) {
                    long entry_universe = internal_ptr_entry_universe(entry_ptr);
                    long entry_time = internal_ptr_entry_time(entry_ptr);
                    long entry_obj = internal_ptr_entry_obj(entry_ptr);

                    index = ((int) (entry_universe ^ entry_time ^ entry_obj) & 0x7FFFFFFF) % length;
                    long next_ptr = internal_ptr_entry_next(entry_ptr);
                    UNSAFE.putLong(internal_ptr_entry_next(entry_ptr), UNSAFE.getLong(internal_ptr_elementData(index)));
                    UNSAFE.putLong(internal_ptr_elementData(index), entry_ptr);
                    entry_ptr = next_ptr;
                }
            }
            //elementData = newData;
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, length);
            this._threshold = (int) (UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE) * this._loadFactor);
            index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        }
        // allocate space for new entry
        long entry_ptr = UNSAFE.allocateMemory(5 * BYTE); // next, universe, time, obj, value pointers
        this._allocated_segments++;

        UNSAFE.putLong(internal_ptr_entry_universe(entry_ptr), universe);
        UNSAFE.putLong(internal_ptr_entry_time(entry_ptr), time);
        UNSAFE.putLong(internal_ptr_entry_obj(entry_ptr), obj);
        UNSAFE.putLong(internal_ptr_entry_next(entry_ptr), internal_ptr_elementData(index));

        UNSAFE.putLong(internal_ptr_elementData(index), entry_ptr);

        return entry_ptr;
    }

    @Override
    public KCacheDirty[] dirties() {
        return new KCacheDirty[0];
    }

    @Override
    public void clear(KMetaModel metaModel) {
        for (int i = 0; i < UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT); i++) {
            long e_ptr = UNSAFE.getLong(internal_ptr_elementData(i));
            while (e_ptr != 0) {

                long e_universe = UNSAFE.getLong(internal_ptr_entry_universe(e_ptr));
                long e_time = UNSAFE.getLong(internal_ptr_entry_time(e_ptr));
                long e_obj = UNSAFE.getLong(internal_ptr_entry_obj(e_ptr));

                KMemoryElement elem = factory.newFromKey(e_universe, e_time, e_obj);
                if (!(elem instanceof KOffHeapMemoryElement)) {
                    throw new RuntimeException("OffHeapMemoryCache only supports OffHeapMemoryElements");
                }

                KOffHeapMemoryElement offHeapElem = (KOffHeapMemoryElement) elem;
                offHeapElem.setMemoryAddress(e_ptr);
                offHeapElem.free(metaModel);
                e_ptr = UNSAFE.getLong(internal_ptr_entry_next(e_ptr));
            }
        }
        if (UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT) > 0) {
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, 0);

            long size = 4 + 4 + KConfig.CACHE_INIT_SIZE * BYTE;
            this._start_address = UNSAFE.reallocateMemory(_start_address, size);
            UNSAFE.setMemory(_start_address, size, (byte) 0);
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE, this._initalCapacity);
        }
    }

    @Override
    public void clean(KMetaModel metaModel) {
        common_clean_monitor(null, metaModel);
    }

    private synchronized void common_clean_monitor(KObject origin, KMetaModel p_metaModel) {
        if (origin != null) {
            if (rootReference != null) {
                rootReference.next = new KObjectWeakReference(origin);
            } else {
                rootReference = new KObjectWeakReference(origin);
            }
        } else {
            KObjectWeakReference current = rootReference;
            KObjectWeakReference previous = null;
            while (current != null) {
                //process current
                if (current.get() == null) {
                    //check is dirty
                    OffHeapMemorySegment currentEntry = (OffHeapMemorySegment) this.get(current.universe, current.time, current.uuid);
                    if (currentEntry == null || !currentEntry.isDirty()) {
                        //call the clean sub process for universe/time/uuid
                        MemorySegmentResolutionTrace resolved = ResolutionHelper.resolve_trees(current.universe, current.time, current.uuid, this);
                        resolved.getUniverseTree().dec();
                        if (resolved.getUniverseTree().counter() <= 0) {
                            remove(KConfig.NULL_LONG, KConfig.NULL_LONG, current.uuid, p_metaModel);
                        }
                        resolved.getTimeTree().dec();
                        if (resolved.getTimeTree().counter() <= 0) {
                            remove(resolved.getUniverse(), KConfig.NULL_LONG, current.uuid, p_metaModel);
                        }
                        resolved.getSegment().dec();
                        if (resolved.getSegment().counter() <= 0) {
                            remove(resolved.getUniverse(), resolved.getTime(), current.uuid, p_metaModel);
                        }
                        //change chaining
                        if (previous == null) { //first case
                            rootReference = current.next;
                        } else { //in the middle case
                            previous.next = current.next;
                        }
                    }
                }
                previous = current;
                current = current.next;
            }
        }
    }

    private void remove(long universe, long time, long obj, KMetaModel p_metaModel) {
        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);

        int hash = (int) (universe ^ time ^ obj);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        if (elementDataSize != 0) {
            long previous = 0;
            long m = UNSAFE.getLong(internal_ptr_elementData(index));
            while (m != 0) {
                long m_universe = UNSAFE.getLong(internal_ptr_entry_universe(m));
                long m_time = UNSAFE.getLong(internal_ptr_entry_time(m));
                long m_obj = UNSAFE.getLong(internal_ptr_entry_obj(m));

                if (m_universe == universe && m_time == time && m_obj == obj) {
                    internal_dec_elementCount();
                    try {
                        long m_value = UNSAFE.getLong(internal_ptr_entry_value(m));
                        KOffHeapMemoryElement memoryElement = (KOffHeapMemoryElement) factory.newFromKey(m_universe, m_time, m_obj);
                        memoryElement.setMemoryAddress(m_value);
                        memoryElement.free(p_metaModel);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (previous == 0) {
                        long m_next = UNSAFE.getLong(internal_ptr_entry_next(m));
                        UNSAFE.putLong(internal_ptr_elementData(index), m_next);
                    } else {
                        long m_next = UNSAFE.getLong(internal_ptr_entry_next(m));
                        UNSAFE.putLong(internal_ptr_entry_next(previous), m_next);
                    }
                }
                previous = m;
                m = UNSAFE.getLong(internal_ptr_entry_next(m));
            }
        }
    }


    public void monitor(KObject origin) {
        common_clean_monitor(origin, null);
    }


    @Override
    public int size() {
        return 0;
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
