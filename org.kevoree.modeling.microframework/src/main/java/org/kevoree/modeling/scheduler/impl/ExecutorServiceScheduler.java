package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceScheduler implements KScheduler {

    /**
     * @ignore ts
     */
    private ExecutorService _service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * @native ts
     * p_runnable.run();
     */
    @Override
    public void dispatch(Runnable p_runnable) {
        _service.submit(p_runnable);
    }

    /**
     * @native ts
     */
    @Override
    public void stop() {
        _service.shutdown();
    }

}
