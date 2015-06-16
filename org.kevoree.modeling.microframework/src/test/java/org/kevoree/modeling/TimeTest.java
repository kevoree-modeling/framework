package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.Element;


/**
 * Created by thomas on 10/21/14.
 */
public class TimeTest {

    @Test
    public void timeCreationTest() {

        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();

        Assert.assertNotNull(dimension0);

        // create time0
        final CloudView t0 = dimension0.time(0l);
        Assert.assertNotNull(t0);
        org.junit.Assert.assertEquals(t0.now(), 0l);

        // create time1
        final CloudView t1 = dimension0.time(1l);
        Assert.assertNotNull(t1);
        org.junit.Assert.assertEquals(t1.now(), 1l);
    }


    final int[] jumpCounter = {4};
    private KCallback<KObject> jumpCallback = new KCallback<KObject>() {
        @Override
        public void on(KObject kObject) {
            if (jumpCounter[0] > 0) {
                jumpCounter[0]--;
                if (kObject != null) {
                    Node n = (Node) kObject;
                    n.jump(jumpCounter[0], jumpCallback);
                }
            }
        }
    };

    @Test
    public void jumpTest() {
        CloudModel cloud = new CloudModel();
        cloud.connect(null);
        CloudUniverse universe = cloud.newUniverse();

        //creates an object at time 3
        CloudView view3 = universe.time(3);
        Node n = view3.createNode();
        view3.setRoot(n, null);
        cloud.save(null);

        //resolves the object from time 5
        CloudView view5 = universe.time(5);
        view5.select("/", new KCallback<KObject[]>() {
            public void on(KObject[] kObjects) {
                Node n = (Node) kObjects[0];
                n.jump(jumpCounter[0], jumpCallback);
            }
        });

    }

    @Test
    public void simpleTimeNavigationTest() {
        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();

        Assert.assertNotNull(dimension0);

        // create time0
        final CloudView t0 = dimension0.time(0l);

        // create node0 and element0 and link them
        final Node node0 = t0.createNode();
        final Element element0 = t0.createElement();
        node0.setElement(element0);
/*
                node0.getElement(new Callback<Element>() {
                    @Override
                    public void on(Element element) {
                        Assert.assertEquals(element0, element);
                        Assert.assertEquals(element.now(), t0.now());
                    }
                });*/

        t0.lookup(node0.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                ((Node) kObject).getElement(new KCallback<Element>() {
                    @Override
                    public void on(Element element) {
                        org.junit.Assert.assertEquals(element0, element);
                        org.junit.Assert.assertEquals(element.now(), t0.now());
                    }
                });
            }
        });
    }


    @Test
    public void distortedTimeNavigationTest() {
        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();

        Assert.assertNotNull(dimension0);

        // create time0
        final CloudView t0 = dimension0.time(0l);
        // create node0
        final Node node0 = t0.createNode();

        node0.getElement(new KCallback<Element>() {
            @Override
            public void on(Element element) {
                Assert.assertNull(element);
            }
        });

        t0.lookup(node0.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                ((Node) kObject).getElement(new KCallback<Element>() {
                    @Override
                    public void on(Element element) {
                        Assert.assertNull(element);
                    }
                });
            }
        });

        // create time1
        final CloudView t1 = dimension0.time(1l);

        // create elem1 and link node0 to elem1
        final Element elem1 = t1.createElement();
        node0.setElement(elem1);

        // at t0 node0.getElement should be null
        t0.lookup(node0.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                ((Node) kObject).getElement(new KCallback<Element>() {
                    @Override
                    public void on(Element element) {
                        Assert.assertNull(element);
                    }
                });
            }
        });

        // at t1 node0.getElement should return elem1
        t1.lookup(node0.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                ((Node) kObject).getElement(new KCallback<Element>() {
                    @Override
                    public void on(Element element) {
                        org.junit.Assert.assertNotNull(element);
                        org.junit.Assert.assertEquals(element, elem1);
                        org.junit.Assert.assertEquals(element.now(), t1.now());
                    }
                });
            }
        });
    }

    @Test
    public void objectModificationTest() {
        CloudModel universe = new CloudModel();
        universe.connect(new KCallback() {
            @Override
            public void on(Object o) {
                CloudUniverse dimension0 = universe.newUniverse();

                Assert.assertNotNull(dimension0);

                // create time0
                final CloudView t0 = dimension0.time(0l);
                // create node0 and elem0 and link them
                final Node node0 = t0.createNode();
                node0.setName("node at 0");
                node0.setValue("0");

                final Element elem0 = t0.createElement();
                node0.setElement(elem0);

                // create time1
                final CloudView t1 = dimension0.time(1l);
                t1.lookup(node0.uuid(), new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        ((Node) kObject).setName("node at 1");
                        ((Node) kObject).setValue("1");
                    }
                });

                // check name and value of node0 at t0
                t0.lookup(node0.uuid(), new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        Assert.assertEquals(((Node) kObject).getName(), "node at 0");
                        Assert.assertEquals(((Node) kObject).getValue(), "0");
                    }
                });


                // check name and value of node0 at t1
                t1.lookup(node0.uuid(), new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        Assert.assertEquals(((Node) kObject).getName(), "node at 1");
                        Assert.assertEquals(((Node) kObject).getValue(), "1");
                    }
                });
            }
        });

    }

    @Test
    public void timeUpdateWithLookupTest() {

        CloudModel model = new CloudModel();
        model.connect(null);
        CloudUniverse universe = model.newUniverse();
        CloudView t0 = universe.time(0L);
        Node node0 = t0.createNode();
        node0.setName("Node0");
        t0.setRoot(node0, null);

        model.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {

            }
        });


        CloudView t1 = universe.time(1L);
        final Element element = t1.createElement();
        element.setName("Element1");
        t1.lookup(node0.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject node0Back) {
                ((Node) node0Back).setElement(element);
            }
        });
        model.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {

            }
        });

        CloudView t0_2 = universe.time(0L);
        t0_2.select("/", new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                if (kObjects != null && kObjects.length > 0) {
                    //TODO reinsert this test
                    //Assert.assertEquals(2, ((Node) kObjects[0]).timeTree().size());
                }
            }
        });


    }


    @Test
    public void timeUpdateWithSelectTest() {

        final CloudModel model = new CloudModel();
        model.connect(null);
        CloudUniverse universe = model.newUniverse();
        CloudView t0 = universe.time(0L);
        Node node0 = t0.createNode();
        node0.setName("Node0");
        t0.setRoot(node0, null);

        model.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {
                model.discard(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable aBoolean) {

                    }
                });
            }
        });


        CloudView t1 = universe.time(1L);
        final Element element = t1.createElement();
        element.setName("Element1");
        t1.select("/", new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                if (kObjects != null && kObjects.length > 0) {
                    ((Node) kObjects[0]).setElement(element);


                }
            }
        });

        model.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {
                model.discard(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable aBoolean) {

                    }
                });
            }
        });

        CloudView t0_2 = universe.time(0L);
        t0_2.select("/", new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                if (kObjects != null && kObjects.length > 0) {
                    //TODO reinsert this test
                    //Assert.assertEquals(2, ((Node) kObjects[0]).timeTree().size());
                }
            }
        });

    }


}
