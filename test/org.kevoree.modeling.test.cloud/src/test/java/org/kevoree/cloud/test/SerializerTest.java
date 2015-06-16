package org.kevoree.cloud.test;

import cloud.*;
import org.junit.Test;
import java.util.concurrent.Semaphore;


/**
 * Created by gregory.nain on 16/10/2014.
 */
public class SerializerTest {

    @Test
    public void serializeTest() throws InterruptedException {
        try {
            Semaphore s = new Semaphore(0);

            CloudModel universe = new CloudModel();
            universe.connect(null);
            CloudUniverse dimension0 = universe.newUniverse();
            CloudView t0 = dimension0.time(0l);
            Node nodeT0 = t0.createNode();
            nodeT0.setName("node0");
            t0.setRoot(nodeT0,null);

            Element child0 = t0.createElement();
            nodeT0.setElement(child0);

            Node nodeT1 = t0.createNode();
            nodeT1.setName("n1");
            nodeT0.addChildren(nodeT1);


            t0.lookup(nodeT0.uuid(),(root) -> {
                t0.xmi().save(root,(result) -> {
                    s.release();
                });
            });
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
