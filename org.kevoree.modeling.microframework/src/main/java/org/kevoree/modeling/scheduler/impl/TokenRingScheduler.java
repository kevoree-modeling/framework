package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ignore ts
 */
public class TokenRingScheduler implements KScheduler, Runnable {

    final LockFreeBoundedQueue tasks = new LockFreeBoundedQueue(10000);
    final ConcurrentLinkedQueue<Runnable> slow_tasks = new ConcurrentLinkedQueue<Runnable>();
    final double slow_priority = 0.25;


    @Override
    public void dispatch(Runnable task) {
        boolean result = tasks.offer(task);
        if (!result) {
            slow_tasks.add(task);
        }
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

    private Random random = new Random();

    public TokenRingScheduler workers(int p_w) {
        this._nbWorker = p_w;
        return this;
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Runnable toExecuteTask = null;
                if (!slow_tasks.isEmpty()) {
                    if (random.nextDouble() <= slow_priority) {
                        toExecuteTask = slow_tasks.poll();
                    }
                }
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
                        Thread.sleep(10 * _nbWorker);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class LockFreeBoundedQueue {

        private Runnable[] values;
        private AtomicLong tailPointer = new AtomicLong();
        private AtomicLong headPointer = new AtomicLong();

        public LockFreeBoundedQueue(int size) {
            values = new Runnable[size];
        }

        public boolean offer(Runnable e) {
            long curTail = tailPointer.get();
            long diff = curTail - values.length;
            if (headPointer.get() <= diff) {
                return false;
            }
            values[(int) (curTail % values.length)] = e;
            while (tailPointer.compareAndSet(curTail, curTail + 1)) ;
            return true;
        }

        public Runnable poll() {
            long curHead = headPointer.get();
            if (curHead >= tailPointer.get())
                return null;
            if (!headPointer.compareAndSet(curHead, curHead + 1)) {
                return null;
            }
            int index = (int) curHead % values.length;
            Runnable value = values[index];
            values[index] = null;
            return value;
        }
    }

}
