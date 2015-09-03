package org.kevoree.modeling.memory.space;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.Random;

public abstract class BaseKChunkSpaceCleanerTest {

    public abstract KDataManager createDataManager();

    /**
     * @native ts
     */
    @Test
    public void test() {
        KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        KDataManager manager = createDataManager();

        final KModel universe = dynamicMetaModel.createModel((KInternalDataManager) manager);
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);

                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());

                long sensorID = sensor.uuid();
                sensor = null;
                System.gc();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //object are dirty cannot be saved
                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                manager.save(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        Assert.assertEquals(0, ((KInternalDataManager) manager).spaceSize());
                        universe.universe(0).time(0).lookup(sensorID, new KCallback<KObject>() {
                            @Override
                            public void on(KObject kObject) {
                                Assert.assertNotNull(kObject);
                                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                                kObject.jump(10, new KCallback<KObject>() {
                                    @Override
                                    public void on(KObject kObject2) {
                                        Assert.assertNotNull(kObject2);
                                        Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                                    }
                                });
                            }
                        });
                    }
                });


            }
        });
    }


    /**
     * @native ts
     */
    //@Test
    public void polyTest() {
        final KDataManager manager = createDataManager();
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        final KMetaAttribute sensorMetaValue = sensorMetaClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);

        System.err.println(">"+sensorMetaValue.index());

        final KModel model = dynamicMetaModel.createModel((KInternalDataManager) manager);
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);

                KDefer defer = model.defer();
                Random random = new Random();
                for (int i = 0; i < 1000000; i++) {
                    final KCallback waiter = defer.waitResult();
                    sensor.jump(i, new KCallback<KObject>() {
                        @Override
                        public void on(KObject jumpedSensor) {
                            jumpedSensor.setByName("value", random.nextDouble());
                            waiter.on(null);
                        }
                    });
                    if (i % 1000 == 0) {
                        model.save(null);
                    }
                }
                defer.then(new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        System.err.println("Hello");
                    }
                });


                /*
                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());

                long sensorID = sensor.uuid();
                sensor = null;
                System.gc();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //object are dirty cannot be saved
                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                manager.save(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        Assert.assertEquals(0, ((KInternalDataManager) manager).spaceSize());
                        model.universe(0).time(0).lookup(sensorID, new KCallback<KObject>() {
                            @Override
                            public void on(KObject kObject) {
                                Assert.assertNotNull(kObject);
                                Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                                kObject.jump(10, new KCallback<KObject>() {
                                    @Override
                                    public void on(KObject kObject2) {
                                        Assert.assertNotNull(kObject2);
                                        Assert.assertEquals(4, ((KInternalDataManager) manager).spaceSize());
                                    }
                                });
                            }
                        });
                    }
                });
                */

            }
        });
    }

}
