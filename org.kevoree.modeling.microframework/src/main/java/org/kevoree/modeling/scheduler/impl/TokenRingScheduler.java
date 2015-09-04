package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @ignore ts
 */
public class TokenRingScheduler implements KScheduler, Runnable {

    LockFreeBoundedQueue tasks = new LockFreeBoundedQueue(10000);
    private Thread worker;

    @Override
    public void dispatch(Runnable task) {
        boolean result = tasks.offer(task);
        if (!result) {
            System.err.println("SchedulerError loosing task...");
        }
    }

    @Override
    public synchronized void start() {
        worker = new Thread(this);
        worker.setDaemon(false);
        isAlive = true;
        worker.start();
    }

    @Override
    public synchronized void stop() {
        isAlive = false;
    }

    private boolean isAlive = false;

    @Override
    public void run() {
        while (isAlive) {
            try {
                Runnable toExecuteTask = tasks.poll();
                if (toExecuteTask != null) {
                    try {
                        toExecuteTask.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(50);
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
