package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @native ts
 * dispatch = function(task:org.kevoree.modeling.scheduler.KTask){
 * setTimeout(task,0);
 * }
 * start = function(){
 * //NNOP
 * }
 * stop = function(){
 * //NOOP
 * }
 * run = function(){
 * //NOOP
 * }
 * detach(){
 * console.log('sync operation not implemented in JS yet !!!!');
 * }
 */
public class AsyncScheduler implements KScheduler/*, Runnable*/ {

    final LockFreeQueue tasks = new LockFreeQueue();

    private WorkerThread[] workers;
    private ThreadGroup tg;
    private AtomicInteger running = new AtomicInteger(0);

    /**
     * @ignore ts
     */
    private final class WorkerThread extends Thread {

        public volatile boolean isAlive = true;

        public WorkerThread(ThreadGroup tg, String s) {
            super(tg, s);
        }

        @Override
        public void run() {
            while (isAlive) {
                try {
                    KTask toExecuteTask = tasks.poll();
                    if (toExecuteTask != null) {
                        try {
                            running.incrementAndGet();
                            toExecuteTask.run();
                            running.decrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void dispatch(KTask task) {
        tasks.offer(task);
        if (running.get() == _nbWorker) {
            //deadlock potentially detected detach one thread randomly
            renewPool();
        }
    }

    private synchronized void renewPool() {
        if (running.get() == _nbWorker) {
            for (int i = 0; i < _nbWorker; i++) {
                workers[i].isAlive = false;
                workers[i] = new WorkerThread(tg, "KMF_Worker_Thread_" + i);
                workers[i].setDaemon(false);
                workers[i].start();
            }
            //System.err.println("ReNew Pool");
        }
    }

    @Override
    public synchronized void start() {
        tg = new ThreadGroup("KMF_Worker");
        workers = new WorkerThread[_nbWorker];
        for (int i = 0; i < _nbWorker; i++) {
            workers[i] = new WorkerThread(tg, "KMF_Worker_Thread_" + i);
            workers[i].setDaemon(false);
            workers[i].start();
        }
    }

    @Override
    public synchronized void stop() {
        tg.destroy();
    }

    @Override
    public void detach() {
        Thread current = Thread.currentThread();
        for (int i = 0; i < _nbWorker; i++) {
            if (workers[i].getId() == current.getId()) {
                //inform the previous thread to die at the end of the blocking task
                WorkerThread workerThread = (WorkerThread) current;
                workerThread.isAlive = false;
                running.decrementAndGet();
                //replace the current current by a fresh one
                workers[i] = new WorkerThread(tg, "KMF_Worker_Thread_" + i);
                workers[i].setDaemon(false);
                workers[i].start();
                //exit the function
                return;
            }
        }
    }

    private int _nbWorker = 1;

    public AsyncScheduler workers(int p_w) {
        this._nbWorker = p_w;
        return this;
    }

    /**
     * @ignore ts
     */
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
