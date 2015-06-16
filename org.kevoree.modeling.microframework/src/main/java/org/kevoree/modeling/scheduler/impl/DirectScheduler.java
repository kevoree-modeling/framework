package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;

public class DirectScheduler implements KScheduler {

    @Override
    public void dispatch(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void stop() {
    }

}
