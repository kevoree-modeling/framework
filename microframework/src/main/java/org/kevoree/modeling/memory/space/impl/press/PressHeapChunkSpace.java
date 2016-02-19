
package org.kevoree.modeling.memory.space.impl.press;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongTree;
import org.kevoree.modeling.memory.chunk.impl.HeapObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.HeapObjectIndexChunk;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.PrimitiveHelper;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class PressHeapChunkSpace implements KChunkSpace {

    /**
     * Global
     */
    private final int _maxEntries;

    private final AtomicInteger _elementCount;

    private KInternalDataManager _manager = null;

    private PressFIFO _lru;

    /**
     * HashMap variables
     */

    private final long[] elementK3a;
    private final long[] elementK3b;
    private final long[] elementK3c;

    private final int[] elementNext;

    private final int[] elementHash;

    private final AtomicIntegerArray elementHashLock;

    private final KChunk[] _values;

    public KChunk[] values() {
        return this._values;
    }

    private AtomicInteger _collisions;

    public int collisions() {
        return this._collisions.get();
    }

    final class InternalDirtyState implements KChunkIterator {

        private AtomicInteger dirtyHead = new AtomicInteger(-1);

        private AtomicInteger dirtySize = new AtomicInteger(0);

        private int[] dirtyNext;

        private PressHeapChunkSpace _parent;

        public InternalDirtyState(int p_maxEntries, PressHeapChunkSpace p_parent) {
            dirtyNext = new int[p_maxEntries];
            this._parent = p_parent;
        }

        public void declareDirty(int index) {
            int previous;
            boolean diff = false;
            do {
                previous = dirtyHead.get();
                if (previous != index) {
                    diff = true;
                }
            } while (!dirtyHead.compareAndSet(previous, index));
            if (diff) {
                this.dirtyNext[index] = previous;
                dirtySize.incrementAndGet();
            }
        }

        @Override
        public boolean hasNext() {
            return this.dirtyHead.get() != -1;
        }

        @Override
        public KChunk next() {
            int unpop;
            int unpopNext;
            do {
                unpop = this.dirtyHead.get();
                unpopNext = this.dirtyNext[unpop];
            } while (unpop != -1 && !this.dirtyHead.compareAndSet(unpop, unpopNext));
            if (unpop == -1) {
                return null;
            } else {
                return this._parent.values()[unpop];
            }
        }

        @Override
        public int size() {
            return this.dirtySize.get();
        }
    }

    private final AtomicReference<InternalDirtyState> _dirtyState;

    private Random random;

    public PressHeapChunkSpace(int maxEntries) {
        this._maxEntries = maxEntries;
        this._lru = new FixedHeapFIFO(maxEntries);
        this.random = new Random();
        this._collisions = new AtomicInteger(0);

        this._dirtyState = new AtomicReference<InternalDirtyState>();
        this._dirtyState.set(new InternalDirtyState(this._maxEntries, this));

        //init std variables
        this.elementK3a = new long[maxEntries];
        this.elementK3b = new long[maxEntries];
        this.elementK3c = new long[maxEntries];

        this.elementNext = new int[maxEntries];
        this.elementHashLock = new AtomicIntegerArray(new int[maxEntries]);
        this.elementHash = new int[maxEntries];
        this._values = new KChunk[maxEntries];
        this._elementCount = new AtomicInteger(0);

        //init internal structures
        for (int i = 0; i < maxEntries; i++) {
            this.elementNext[i] = -1;
            this.elementHash[i] = -1;
            this.elementHashLock.set(i, -1);
        }
    }

    @Override
    public void setManager(KDataManager dataManager) {
        this._manager = (KInternalDataManager) dataManager;
    }

    @Override
    public final KChunk get(long universe, long time, long obj) {
        if (this._elementCount.get() == 0) {
            return null;
        }
        // int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % this._maxEntries;
        int index = (PrimitiveHelper.tripleHash(universe, time, obj) & 0x7FFFFFFF) % this._maxEntries;
        int m = this.elementHash[index];
        while (m != -1) {
            if (universe == this.elementK3a[m] && time == this.elementK3b[m] && obj == elementK3c[m]) {
                //GET VALUE
                return this._values[m];
            } else {
                m = this.elementNext[m];
            }
        }
        return null;
    }

    @Override
    public KChunk create(long universe, long time, long obj, short type, KMetaModel metaModel) {
        KChunk newElement = internal_createElement(universe, time, obj, type);
        return internal_put(universe, time, obj, newElement, metaModel);
    }

    @Override
    public KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel) {
        return (KObjectChunk) internal_put(newUniverse, newTime, newObj, previousElement.clone(newUniverse, newTime, newObj, metaModel), metaModel);
    }

    private KChunk internal_createElement(long p_universe, long p_time, long p_obj, short type) {
        switch (type) {
            case KChunkTypes.OBJECT_CHUNK:
                return new HeapObjectChunk(p_universe, p_time, p_obj, this);
            case KChunkTypes.LONG_LONG_MAP:
                return new ArrayLongLongMap(p_universe, p_time, p_obj, this);
            case KChunkTypes.LONG_TREE:
                return new ArrayLongTree(p_universe, p_time, p_obj, this);
            case KChunkTypes.OBJECT_CHUNK_INDEX:
                return new HeapObjectIndexChunk(p_universe, p_time, p_obj, this);
            default:
                return null;
        }
    }

    private synchronized KChunk internal_put(long universe, long time, long p_obj, KChunk payload, KMetaModel metaModel) {
        KChunk result;
        int entry;
        int index;
        int hash = PrimitiveHelper.tripleHash(universe, time, p_obj);//  (int) (universe ^ time ^ p_obj);
        index = (hash & 0x7FFFFFFF) % this._maxEntries;
        entry = findNonNullKeyEntry(universe, time, p_obj, index);
        if (entry == -1) {
            //we look for nextIndex
            int nbTry = 0;
            int currentVictimIndex = this._lru.dequeue();
            while (this._values[currentVictimIndex] != null && this._values[currentVictimIndex].counter() > 0 /*&& nbTry < this._maxEntries*/) {
                this._lru.enqueue(currentVictimIndex);
                currentVictimIndex = this._lru.dequeue();
                nbTry++;
                if (nbTry % (this._maxEntries / 10) == 0) {
                    System.gc();
                    //System.err.println("GC "+nbTry);
                }
            }

            if (nbTry == this._maxEntries) {
                throw new RuntimeException("Press Cache is Full, too many object are reserved!");
            }

            if (this._values[currentVictimIndex] != null) {

                KChunk victim = this._values[currentVictimIndex];
                long victimUniverse = victim.universe();
                long victimTime = victim.time();
                long victimObj = victim.obj();

                /*
                if (this._values[currentVictimIndex].counter() != 0) {
                    System.err.println(victimUniverse + "," + victimTime + "," + victimObj + "=>" + this._values[currentVictimIndex].counter());
                }*/

                //int hashVictim = (int) (victimUniverse ^ victimTime ^ victimObj);
                int hashVictim = PrimitiveHelper.tripleHash(victimUniverse, victimTime, victimObj);

                //XOR three keys and hash according to maxEntries
                int indexVictim = (hashVictim & 0x7FFFFFFF) % this._maxEntries;
                int previousMagic;
                do {
                    previousMagic = random.nextInt();
                } while (!this.elementHashLock.compareAndSet(indexVictim, -1, previousMagic));
                //we obtains the token, now remove the element
                int m = elementHash[indexVictim];
                int last = -1;
                while (m >= 0) {
                    if (victimUniverse == elementK3a[m] && victimTime == elementK3b[m] && victimObj == elementK3c[m]) {
                        break;
                    }
                    last = m;
                    m = elementNext[m];
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == -1) {
                    int previousNext = elementNext[m];
                    elementHash[indexVictim] = previousNext;
                } else {
                    elementNext[last] = elementNext[m];
                }
                elementNext[m] = -1;//flag to dropped value

                //UNREF victim value object
                _values[currentVictimIndex] = null;

                //free the lock
                this.elementHashLock.compareAndSet(indexVictim, previousMagic, -1);
                this._elementCount.decrementAndGet();

                //TEST IF VICTIM IS DIRTY
                if ((victim.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT) {
                    //SAVE VICTIM
                    saveChunk(victim, metaModel, new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            //free victim from memory
                            victim.free(metaModel);
                        }
                    });
                } else {
                    //FREE VICTIM FROM MEMORY
                    victim.free(metaModel);
                }
            }
            elementK3a[currentVictimIndex] = universe;
            elementK3b[currentVictimIndex] = time;
            elementK3c[currentVictimIndex] = p_obj;
            _values[currentVictimIndex] = payload;

            int previousMagic;
            do {
                previousMagic = random.nextInt();
            } while (!this.elementHashLock.compareAndSet(index, -1, previousMagic));

            if (elementHash[index] != -1) {
                this._collisions.incrementAndGet();
            }

            elementNext[currentVictimIndex] = elementHash[index];
            elementHash[index] = currentVictimIndex;
            result = payload;
            //free the lock
            this.elementHashLock.compareAndSet(index, previousMagic, -1);
            this._elementCount.incrementAndGet();
            //reEnqueue
            this._lru.enqueue(currentVictimIndex);
        } else {
            result = _values[entry];
        }
        return result;
    }

    private int findNonNullKeyEntry(long universe, long time, long obj, int index) {
        int m = this.elementHash[index];
        while (m >= 0) {
            if (universe == this.elementK3a[m] && time == this.elementK3b[m] && obj == this.elementK3c[m]) {
                return m;
            }
            m = this.elementNext[m];
        }
        return -1;
    }

    @Override
    public KChunkIterator detachDirties() {
        return _dirtyState.getAndSet(new InternalDirtyState(this._maxEntries, this));
    }

    @Override
    public void remove(long universe, long time, long obj, KMetaModel p_metaModel) {
        //NOOP, external remove is not allowed in press mode
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {
        long universe = dirtyChunk.universe();
        long time = dirtyChunk.time();
        long obj = dirtyChunk.obj();
        int hash = PrimitiveHelper.tripleHash(universe , time , obj);
        int index = (hash & 0x7FFFFFFF) % this._maxEntries;
        int entry = findNonNullKeyEntry(universe, time, obj, index);
        if (entry != -1) {
            this._dirtyState.get().declareDirty(entry);
        }
    }

    @Override
    public final void clear(KMetaModel metaModel) {
        //TODO
    }

    @Override
    public void free(KMetaModel metaModel) {
        //TODO
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
    public void printDebug(KMetaModel p_metaModel) {
        System.out.println(internal_toString(p_metaModel));
    }

    @Override
    public String toString() {
        return internal_toString(null);
    }

    private String internal_toString(KMetaModel p_metaModel) {
        StringBuilder buffer = new StringBuilder();
        try {
            for (int i = 0; i < this._values.length; i++) {
                KChunk loopChunk = this._values[i];
                if (loopChunk != null) {
                    String content;
                    if (p_metaModel != null) {
                        content = loopChunk.serialize(p_metaModel);
                    } else {
                        content = "no model";
                    }
                    buffer.append(i + "#:" + this.elementK3a[i] + "," + this.elementK3b[i] + "," + this.elementK3c[i] + "=>" + loopChunk.type() + "(count:" + loopChunk.counter() + ",flag:" + loopChunk.getFlags() + ")" + "==>" + content + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    @Override
    public final int size() {
        return this._elementCount.get();
    }

}



