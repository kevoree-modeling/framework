package org.kevoree.modeling.scheduler;

import java.util.concurrent.atomic.AtomicReference;

public abstract class KTask implements Runnable {

    public final AtomicReference<KTask> next = new AtomicReference<KTask>();
    
}
