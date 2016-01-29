package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.KTask;

public class DirectScheduler implements KScheduler {

    @Override
    public void dispatch(KTask task) {
        task.run();
    }

    @Override
    public void start() {
        //NOOP
    }

    @Override
    public void stop() {
        //NOOP
    }

    @Override
    public void detach() {
        //NOOP
    }

}
