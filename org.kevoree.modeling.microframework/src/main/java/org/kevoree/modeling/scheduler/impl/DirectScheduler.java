package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.KScheduler;

public class DirectScheduler implements KScheduler {

    /**
     * @native ts
     * if(runnable['run'] === undefined){setTimeout(runnable,0);} else {setTimeout(runnable.run,0);}
     */
    @Override
    public void dispatch(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
