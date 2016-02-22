package org.kevoree.modeling.memory.space.impl.press;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.*;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.PrimitiveHelper;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * memory structure:
 * | max entries (4) | elem count (4) | lru (8) | elem key3 (max entries * 3 * 8) |
 * | elem next (max entries * 4) | elem hash (max entries * 4) | elem hash lock (max entries * 4) |
 * | chunk (max entries * 8) | chunk type (max entries * 2) | dirty head (4) | dirty size (4) | dirty next (max entries * 4) |
 */
public class PressOffHeapChunkSpace implements KChunkSpace {
    // unsafe access
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    /**
     * shortcuts for memory access
     */
    private static final int ATT_MAX_ENTRIES_LEN = 4;
    private static final int ATT_ELEM_COUNT_LEN = 4;
    private static final int ATT_LRU_LEN = 8;
    private static final int ATT_ELEM_KEY3_LEN = 8;
    private static final int ATT_ELEM_NEXT_LEN = 4;
    private static final int ATT_ELEM_HASH_LEN = 4;
    private static final int ATT_ELEM_HASH_LOCK_LEN = 4;
    private static final int ATT_CHUNK_LEN = 8;
    private static final int ATT_CHUNK_TYPE_LEN = 2;
    private static final int ATT_DIRTY_HEAD_LEN = 4;
    private static final int ATT_DIRTY_SIZE_LEN = 4;
    private static final int ATT_DIRTY_NEXT_LEN = 4;

    private static final int OFFSET_MAX_ENTRIES = 0;
    private static final int OFFSET_ELEM_COUNT = OFFSET_MAX_ENTRIES + ATT_MAX_ENTRIES_LEN;
    private static final int OFFSET_LRU = OFFSET_ELEM_COUNT + ATT_ELEM_COUNT_LEN;
    private static final int OFFSET_ELEM_KEY3 = OFFSET_LRU + ATT_LRU_LEN;

    private long internal_offset_elem_key3(int index) {
        return OFFSET_LRU + ATT_LRU_LEN + index * 3 * ATT_ELEM_KEY3_LEN;
    }

    private long internal_offset_elem_next(int index) {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return OFFSET_ELEM_KEY3 + maxEntries * 3 * ATT_ELEM_KEY3_LEN + index * ATT_ELEM_NEXT_LEN;
    }

    private long internal_offset_elem_hash(int index) {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return internal_offset_elem_next(0) + maxEntries * ATT_ELEM_NEXT_LEN + index * ATT_ELEM_HASH_LEN;
    }

    private long internal_offset_elem_hash_lock(int index) {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return internal_offset_elem_hash(0) + maxEntries * ATT_ELEM_HASH_LEN + index * ATT_ELEM_HASH_LOCK_LEN;
    }

    private long internal_offset_chunk(int index) {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return internal_offset_elem_hash_lock(0) + maxEntries * ATT_ELEM_HASH_LOCK_LEN + index * ATT_CHUNK_LEN;
    }

    private long internal_offset_chunk_type(int index) {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return internal_offset_chunk(0) + maxEntries * ATT_CHUNK_LEN + index * ATT_CHUNK_TYPE_LEN;
    }

    private long internal_offset_dirty_head() {
        long maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        return internal_offset_chunk_type(0) + maxEntries * ATT_CHUNK_TYPE_LEN;
    }

    private long internal_offset_dirty_size() {
        return internal_offset_dirty_head() + ATT_DIRTY_HEAD_LEN;
    }

    private long internal_offset_dirty_next() {
        return internal_offset_dirty_size() + ATT_DIRTY_SIZE_LEN;
    }

    /**
     * variables
     */
    private KInternalDataManager _manager = null;
    private volatile long _start_address;

