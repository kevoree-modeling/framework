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

        UNSAFE.putInt(internal_ptr_elementCount(), 0);
        UNSAFE.putInt(internal_ptr_elementDataSize(), _initalCapacity);

        this._threshold = (int) (UNSAFE.getInt(internal_ptr_elementDataSize()) * this._loadFactor);
    }

    private int internal_size_base(int length) {
        return 4 + 4 + length * 8;
    }

    private long internal_ptr_elementCount() {
        return _start_address;
    }

    private long internal_ptr_elementDataSize() {
        return internal_ptr_elementCount() + 4;
    }

    private long internal_ptr_elementData() {
        return internal_ptr_elementDataSize() + 4;
    }

    private long internal_ptr_elementData(int index) {
        return internal_ptr_elementData() + index * 8;
    }

    private long internal_ptr_entry_next(long entry_ptr) {
        return entry_ptr;
    }

    private long internal_ptr_entry_universe(long entry_ptr) {
        return internal_ptr_entry_next(entry_ptr) + 8;
    }

    private long internal_ptr_entry_time(long entry_ptr) {
        return internal_ptr_entry_universe(entry_ptr) + 8;
    }

    private long internal_ptr_entry_obj(long entry_ptr) {
        return internal_ptr_entry_time(entry_ptr) + 8;
    }

    private long internal_ptr_entry_value(long entry_ptr) {
        return internal_ptr_entry_obj(entry_ptr) + 8;
    }

    private int internal_inc_ElementCount() {
        int c = UNSAFE.getInt(internal_ptr_elementCount()) + 1;
        UNSAFE.putInt(internal_ptr_elementCount(), c);
        return c;
    }

    private int internal_dec_elementCount() {
        int c = UNSAFE.getInt(internal_ptr_elementCount()) - 1;
        UNSAFE.putInt(internal_ptr_elementCount(), c);
        return c;
    }

    @Override
    public KMemoryElement get(long universe, long time, long obj) {
        int elementDataSize = UNSAFE.getInt(internal_ptr_elementDataSize());
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
        int elementDataSize = UNSAFE.getInt(internal_ptr_elementDataSize());
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
        int newElementCount = internal_inc_ElementCount();

        if (newElementCount > this._threshold) {
            int length = (UNSAFE.getInt(internal_ptr_elementDataSize() == 0 ? 1 : UNSAFE.getInt(internal_ptr_elementDataSize()) << 1));
            int size = internal_size_base(length);
            this._start_address = UNSAFE.reallocateMemory(_start_address, size);

            for (int i = 0; i < UNSAFE.getInt(internal_ptr_elementDataSize()); i++) {
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
            UNSAFE.putInt(internal_ptr_elementDataSize(), length);
            this._threshold = (int) (UNSAFE.getInt(internal_ptr_elementDataSize()) * this._loadFactor);
            index = (hash & 0x7FFFFFFF) % UNSAFE.getInt(internal_ptr_elementDataSize());
        }
        // allocate space for new entry
        long entry_ptr = UNSAFE.allocateMemory(5 * 8); // next, universe, time, obj, value pointers
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
        for (int i = 0; i < UNSAFE.getInt(internal_ptr_elementCount()); i++) {
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
        if (UNSAFE.getInt(internal_ptr_elementCount()) > 0) {
            UNSAFE.putInt(internal_ptr_elementCount(), 0);

            long size = 4 + 4 + KConfig.CACHE_INIT_SIZE * 8;
            this._start_address = UNSAFE.reallocateMemory(_start_address, size);
            UNSAFE.setMemory(_start_address, size, (byte) 0);
            UNSAFE.putInt(internal_ptr_elementDataSize(), this._initalCapacity);
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
        int elementDataSize = UNSAFE.getInt(internal_ptr_elementDataSize());

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
