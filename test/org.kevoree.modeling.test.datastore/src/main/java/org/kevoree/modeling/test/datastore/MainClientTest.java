package org.kevoree.modeling.test.datastore;

import geometry.GeometryModel;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Created by gregory.nain on 10/11/14.
 */
public class MainClientTest {


    public static void main(String[] args) {

        Semaphore s = new Semaphore(0);

        GeometryModel geoModel = new GeometryModel();

        WebSocketGateway.exposeModel(geoModel, 23665).start();



        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
            }
        }));

    }
}
