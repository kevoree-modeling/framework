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
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.KTask;
import org.kevoree.modeling.scheduler.impl.LockFreeScheduler;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

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

    /*
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CountDownLatch latch = new CountDownLatch(10000);
        long counter = latch.getCount();
        for(int i=0;i<counter;i++){
            service.submit(new Runnable() {
                @Override
                public void run() {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.err.println("End");

        service.shutdown();
    }*/


    /**
     * @native ts
     */
    @Test
    public void polyTest() {

        CountDownLatch latch = new CountDownLatch(1);

        //final KDataManager manager = createDataManager();
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        final KMetaAttribute sensorMetaValue = sensorMetaClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);
        final KMetaAttribute sensorMetaValue2 = sensorMetaClass.addAttribute("value2", KPrimitiveTypes.DOUBLE);
        //   final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.create().withScheduler(new LockFreeScheduler()).build());
        model.connect(new KCallback<Throwable>() {

            @Override
            public void on(Throwable throwable) {
                KObject sensor = model.create(sensorMetaClass, 0, 0);
                long uuid = sensor.uuid();
                sensor = null;
                KDefer defer = model.defer();
                Random random = new Random();
                for (int i = 0; i < 1000; i++) {
                    final KCallback waiter = defer.waitResult();
                    model.lookup(0, i, uuid, new KCallback<KObject>() {
                        @Override
                        public void on(final KObject jumpedSensor) {
                            try {
                                jumpedSensor.setByName("value2", random.nextDouble());
                                //jumpedSensor.setByName("value", random.nextDouble());
                                if (jumpedSensor.now() % 100 == 0) {
                                    model.save(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                waiter.on(null);
                            }
                        }
                    });
                }

                System.gc();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                defer.then(new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        ((KInternalDataManager) model.manager()).scheduler().dispatch(new KTask() {
                            @Override
                            public void run() {
                                //((KInternalDataManager) model.manager()).printDebug();
                                latch.countDown();
                            }
                        });


/*
                        model.save(new KCallback() {
                            @Override
                            public void on(Object o) {

                                System.gc();

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                System.out.println("endSize>" + ((KInternalDataManager) model.manager()).spaceSize());
                                ((KInternalDataManager) model.manager()).printDebug();
                            }
                        });*/

                    }
                });

                /*
                Assert.assertEquals(4, ((K InternalDataManager) manager).spaceSize());

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

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