    public PressOffHeapChunkSpace(int maxEntries) {
        // allocate space
        long mem = ATT_MAX_ENTRIES_LEN + ATT_ELEM_COUNT_LEN + ATT_LRU_LEN
                + (maxEntries * ATT_ELEM_KEY3_LEN * 3) + (maxEntries * ATT_ELEM_NEXT_LEN)
                + (maxEntries * ATT_ELEM_HASH_LEN) + (maxEntries * ATT_ELEM_HASH_LOCK_LEN)
                + (maxEntries * ATT_CHUNK_LEN) + (maxEntries * ATT_CHUNK_TYPE_LEN)
                + ATT_DIRTY_HEAD_LEN + ATT_DIRTY_SIZE_LEN + (maxEntries * ATT_DIRTY_NEXT_LEN);

        this._start_address = UNSAFE.allocateMemory(mem);

        // init
        UNSAFE.putInt(this._start_address + OFFSET_MAX_ENTRIES, maxEntries);
        // create and link lru list
        FixedOffHeapFIFO lru = new FixedOffHeapFIFO(-1, maxEntries);
        UNSAFE.putLong(this._start_address + OFFSET_LRU, lru.getMemoryAddress());

        UNSAFE.putInt(this._start_address + OFFSET_ELEM_COUNT, 1);

        // init
        for (int i = 0; i < maxEntries; i++) {
            UNSAFE.putInt(this._start_address + internal_offset_elem_next(i), -1);
            UNSAFE.putInt(this._start_address + internal_offset_elem_hash(i), -1);
            UNSAFE.putInt(this._start_address + internal_offset_elem_hash_lock(i), -1);

            UNSAFE.putLong(this._start_address + internal_offset_chunk(i), -1);
        }
    }

    @Override
    public void setManager(KDataManager dataManager) {
        this._manager = (KInternalDataManager) dataManager;
    }

