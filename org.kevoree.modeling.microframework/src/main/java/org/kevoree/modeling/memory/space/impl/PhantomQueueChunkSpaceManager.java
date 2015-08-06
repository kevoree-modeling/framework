package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.space.KChunkSpace;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class PhantomQueueChunkSpaceManager extends AbstractCountingChunkSpaceManager implements Runnable {

    private final ReferenceQueue<KObject> referenceQueue;

    public PhantomQueueChunkSpaceManager(KChunkSpace p_storage) {
        super(p_storage);
        referenceQueue = new ReferenceQueue<KObject>();
        Thread cleanupThread = new Thread(this);
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    public void register(KObject kobj) {
        new KObjectPhantomReference(kobj);
    }

    @Override
    public void registerAll(KObject[] kobjects) {
        for (int i = 0; i < kobjects.length; i++) {
            if (kobjects[i] != null) {
                new KObjectPhantomReference(kobjects[i]);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            KObjectPhantomReference kobj = null;
            try {
                kobj = (KObjectPhantomReference) referenceQueue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.err.println("ShouldClean" + kobj.obj);
        }
    }

    class KObjectPhantomReference extends PhantomReference<KObject> {

        public long universe;
        public long time;
        public long obj;

        public KObjectPhantomReference(KObject referent) {
            super(referent, referenceQueue);
            this.universe = referent.universe();
            this.time = referent.now();
            this.obj = referent.uuid();
        }
    }

}
