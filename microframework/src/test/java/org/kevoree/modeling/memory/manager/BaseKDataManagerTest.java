package org.kevoree.modeling.memory.manager;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class BaseKDataManagerTest {

    @Test
    public void temporalResolutionTest() {
        KMetaModel metaModel = new MetaModel("Test");
        KMetaClass metaClassSensor = metaModel.addMetaClass("Sensor");
        metaClassSensor.addAttribute("name", KPrimitiveTypes.STRING);
        metaClassSensor.addAttribute("value", KPrimitiveTypes.DOUBLE);
        metaClassSensor.setTemporalResolution(10);
        KModel model = metaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject origin = model.createByName("Sensor", 0, 0);
                origin.setByName("name", "Sensor#1");


                KDefer defer = model.defer();
                for (int i = 0; i < 100; i++) {
                    origin.jump(i,defer.waitResult());
                }
                defer.then(new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        for (int i = 0; i < 100; i++) {
                            ((KObject)objects[i]).setByName("value", i);
                        }
                        origin.allTimes(new KCallback<long[]>() {
                            @Override
                            public void on(long[] times) {
                                Assert.assertEquals(times.length, 10);
                                origin.manager().lookupAllTimes(origin.universe(), times, origin.uuid(), new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] kObjects) {
                                        for (int i = 0; i < kObjects.length; i++) {
                                            long lastVal = times[i] + 9;
                                            Assert.assertEquals("{\"universe\":0,\"time\":" + times[i] + ",\"uuid\":1,\"data\":{\"name\":\"Sensor#1\",\"value\":" + (double) lastVal + "}}", kObjects[i].toJSON());
                                        }
                                    }
                                });
                        /*
                        origin.manager().lookupAllObjectsTimes(origin.universe(), times, new long[]{origin.uuid()}, new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] kObjects) {
                                for (int i = 0; i < kObjects.length; i++) {
                                    long lastVal = times[i] + 9;
                                    Assert.assertEquals("{\"universe\":0,\"time\":" + times[i] + ",\"uuid\":1,\"data\":{\"name\":\"Sensor#1\",\"value\":" + (double) lastVal + "}}", kObjects[i].toJSON());
                                }
                            }
                        });*/
                            }
                        });


                    }
                });


                /*
                for (int i = 0; i < 100; i++) {
                    final int finalI = i;
                    origin.jump(i, new KCallback<KObject>() {
                        @Override
                        public void on(KObject timedOrigin) {
                            timedOrigin.setByName("value", finalI);
                        }
                    });
                }*/


            }
        });
    }

}