    @Override
    public KChunk get(long universe, long time, long obj) {
        // elem count == 0?
        if (UNSAFE.getInt(this._start_address + OFFSET_ELEM_COUNT) == 0) {
            return null;
        }

        int _maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        int index = (PrimitiveHelper.tripleHash(universe, time, obj) & 0x7FFFFFFF) % _maxEntries;
        int m = UNSAFE.getInt(this._start_address + internal_offset_elem_hash(index));

        while (m != -1) {
            long _universe = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m));
            long _time = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 1 * ATT_ELEM_KEY3_LEN);
            long _obj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 2 * ATT_ELEM_KEY3_LEN);

            if (universe == _universe && time == _time && obj == _obj) {
                //GET VALUE
                long chunk_addr = UNSAFE.getLong(this._start_address + internal_offset_chunk(m));
                short chunk_type = UNSAFE.getShort(this._start_address + internal_offset_chunk_type(m));

                KOffHeapChunk chunk = internal_createElement(chunk_addr, universe, time, obj, chunk_type);
                return chunk;

            } else {
                m = UNSAFE.getInt(this._start_address + internal_offset_elem_next(m));
            }
        }

        return null;
    }

    // TODO merge with get method
    private int getIndex(long universe, long time, long obj) {
        // elem count == 0?
        if (UNSAFE.getInt(this._start_address + OFFSET_ELEM_COUNT) == 0) {
            return -1;
        }

        int _maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        int index = (PrimitiveHelper.tripleHash(universe, time, obj) & 0x7FFFFFFF) % _maxEntries;
        int m = UNSAFE.getInt(this._start_address + internal_offset_elem_hash(index));
        while (m != -1) {
            long _universe = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m));
            long _time = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 1 * ATT_ELEM_KEY3_LEN);
            long _obj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 2 * ATT_ELEM_KEY3_LEN);
            if (universe == _universe && time == _time && obj == _obj) {
                return m;
            } else {
                m = UNSAFE.getInt(this._start_address + internal_offset_elem_next(m));
            }
        }

        return -1;
    }

    @Override
    public KChunk create(long universe, long time, long obj, short type, KMetaModel metaModel) {
        KOffHeapChunk newElement = internal_createElement(-1, universe, time, obj, type);
        return internal_put(universe, time, obj, newElement, metaModel);
    }

    private synchronized KChunk internal_put(long universe, long time, long obj, KOffHeapChunk payload, KMetaModel metaModel) {
        KOffHeapChunk result;
        int entry;
        int index;
        int hash = PrimitiveHelper.tripleHash(universe, time, obj);

        int _maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        index = (hash & 0x7FFFFFFF) % _maxEntries;

        long lru_addr = UNSAFE.getLong(this._start_address + OFFSET_LRU);
        FixedOffHeapFIFO lru = new FixedOffHeapFIFO(lru_addr, -1);

        entry = findNonNullKeyEntry(universe, time, obj, index);
        if (entry == -1) {
            //we look for nextIndex
            int nbTry = 0;
            int currentVictimIndex = lru.dequeue();

            long victimAddr = UNSAFE.getLong(this._start_address + internal_offset_chunk(currentVictimIndex));
            short vctimType = UNSAFE.getShort(this._start_address + internal_offset_chunk_type(currentVictimIndex));

            long victimUniverse = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(currentVictimIndex));
            long victimTime = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(currentVictimIndex) + 1 * ATT_ELEM_KEY3_LEN);
            long victimObj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(currentVictimIndex) + 2 * ATT_ELEM_KEY3_LEN);
            KOffHeapChunk victimChunk = internal_createElement(victimAddr, victimUniverse, victimTime, victimObj, vctimType);

            while (victimAddr != -1 && victimChunk.counter() > 0 /*&& nbTry < this._maxEntries*/) {
                lru.enqueue(currentVictimIndex);
                currentVictimIndex = lru.dequeue();
                nbTry++;
                if (nbTry % (_maxEntries / 10) == 0) {
                    System.gc();
                }
            }

            if (nbTry == _maxEntries) {
                throw new RuntimeException("Cache Loop");
            }

            if (victimAddr != -1) {
                int hashVictim = PrimitiveHelper.tripleHash(victimUniverse, victimTime, victimObj);
                //XOR three keys and hash according to maxEntries
                int indexVictim = (hashVictim & 0x7FFFFFFF) % _maxEntries;
                int previousMagic;
                do {
                    previousMagic = RandomUtil.nextInt();
                }
                while (!UNSAFE.compareAndSwapInt(null, this._start_address + internal_offset_elem_hash_lock(indexVictim), -1, previousMagic));

                //we obtains the token, now remove the element
                int m = UNSAFE.getInt(this._start_address + internal_offset_elem_hash_lock(indexVictim));
                int last = -1;
                while (m >= 0) {

                    long _universe = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m));
                    long _time = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 1 * ATT_ELEM_KEY3_LEN);
                    long _obj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 2 * ATT_ELEM_KEY3_LEN);

                    if (victimUniverse == _universe && victimTime == _time && victimObj == _obj) {
                        break;
                    }
                    last = m;
                    m = UNSAFE.getInt(this._start_address + internal_offset_elem_next(m));
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == -1) {
                    int previousNext = UNSAFE.getInt(this._start_address + internal_offset_elem_next(m));
                    UNSAFE.putInt(this._start_address + internal_offset_elem_hash(indexVictim), previousNext);
                } else {
                    UNSAFE.putInt(this._start_address + internal_offset_elem_next(last), UNSAFE.getInt(this._start_address + internal_offset_elem_next(m)));
                }
                UNSAFE.putInt(this._start_address + internal_offset_elem_next(m), -1);//flag to dropped value

                //UNREF victim value object
                UNSAFE.putLong(this._start_address + internal_offset_chunk(currentVictimIndex), -1);

                //free the lock
                UNSAFE.compareAndSwapInt(null, this._start_address + internal_offset_elem_hash_lock(indexVictim), previousMagic, -1);

                //TEST IF VICTIM IS DIRTY
                if ((victimChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT) {
                    //SAVE VICTIM
                    saveChunk(victimChunk, metaModel, new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            //free victim from memory
                            victimChunk.free(metaModel);
                        }
                    });
                } else {
                    //FREE VICTIM FROM MEMORY
                    victimChunk.free(metaModel);
                }
            }

            UNSAFE.putLong(this._start_address + internal_offset_elem_key3(currentVictimIndex), universe);
            UNSAFE.putLong(this._start_address + internal_offset_elem_key3(currentVictimIndex) + 1 * ATT_ELEM_KEY3_LEN, time);
            UNSAFE.putLong(this._start_address + internal_offset_elem_key3(currentVictimIndex) + 2 * ATT_ELEM_KEY3_LEN, obj);

            UNSAFE.putLong(this._start_address + internal_offset_chunk(currentVictimIndex), payload.memoryAddress());
            UNSAFE.putShort(this._start_address + internal_offset_chunk_type(currentVictimIndex), payload.type());

            int previousMagic;
            do {
                previousMagic = RandomUtil.nextInt();
            }
            while (!UNSAFE.compareAndSwapInt(null, this._start_address + internal_offset_elem_hash_lock(index), -1, previousMagic));

            UNSAFE.putInt(this._start_address + internal_offset_elem_next(currentVictimIndex), UNSAFE.getInt(this._start_address + internal_offset_elem_hash(index)));
            UNSAFE.putInt(this._start_address + internal_offset_elem_hash(index), currentVictimIndex);
            result = payload;

            //free the lock
            UNSAFE.compareAndSwapInt(null, this._start_address + internal_offset_elem_hash_lock(index), previousMagic, -1);
            UNSAFE.getAndAddInt(null, this._start_address + OFFSET_ELEM_COUNT, 1);

            //reEnqueue
            lru.enqueue(currentVictimIndex);

        } else {
            long result_addr = UNSAFE.getLong(this._start_address + internal_offset_chunk(entry));
            short result_type = UNSAFE.getShort(this._start_address + internal_offset_chunk_type(entry));

            result = internal_createElement(result_addr, universe, time, obj, result_type);
        }

        return result;
    }

    private int findNonNullKeyEntry(long universe, long time, long obj, int index) {
        int m = UNSAFE.getInt(this._start_address + internal_offset_elem_hash(index));
        while (m >= 0) {
            long _universe = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m));
            long _time = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 1 * ATT_ELEM_KEY3_LEN);
            long _obj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(m) + 2 * ATT_ELEM_KEY3_LEN);

            if (universe == _universe && time == _time && obj == _obj) {
                return m;
            }
            m = UNSAFE.getInt(this._start_address + internal_offset_elem_next(m));
        }
        return -1;
    }


    private KOffHeapChunk internal_createElement(long p_mem_addr, long p_universe, long p_time, long p_obj, short type) {
        switch (type) {
            case KChunkTypes.OBJECT_CHUNK:
                return new OffHeapObjectChunk(p_mem_addr, p_universe, p_time, p_obj, this);

            case KChunkTypes.LONG_LONG_MAP:
                return new OffHeapLongLongMap(p_mem_addr, p_universe, p_time, p_obj, this);

            case KChunkTypes.LONG_TREE:
                return new OffHeapLongTree(p_mem_addr, p_universe, p_time, p_obj, this);

//            case KChunkTypes.OBJECT_CHUNK_INDEX:
//                return new HeapObjectIndexChunk(p_universe, p_time, p_obj, this);

            default:
                return null;
        }
    }

    private void saveChunk(KChunk chunk, KMetaModel p_metaModel, KCallback<Throwable> result) {
        if (this._manager != null) {
            KContentDeliveryDriver cdn = this._manager.cdn();
            if (cdn != null) {
                long[] key = new long[3];
                key[0] = chunk.universe();
                key[1] = chunk.time();
                key[2] = chunk.obj();
                String[] payload = new String[1];
                payload[0] = chunk.serialize(p_metaModel);
                cdn.put(key, payload, new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        chunk.setFlags(0, KChunkFlags.DIRTY_BIT);
                        result.on(throwable);
                    }
                }, -1);
            }
        }
    }


    @Override
    public KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel) {
        return (KObjectChunk) internal_put(newUniverse, newTime, newObj, (KOffHeapChunk) previousElement.clone(newUniverse, newTime, newObj, metaModel), metaModel);
    }

    @Override
    public void clear(KMetaModel metaModel) {
        // TODO
    }

    @Override
    public void free(KMetaModel metaModel) {
        // TODO
    }

    @Override
    public void remove(long universe, long time, long obj, KMetaModel metaModel) {
        //NOOP, external remove is not allowed in press mode
    }

    @Override
    public int size() {
        return UNSAFE.getInt(this._start_address + OFFSET_ELEM_COUNT);
    }

    @Override
    public KChunkIterator detachDirties() {
        return null;
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {
        long universe = dirtyChunk.universe();
        long time = dirtyChunk.time();
        long obj = dirtyChunk.obj();
        int hash = (int) (universe ^ time ^ obj);

        int _maxEntries = UNSAFE.getInt(this._start_address + OFFSET_MAX_ENTRIES);
        int index = (hash & 0x7FFFFFFF) % _maxEntries;
        int entry = findNonNullKeyEntry(universe, time, obj, index);
        if (entry != -1) {
            //this._dirtyState.get().declareDirty(entry);

            int previous;
            boolean diff = false;
            do {
                previous = UNSAFE.getInt(this._start_address + internal_offset_dirty_head());
                if (previous != index) {
                    diff = true;
                }
            }
            while (!UNSAFE.compareAndSwapInt(null, this._start_address + internal_offset_dirty_head(), previous, index));

            if (diff) {
                UNSAFE.putInt(this._start_address + internal_offset_dirty_next(), previous);
                UNSAFE.getAndAddInt(null, this._start_address + internal_offset_dirty_size(), 1);
            }
        }
    }

    @Override
    public void printDebug(KMetaModel p_metaModel) {
        System.out.println(internal_toString(p_metaModel));
    }

    @Override
    public String toString() {
        return internal_toString(null);
    }

    private String internal_toString(KMetaModel p_metaModel) {
        return "asdf";
//        StringBuilder buffer = new StringBuilder();
//        try {
//            int elemCount = UNSAFE.getInt(this._start_address + OFFSET_ELEM_COUNT);
//            for (int i = 0; i < elemCount; i++) {
//                long addr = UNSAFE.getLong(this._start_address + internal_offset_chunk(i));
//                short type = UNSAFE.getShort(this._start_address + internal_offset_chunk_type(i));
//                long universe = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(i));
//                long time = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(i) + 1 * ATT_ELEM_KEY3_LEN);
//                long obj = UNSAFE.getLong(this._start_address + internal_offset_elem_key3(i) + 2 * ATT_ELEM_KEY3_LEN);
//
//                if (addr != -1) {
//                    KChunk loopChunk = internal_createElement(addr, universe, time, obj, type);
//
//                    String content;
//                    if (p_metaModel != null) {
//                        content = loopChunk.serialize(p_metaModel);
//                    } else {
//                        content = "no model";
//                    }
//                    buffer.append(i + "#:" + universe + "," + time + "," + obj + "=>" + loopChunk.type() + "(count:" + loopChunk.counter() + ",flag:" + loopChunk.getFlags() + ")" + "==>" + content + "\n");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return buffer.toString();
    }


    public final void notifyRealloc(long newAddress, long universe, long time, long obj) {
        int index = getIndex(universe, time, obj);
        if (index != -1) {
            long currentAddress = UNSAFE.getLong(this._start_address + internal_offset_chunk(index));
            if (currentAddress != newAddress) {
                UNSAFE.putLong(this._start_address + internal_offset_chunk(index), newAddress);
            }
        }
    }

}
