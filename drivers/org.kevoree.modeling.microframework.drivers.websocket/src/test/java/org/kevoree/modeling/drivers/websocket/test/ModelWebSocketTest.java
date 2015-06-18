package org.kevoree.modeling.drivers.websocket.test;


import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.drivers.websocket.WebSocketCDNClient;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ModelWebSocketTest {

    private int PORT = 6000;

    @Test
    public void test() {
        KMetaModel dynamicMM = new MetaModel("mock");
        KMetaClass dynamicSensorClass = dynamicMM.addMetaClass("sensor");
        dynamicSensorClass.addAttribute("name", KPrimitiveTypes.STRING);
        dynamicSensorClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);

        KModel model = dynamicMM.model();

        //expose it to web
        WebSocketGateway wrapper = WebSocketGateway.exposeModel(model, PORT);
        wrapper.start();

        CountDownLatch latch = new CountDownLatch(1);


        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject sensor = model.create(dynamicSensorClass, 0, 0);
                sensor.set(dynamicSensorClass.attribute("name"), "MyName");
                sensor.set(dynamicSensorClass.attribute("value"), "42.42");
                long sensorUUID = sensor.uuid();

                model.save(new KCallback() {
                    @Override
                    public void on(Object o) {
                        //ok lets start a second VIRTUAL model connected through WebSocket
                        WebSocketCDNClient client = new WebSocketCDNClient("ws://localhost:" + PORT);
                        KModel modelClient = dynamicMM.model();
                        modelClient.setContentDeliveryDriver(client);
                        modelClient.connect(new KCallback() {
                            @Override
                            public void on(Object o) {
                                modelClient.lookup(0, 0, sensorUUID, new KCallback<KObject>() {
                                    @Override
                                    public void on(KObject kObject) {
                                        latch.countDown();
                                        Assert.assertEquals(kObject,sensor);
                                        Assert.assertEquals("{\"universe\":0,\"time\":0,\"uuid\":1,\"data\":{\"name\":\"MyName\",\"value\":[0.0,1.0,0.0,0.0,42.42]}}",sensor.toJSON());
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });




        try {
            latch.await(4000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(latch.getCount(), 0);

        wrapper.stop();

    }

}
