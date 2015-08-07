package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.KChunk;
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

    /* This is the very first GC collector for KMF, thanks Floreal :-) */

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

                //TODO delegate to resolver this management
                KChunk resolvedChunk = _space.get(kobj.previousResolved[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], kobj.previousResolved[AbstractKObject.TIME_PREVIOUS_INDEX], kobj.obj);
                KChunk resolvedTimeTree = _space.get(kobj.previousResolved[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], KConfig.NULL_LONG, kobj.obj);
                KChunk resolvedUniverseTree = _space.get(KConfig.NULL_LONG, KConfig.NULL_LONG, kobj.obj);
                KChunk resolvedGlobalTree = _space.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);

                if (resolvedChunk != null) {
                    unmarkMemoryElement(resolvedChunk);
                    if (resolvedChunk.counter() == 0) {
                        _space.remove(resolvedChunk.universe(), resolvedChunk.time(), resolvedChunk.obj(), _metaModel);
                    }
                }
                if (resolvedTimeTree != null) {
                    unmarkMemoryElement(resolvedTimeTree);
                    if (resolvedTimeTree.counter() == 0) {
                        _space.remove(resolvedTimeTree.universe(), resolvedTimeTree.time(), resolvedTimeTree.obj(), _metaModel);
                    }
                }
                if (resolvedUniverseTree != null) {
                    unmarkMemoryElement(resolvedUniverseTree);
                    if (resolvedUniverseTree.counter() == 0) {
                        _space.remove(resolvedUniverseTree.universe(), resolvedUniverseTree.time(), resolvedUniverseTree.obj(), _metaModel);
                    }
                }
                if (resolvedGlobalTree != null) {
                    unmarkMemoryElement(resolvedGlobalTree);
                    if (resolvedGlobalTree.counter() == 0) {
                        _space.remove(resolvedGlobalTree.universe(), resolvedGlobalTree.time(), resolvedGlobalTree.obj(), _metaModel);
                    }
                }
            }
        }
    }

    class KObjectPhantomReference extends PhantomReference<KObject> {

        public long obj;
        public long[] previousResolved;
        private KObjectPhantomReference next;
        private KObjectPhantomReference previous;

        public KObjectPhantomReference(KObject referent) {
            super(referent, referenceQueue);
            this.obj = referent.uuid();
            previousResolved = ((AbstractKObject) referent).previousResolved();
        }
    }

}
