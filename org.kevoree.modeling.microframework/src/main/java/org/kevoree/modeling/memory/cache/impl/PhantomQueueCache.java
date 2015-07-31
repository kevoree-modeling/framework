package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.meta.KMetaModel;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicReference;

public class PhantomQueueCache extends AbstractCountingCache {

    private final ReferenceQueue<KObject> queue = new ReferenceQueue<KObject>();

    private final KDataManager _manager;

    private final AtomicReference<KMemoryElement> dirtyList = new AtomicReference<KMemoryElement>();


    public PhantomQueueCache(KMemoryStorage p_storage, KDataManager p_manager) {
        super(p_storage);
        _manager = p_manager;
    }

    @Override
    public void register(KObject object) {
        new KObjectReference((AbstractKObject)object, queue);
    }

    @Override
    public void registerAll(KObject[] objects) {
        for (int i = 0; i < objects.length; i++) {
            new KObjectReference((AbstractKObject)objects[i], queue);
        }
    }

    @Override
    void collect(KMemoryStorage storage) {
        KObjectReference ref;
        KMetaModel mmodel = _manager.model().metaModel();
        while((ref = (KObjectReference)queue.poll()) != null) {
            long uuid = ref.uuid;
            long[] resolved = ref.resolved;
            long universe = resolved[AbstractKObject.UNIVERSE_PREVIOUS_INDEX];
            long time = resolved[AbstractKObject.TIME_PREVIOUS_INDEX];

            decAndRemove(universe, time, uuid, mmodel);
            decAndRemove(universe, KConfig.NULL_LONG, uuid, mmodel);
            decAndRemove(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid, mmodel);
            decAndRemove(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, mmodel);
        }
        _storage.compact();
    }


    private final void decAndRemove(long universe, long time, long uuid, KMetaModel mmodel) {
        KMemoryElement elt = _storage.get(universe, time, uuid);
        if (elt != null) {
            elt.dec();
            if (elt.counter() == 0) {
                _storage.remove(universe, time, uuid, mmodel);
            }
        }
    }

    static class KObjectReference extends PhantomReference<KObject> {

        private final long uuid;
        private final long[] resolved;

        public KObjectReference(AbstractKObject referent, ReferenceQueue<KObject> queue) {
            super(referent, queue);
            uuid = referent.uuid();
            resolved = referent.previousResolved();
        }
    }
}
