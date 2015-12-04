package org.kevoree.modeling.scheduler;

public interface KScheduler {

    void dispatch(KTask task);

    void start();

    void stop();

}
