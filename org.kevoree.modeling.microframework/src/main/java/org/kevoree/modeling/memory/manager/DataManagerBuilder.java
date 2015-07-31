package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.memory.manager.impl.DataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.strategy.impl.HeapPhantomQueueMemoryStrategy;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class DataManagerBuilder {

    private KContentDeliveryDriver _driver;

    private KScheduler _scheduler;

    private KMemoryStrategy _strategy;

    public KContentDeliveryDriver driver() {
        if (this._driver == null) {
            this._driver = new MemoryContentDeliveryDriver();
        }
        return _driver;
    }

    public KScheduler scheduler() {
        if (this._scheduler == null) {
            this._scheduler = new DirectScheduler();
        }
        return _scheduler;
    }

    public KMemoryStrategy strategy() {
        if (this._strategy == null) {
            this._strategy = new HeapPhantomQueueMemoryStrategy();
        }
        return _strategy;
    }

    public static DataManagerBuilder create() {
        return new DataManagerBuilder();
    }

    public DataManagerBuilder withContentDeliveryDriver(KContentDeliveryDriver p_driver) {
        this._driver = p_driver;
        return this;
    }

    public DataManagerBuilder withScheduler(KScheduler p_scheduler) {
        this._scheduler = p_scheduler;
        return this;
    }

    public DataManagerBuilder withMemoryStrategy(KMemoryStrategy p_strategy) {
        this._strategy = p_strategy;
        return this;
    }

    public KInternalDataManager build() {
        return new DataManager(driver(), scheduler(), strategy());
    }

    public static KInternalDataManager buildDefault() {
        return create().build();
    }

}
