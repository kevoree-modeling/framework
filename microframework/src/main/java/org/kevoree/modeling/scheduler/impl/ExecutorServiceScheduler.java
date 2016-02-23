package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceScheduler implements KScheduler {

    private ExecutorService service;

    public ExecutorServiceScheduler() {
        service = Executors.newFixedThreadPool(8);
    }

    @Override
    public void dispatch(final KTask task) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void detach() {

    }

}
