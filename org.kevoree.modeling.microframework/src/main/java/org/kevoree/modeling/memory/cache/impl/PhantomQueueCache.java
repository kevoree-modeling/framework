package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.meta.KMetaModel;

public class PhantomQueueCache implements KCache {

    private KMemoryStorage _storage;

    public PhantomQueueCache(KMemoryStorage p_storage) {
        this._storage = p_storage;
    }

    @Override
    public KMemoryElement getAndMark(long universe, long time, long obj) {
        KMemoryElement resolvedElement = _storage.get(universe, time, obj);
        if (resolvedElement != null) {
            resolvedElement.inc();
        }
        return resolvedElement;
    }

    @Override
    public void unmark(long universe, long time, long obj) {
        KMemoryElement resolvedElement = _storage.get(universe, time, obj);
        if (resolvedElement != null) {
            resolvedElement.dec();
        }
    }

    @Override
    public KMemoryElement unsafeGet(long universe, long time, long obj) {
        return _storage.get(universe, time, obj);
    }

    @Override
    public KMemoryElement createAndMark(long universe, long time, long obj, short type) {
        KMemoryElement newCreatedElement = _storage.create(universe, time, obj, type);
        if (newCreatedElement != null) {
            newCreatedElement.inc();
        }
        return newCreatedElement;
    }

    @Override
    public void unMarkMemoryElement(KMemoryElement element) {
        element.dec();
    }

    @Override
    public KMemoryChunk cloneMarkAndUnmark(KMemoryChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel) {
        KMemoryChunk newCreatedElement = _storage.clone(previous, newUniverse, newTime, obj, metaModel);
        newCreatedElement.inc();
        previous.dec();
        return newCreatedElement;
    }


/*
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
                //processValues current
                if (current.get() == null) {
                    //check is dirty
                    HeapMemoryChunk currentEntry = (HeapMemoryChunk) this.get(current.universe, current.time, current.uuid);
                    if (currentEntry == null || !currentEntry.isDirty()) {
                        //call the clean sub processValues for universe/time/uuid
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
            //now we try to compact if deleted elements
            compact();
        }
    }*/


}
