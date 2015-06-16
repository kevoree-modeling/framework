package org.kevoree.modeling.scheduler;

public interface KScheduler {

    void dispatch(Runnable runnable);

    void stop();

}
