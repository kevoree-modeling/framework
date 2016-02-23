package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingAsyncScheduler implements KScheduler {

    private ArrayBlockingQueue<KTask> tasks;
    private WorkerThread[] workers;
    private ThreadGroup tg;
    private AtomicInteger running = new AtomicInteger(0);
    private int _nbWorker = 1;

    public BlockingAsyncScheduler() {
        tasks = new ArrayBlockingQueue<KTask>(100);
    }

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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void dispatch(KTask task) {
        //tasks.offer(task);
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /*
        if (running.get() == _nbWorker) {
            //deadlock potentially detected detach one thread randomly
            renewPool();
        }*/
    }

    private synchronized void renewPool() {
        if (running.get() == _nbWorker) {
            for (int i = 0; i < _nbWorker; i++) {
                workers[i].isAlive = false;
                workers[i] = new WorkerThread(tg, "KMF_Worker_Thread_" + i);
                workers[i].setDaemon(false);
                workers[i].start();
            }
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
        for (int i = 0; i < _nbWorker; i++) {
            WorkerThread workerThread = workers[i];
            workerThread.isAlive = false;
        }
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

    public BlockingAsyncScheduler workers(int p_w) {
        this._nbWorker = p_w;
        tasks = new ArrayBlockingQueue<KTask>(p_w);
        return this;
    }

}
