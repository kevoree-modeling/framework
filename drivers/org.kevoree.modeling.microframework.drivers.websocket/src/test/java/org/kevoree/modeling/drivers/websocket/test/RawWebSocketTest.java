package org.kevoree.modeling.drivers.websocket.test;


import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.cdn.impl.ContentPutRequest;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.drivers.websocket.WebSocketContentDeliveryDriverClient;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RawWebSocketTest {

    private int PORT = 6000;

    @Test
    public void test() {
        KContentDeliveryDriverMock mock = new KContentDeliveryDriverMock();
        KMetaModel dynamicMM = new MetaModel("mock");
        KModel model = dynamicMM.model();


        KContentKey[] getRequest = new KContentKey[3];
        getRequest[0] = KContentKey.createGlobalUniverseTree();
        getRequest[1] = KContentKey.createLastPrefix();
        getRequest[2] = KContentKey.createRootUniverseTree();

        ContentPutRequest putRequest = new ContentPutRequest(2);
        putRequest.put(KContentKey.createGlobalUniverseTree(), "GlobalUniverseTree");

        Events eventsMessage = new Events(1);
        int[] meta = new int[1];
        meta[0] = 42;
        eventsMessage.setEvent(0, KContentKey.createGlobalUniverseTree(), meta);

        model.setContentDeliveryDriver(mock);
        WebSocketGateway wrapper = WebSocketGateway.exposeModel(model,PORT);
        wrapper.start();

        CountDownLatch latch = new CountDownLatch(3);

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                WebSocketContentDeliveryDriverClient client = new WebSocketContentDeliveryDriverClient("ws://localhost:" + PORT);
                client.connect(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {

                        client.get(getRequest, new KCallback<String[]>() {
                            @Override
                            public void on(String[] resultPayloads) {
                                latch.countDown();
                                Assert.assertEquals(resultPayloads.length, getRequest.length);
                                for (int i = 0; i < resultPayloads.length; i++) {
                                    Assert.assertEquals(resultPayloads[i], getRequest[i].toString());
                                }
                            }
                        });

                        client.put(putRequest, new KCallback<Throwable>() {
                            @Override
                            public void on(Throwable throwable) {
                                latch.countDown();
                                Assert.assertEquals(mock.alreadyPut.size(), putRequest.size());
                                for (int i = 0; i < putRequest.size(); i++) {
                                    Assert.assertEquals(putRequest.getContent(i), mock.alreadyPut.get(putRequest.getKey(i).toString()));
                                }
                            }
                        });
                        client.atomicGetIncrement(KContentKey.createGlobalUniverseTree(), new KCallback<Short>() {
                            @Override
                            public void on(Short s) {
                                latch.countDown();
                                Assert.assertTrue(s == 0);
                            }
                        });
                        client.send(eventsMessage);
                    }
                });
            }
        });


        try {
            mock.msgCounter.await(4000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            latch.await(4000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(latch.getCount(), 0);

        Assert.assertEquals(mock.msgCounter.getCount(), 0);

    }

}
