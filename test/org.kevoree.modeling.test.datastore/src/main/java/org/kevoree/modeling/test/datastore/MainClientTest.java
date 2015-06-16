package org.kevoree.modeling.test.datastore;

import geometry.GeometryModel;
import org.kevoree.modeling.api.KEventListener;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.api.meta.Meta;
import org.kevoree.modeling.databases.websocket.WebSocketWrapper;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Created by gregory.nain on 10/11/14.
 */
public class MainClientTest {


    public static void main(String[] args) {

        Semaphore s = new Semaphore(0);

        GeometryModel geoModel = new GeometryModel();
        geoModel.setContentDeliveryDriver(new WebSocketWrapper(geoModel.manager().cdn(), 23665));


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
            }
        }));

    }
}
