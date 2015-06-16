package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.cloudmodel.*;
import org.kevoree.modeling.traversal.visitor.KModelVisitor;
import org.kevoree.modeling.traversal.visitor.KVisitResult;

/**
 * Created by duke on 10/13/14.
 */
public class HelloTest {


    @Test
    public void badLookupTest() {
        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    CloudUniverse universe = model.newUniverse();
                    CloudView time0 = universe.time(0l);
                    Node root = time0.createNode();
                    time0.setRoot(root,null);
                    root.setName("root");
                    Assert.assertEquals("root", root.getName());
                    Node n1 = time0.createNode();
                    n1.setName("n1");
                    Node n2 = time0.createNode();
                    n2.setName("n2");
                    root.addChildren(n1);
                    root.addChildren(n2);

                    time0.lookup(42,new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            Assert.assertNull(kObject);
                        }
                    });

                }
            }
        });
    }

    @Test
    public void simpleTest() {
        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    CloudUniverse universe = model.newUniverse();
                    CloudView time0 = universe.time(0l);
                    final Node root = time0.createNode();
//                    time0.setRoot(root, null);
                    root.setName("root");
                    Assert.assertEquals("root", root.getName());
                    Node n1 = time0.createNode();
                    n1.setName("n1");
                    Node n2 = time0.createNode();
                    n2.setName("n2");
                    root.addChildren(n1);
                    root.addChildren(n2);
                    time0.lookup(root.uuid(),new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            Assert.assertNotNull(kObject);
                            Assert.assertEquals(kObject, root);
                        }
                    });
                    /*
                    n1.inbounds().then(new Callback<KObject[]>() {
                        @Override
                        public void on(KObject[] kObjects) {
                            Assert.assertNotNull(kObjects[0]);
                            Assert.assertEquals(kObjects[0].uuid(), root.uuid());
                        }
                    });
                    n2.inbounds().then(new Callback<KObject[]>() {
                        @Override
                        public void on(KObject[] kObjects) {
                            Assert.assertEquals(kObjects[0].uuid(), root.uuid());
                        }
                    });
                    */
                }
            }
        });
    }

    @Test
    public void helloTest() {
        CloudModel universe = new CloudModel();
        universe.connect(null);

        CloudUniverse dimension0 = universe.newUniverse();

        Assert.assertNotNull(dimension0);
        CloudView t0 = dimension0.time(0l);

        Assert.assertNotNull(t0);
        Assert.assertEquals(t0.now(), 0l);
        Node nodeT0 = t0.createNode();
        Assert.assertNotNull(nodeT0);
        Assert.assertNotNull(nodeT0.uuid());
        // assertNotNull(nodeT0.path());
        Assert.assertNull(nodeT0.getName());
        //Assert.assertEquals("name=", nodeT0.domainKey());
        nodeT0.setName("node0");
        Assert.assertEquals("node0", nodeT0.getName());
        //Assert.assertEquals("name=node0", nodeT0.domainKey());
        Assert.assertEquals(0l, nodeT0.now());
//        assertNull(nodeT0.parentPath());
        Element child0 = t0.createElement();
        //TODO reInsert following test
        //Assert.assertNotNull(child0.timeTree());
        //Assert.assertTrue(child0.timeTree().last().equals(0l));
        //Assert.assertTrue(child0.timeTree().first().equals(0l));

        Node nodeT1 = t0.createNode();
        nodeT1.setName("n1");

        nodeT0.addChildren(nodeT1);

//        assertTrue(nodeT1.path().endsWith("/children[name=n1]"));

        final int[] i = {0};
        final int[] j = {0};
        nodeT0.getChildren(new KCallback<Node[]>() {
            @Override
            public void on(Node[] n) {
                for (int k = 0; k < n.length; k++) {
                    i[0]++;
                }
            }
        });
        Assert.assertEquals(1, i[0]);
        Node nodeT3 = t0.createNode();
        nodeT3.setName("n3");
        nodeT1.addChildren(nodeT3);


        i[0] = 0;
        j[0] = 0;
        nodeT0.visit(new KModelVisitor() {
            @Override
            public KVisitResult visit(KObject elem) {
                i[0]++;
                return KVisitResult.CONTINUE;
            }
        },new KCallback<Throwable>() {
            @Override
            public void on(Throwable t) {
                j[0]++;
            }
        });
        Assert.assertEquals(2, i[0]);
        Assert.assertEquals(1, j[0]);

        //System.err.println(nodeT0);

    }

}
