package org.kevoree.modeling.memory.cache.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;

/**
 * Created by duke on 27/03/15.
 */
public class KCacheCleanerTest {

    /**
     * @native ts
     */
    @Test
    public void test() {
        KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        final KModel universe = dynamicMetaModel.model();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                long sensorID = sensor.uuid();
                sensor = null;
                universe.save(null);
                System.gc();
                universe.manager().cleanCache();
                Assert.assertEquals(1, universe.manager().cache().size());
                universe.universe(0).time(0).lookup(sensorID, new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        Assert.assertNotNull(kObject);
                        Assert.assertEquals(4, universe.manager().cache().size());

                        kObject.jump(10, new KCallback<KObject>() {
                            @Override
                            public void on(KObject kObject2) {
                                Assert.assertNotNull(kObject2);
                                Assert.assertEquals(4, universe.manager().cache().size());
                            }
                        });
                    }
                });
            }
        });
    }

}
