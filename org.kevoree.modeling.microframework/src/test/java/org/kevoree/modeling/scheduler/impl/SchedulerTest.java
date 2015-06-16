package org.kevoree.modeling.scheduler.impl;

import org.kevoree.modeling.scheduler.BaseKSchedulerTest;
import org.kevoree.modeling.scheduler.KScheduler;

/**
 * Created by duke on 23/01/15.
 */
public class SchedulerTest extends BaseKSchedulerTest {


    @Override
    public KScheduler createScheduler() {
        return new ExecutorServiceScheduler();
    }
}
