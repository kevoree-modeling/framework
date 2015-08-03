package org.kevoree.modeling.memory.space.impl;

import org.junit.Test;

public class KChunkSpaceCleanerTest {

    /**
     * @native ts
     */
    @Test
    public void test() {

        /*
        KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        final KModel universe = dynamicMetaModel.createModel();
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
        */

    }

}
