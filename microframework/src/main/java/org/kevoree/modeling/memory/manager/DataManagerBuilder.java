package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.PhantomQueueChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.press.PressHeapChunkSpace;
import org.kevoree.modeling.memory.manager.impl.DataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.scheduler.impl.AsyncScheduler;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;

public class DataManagerBuilder {

    private KContentDeliveryDriver _driver;

    private KScheduler _scheduler;

    private KBlas _blas;

    private KChunkSpace _space;

    private KChunkSpaceManager _spaceManager;

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
            this._scheduler = new AsyncScheduler();
        }
        return _scheduler;
    }

    public KChunkSpace space() {
        if (this._space == null) {
            this._space = new PressHeapChunkSpace(100000);
        }
        return _space;
    }

    /**
     * @native ts
     * if (this._spaceManager == null) { this._spaceManager = new org.kevoree.modeling.memory.space.impl.ManualChunkSpaceManager(); }
     * return this._spaceManager;
     */
    public KChunkSpaceManager spaceManager() {
        if (this._spaceManager == null) {
            this._spaceManager = new PhantomQueueChunkSpaceManager();
        }
        return _spaceManager;
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

    public DataManagerBuilder withSpace(KChunkSpace p_space) {
        this._space = p_space;
        return this;
    }

    public DataManagerBuilder withSpaceManager(KChunkSpaceManager p_spaceManager) {
        this._spaceManager = p_spaceManager;
        return this;
    }

    public DataManagerBuilder withBlas(KBlas p_blas) {
        this._blas = p_blas;
        return this;
    }

    public KInternalDataManager build() {
        return new DataManager(driver(), scheduler(), space(), spaceManager(), blas());
    }

    public static KInternalDataManager buildDefault() {
        return create().build();
    }


}
