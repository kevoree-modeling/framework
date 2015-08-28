package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.memory.manager.impl.DataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.strategy.impl.HeapMemoryStrategy;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;
import org.kevoree.modeling.scheduler.impl.ExecutorServiceScheduler;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;

public class DataManagerBuilder {

    private KContentDeliveryDriver _driver;

    private KScheduler _scheduler;

    private KMemoryStrategy _strategy;

    private KBlas _blas;

    public KContentDeliveryDriver driver() {
        if (this._driver == null) {
            this._driver = new MemoryContentDeliveryDriver();
        }
        return _driver;
    }

    public KBlas blas() {
        if (this._blas == null) {
            this._blas = new JavaBlas();
        }
        return _blas;
    }

    /**
     * @native ts
     * if (this._scheduler == null) { this._scheduler = new org.kevoree.modeling.scheduler.impl.DirectScheduler(); }
     * return this._scheduler;
     */
    public KScheduler scheduler() {
        if (this._scheduler == null) {
            //this._scheduler = new DirectScheduler();
            this._scheduler = new ExecutorServiceScheduler();
        }
        return _scheduler;
    }

    public KMemoryStrategy strategy() {
        if (this._strategy == null) {
            this._strategy = new HeapMemoryStrategy();
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

    public DataManagerBuilder withBlas(KBlas p_blas) {
        this._blas = p_blas;
        return this;
    }

    public KInternalDataManager build() {
        return new DataManager(driver(), scheduler(), strategy(), blas());
    }

    public static KInternalDataManager buildDefault() {
        return create().build();
    }

}
