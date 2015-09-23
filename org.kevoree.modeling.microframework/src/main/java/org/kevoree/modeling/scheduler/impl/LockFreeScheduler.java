package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ignore ts
 */
public class LockFreeScheduler implements KScheduler, Runnable {

    final LockFreeQueue tasks = new LockFreeQueue();

    @Override
    public void dispatch(KTask task) {
        tasks.offer(task);
    }

    private Thread[] workers;
    private ThreadGroup tg;

    @Override
    public synchronized void start() {
        tg = new ThreadGroup("KMF_TokenRing");
        isAlive = true;
        workers = new Thread[_nbWorker];
        for (int i = 0; i < _nbWorker; i++) {
            workers[i] = new Thread(tg, this, "KMF_TokenRing_Thread_" + i);
            workers[i].setDaemon(false);
            workers[i].start();
        }
    }

    @Override
    public synchronized void stop() {
        isAlive = false;
    }

    private volatile boolean isAlive = false;

    private int _nbWorker = 1;

    public LockFreeScheduler workers(int p_w) {
        this._nbWorker = p_w;
        return this;
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                KTask toExecuteTask = null;
                if (toExecuteTask == null) {
                    toExecuteTask = tasks.poll();
                }
                if (toExecuteTask != null) {
                    try {
                        toExecuteTask.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(20 * _nbWorker);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class LockFreeQueue {
        private final AtomicLong length = new AtomicLong(1L);

        private class Wrapper {
            public KTask ref;
            public AtomicReference<Wrapper> next = new AtomicReference<Wrapper>(null);
        }

        private final Wrapper stub = new Wrapper();
        private final AtomicReference<Wrapper> head = new AtomicReference<Wrapper>(stub);
        private final AtomicReference<Wrapper> tail = new AtomicReference<Wrapper>(stub);

        public void offer(KTask x) {
            Wrapper wrapper = new Wrapper();
            wrapper.ref = x;
            addNode(wrapper);
            length.incrementAndGet();
        }

        public KTask poll() {
            while (true) {
                long l = length.get();
                if (l == 1) {
                    return null;
                }
                if (length.compareAndSet(l, l - 1)) {
                    break;
                }
            }
            while (true) {
                Wrapper r = head.get();
                if (r == null) {
                    throw new IllegalStateException("null head");
                }
                if (r.next.get() == null) {
                    length.incrementAndGet();
                    return null;
                }
                if (head.compareAndSet(r, r.next.get())) {
                    if (r == stub) {
                        stub.next.set(null);
                        addNode(stub);
                    } else {
                        return r.ref;
                    }
                }
            }
        }

        private void addNode(Wrapper n) {
            Wrapper t;
            while (true) {
                t = tail.get();
                if (tail.compareAndSet(t, n)) {
                    break;
                }
            }
            if (t.next.compareAndSet(null, n)) {
                return;
            }
            throw new IllegalStateException("bad tail next");
        }
    }


}
