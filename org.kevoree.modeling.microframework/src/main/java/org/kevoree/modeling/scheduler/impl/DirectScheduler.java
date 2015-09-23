package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

public class DirectScheduler implements KScheduler {

    /**
     * @native ts
     * setTimeout(task,0);
     */
    @Override
    public void dispatch(KTask task) {
        task.run();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
