package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ignore ts
 */
public class PhantomQueueChunkSpaceManager extends AbstractCountingChunkSpaceManager implements Runnable {

    private final ReferenceQueue<KObject> referenceQueue;
    private final AtomicReference<KObjectPhantomReference> headPhantom;
    private KMetaModel _metaModel;

    public PhantomQueueChunkSpaceManager(KChunkSpace p_storage) {
        super(p_storage);
        headPhantom = new AtomicReference<KObjectPhantomReference>();
        referenceQueue = new ReferenceQueue<KObject>();
        Thread cleanupThread = new Thread(this);
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    public void register(KObject kobj) {
        if (_metaModel == null) {
            _metaModel = kobj.manager().model().metaModel();
        }
        KObjectPhantomReference newRef = new KObjectPhantomReference(kobj);
        do {
            newRef.next = headPhantom.get();
        } while (!headPhantom.compareAndSet(newRef.next, newRef));
        if (newRef.next != null) {
            newRef.next.previous = newRef;
        }
    }

    @Override
    public void registerAll(KObject[] kobjects) {
        for (int i = 0; i < kobjects.length; i++) {
            if (kobjects[i] != null) {
                register(kobjects[i]);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            KObjectPhantomReference kobj = null;
            try {
                kobj = (KObjectPhantomReference) referenceQueue.remove();
                if (kobj.previous == null) {
                    if (!headPhantom.compareAndSet(kobj, kobj.next)) {
                        //ouch should try to remove from the previous
                        KObjectPhantomReference nextRef = kobj.next;
                        KObjectPhantomReference previousRef = kobj.previous;
                        if (nextRef != null) {
                            nextRef.previous = previousRef;
                        }
                        if (previousRef != null) {
                            previousRef.next = nextRef;
                        }
                    }
                } else {
                    KObjectPhantomReference nextRef = kobj.next;
                    KObjectPhantomReference previousRef = kobj.previous;
                    if (nextRef != null) {
                        nextRef.previous = previousRef;
                    }
                    previousRef.next = nextRef;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (kobj != null) {
                System.err.println("Drop " + kobj.universe + "," + kobj.time + "," + kobj.obj);
                _space.remove(kobj.universe, kobj.time, kobj.obj, _metaModel);
            }
        }
    }

    class KObjectPhantomReference extends PhantomReference<KObject> {

        public long universe;
        public long time;
        public long obj;
        private KObjectPhantomReference next;
        private KObjectPhantomReference previous;

        public KObjectPhantomReference(KObject referent) {
            super(referent, referenceQueue);
            this.universe = referent.universe();
            this.time = referent.now();
            this.obj = referent.uuid();
        }
    }

}
