package org.kevoree.modeling.traversal;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;

import org.junit.Assert;

/**
 * Created by duke on 10/27/14.
 */
public class BasicSelectTest {


    @Test
    public void rootSelectTest() throws Exception {
        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();
        CloudView t0 = dimension0.time(0l);
        final Node node = t0.createNode();
        node.setName("n0");
        t0.setRoot(node,new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        });
        t0.getRoot(new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                Assert.assertEquals(kObject.uuid(), node.uuid());
                Assert.assertEquals(kObject, node);
            }
        });
        t0.select("/",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                Assert.assertEquals(kObjects[0], node);
            }
        });
        final CloudView t1 = dimension0.time(1l);
        t1.getRoot(new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                Assert.assertEquals(node.uuid(), kObject.uuid());
                Assert.assertEquals(t1.now(), kObject.now());
            }
        });
        t1.select("/",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                Assert.assertEquals(node.uuid(), kObjects[0].uuid());
                Assert.assertEquals(t1.now(), kObjects[0].now());
            }
        });
    }


    @Test
    public void selectTest() throws Exception {
        CloudModel universe = new CloudModel();
        universe.connect(null);

        CloudUniverse dimension0 = universe.newUniverse();
        CloudView t0 = dimension0.time(0l);
        Node node = t0.createNode();
        node.setName("n0");
        t0.setRoot(node,null);
        final Node node2 = t0.createNode();
        node2.setName("n1");
        node.addChildren(node2);

        final Node node3 = t0.createNode();
        node3.setName("n2");
        node2.addChildren(node3);

        Node node4 = t0.createNode();
        node4.setName("n4");
        node3.addChildren(node4);

        Node node5 = t0.createNode();
        node5.setName("n5");
        node3.addChildren(node5);

        t0.select("children[]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node2, selecteds[0]);
            }
        });

        t0.select("children[name=*]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node2, selecteds[0]);
            }
        });

        t0.select("children[name=n*]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node2, selecteds[0]);
            }
        });

        t0.select("children[name=n1]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node2, selecteds[0]);
            }
        });

        t0.select("children[name=!n1]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(0, selecteds.length);
            }
        });

        t0.select("children[name!=n1]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(0, selecteds.length);
            }
        });

        t0.select("children[name=n1]/children[name=n2]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node3, selecteds[0]);
            }
        });

        t0.select("/children[name=n1]/children[name=n2]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node3, selecteds[0]);
            }
        });

        node.select("children[name=n1]/children[name=n2]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(1, selecteds.length);
                Assert.assertEquals(node3, selecteds[0]);
            }
        });

        node.select("children[name=n1]/children[name=n2]/children[name=*]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(2, selecteds.length);
            }
        });

    }


    @Test
    public void selectTest2() throws Exception {
        CloudModel universe = new CloudModel();
        universe.connect(null);

        CloudUniverse dimension0 = universe.newUniverse();
        CloudView t0 = dimension0.time(0l);
        Node n0 = t0.createNode();
        n0.setName("n0");
        n0.setValue("v0");
        t0.setRoot(n0,null);

        final Node n1 = t0.createNode();
        n1.setName("n1");
        n1.setValue("v1");
        n0.addChildren(n1);

        final Node n2 = t0.createNode();
        n2.setName("n2");
        n2.setValue("v2");
        n0.addChildren(n2);

        t0.select("children[value=v2,name=n1]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(selecteds.length,0);
            }
        });

        t0.select("children[value=v2,name=n2]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(selecteds.length,1);
            }
        });

        t0.select("children[name=*]",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] selecteds) {
                Assert.assertEquals(selecteds.length, 2);
            }
        });
    }

}
