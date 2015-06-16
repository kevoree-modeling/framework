package org.kevoree.modeling.traversal;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Element;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;

/**
 * Created by thomas on 19/12/14.
 */
public class TraversalTest {

    @Test
    public void simpleTraversalTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(null);
        final CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);

        final Node node0 = t0.createNode();
        final Element elem0_0 = t0.createElement();
        node0.setElement(elem0_0);

        t0.setRoot(node0, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                final Node node1 = t0.createNode();
                node1.setName("child1");
                final Element elem1_0 = t0.createElement();
                node1.setElement(elem1_0);

                final Node node2 = t0.createNode();
                node2.setName("child2");
                final Element elem2_0 = t0.createElement();
                node2.setElement(elem2_0);

                node0.addChildren(node1);
                node0.addChildren(node2);

                // traversal promise
                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects.length, 2);
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withAttribute(MetaNode.ATT_NAME, "child*").map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 2);
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withAttribute(MetaNode.ATT_NAME, "child1").map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 1);
                        Assert.assertEquals(objects[0], "child1");
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withoutAttribute(MetaNode.ATT_NAME, "child1").map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 1);
                        Assert.assertEquals(objects[0], "child2");
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withAttribute(MetaNode.ATT_NAME, null).map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 0);
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withAttribute(MetaNode.ATT_NAME, "*").map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 2);
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withoutAttribute(MetaNode.ATT_NAME, null).map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 2);
                    }
                });

                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).withoutAttribute(MetaNode.ATT_NAME, "*").map(MetaNode.ATT_NAME, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length, 0);
                    }
                });

            }
        });
    }

    @Test
    public void chainedTraversalTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(null);
        final CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);

        final Node node0 = t0.createNode();
        final Element elem0_0 = t0.createElement();
        node0.setElement(elem0_0);

        t0.setRoot(node0, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                final Node node1 = t0.createNode();
                node1.setName("child1");
                final Element elem1_0 = t0.createElement();
                elem1_0.setName("child1_elem1");
                node1.setElement(elem1_0);

                final Node node2 = t0.createNode();
                node2.setName("child2");
                final Element elem2_0 = t0.createElement();
                elem2_0.setName("child2_elem1");
                node2.setElement(elem2_0);

                node0.addChildren(node1);
                node0.addChildren(node2);

                // chained traversal promise
                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).traverse((KMetaReference) node0.metaClass().metaByName("element")).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects.length, 2);
                    }
                });

            }
        });
    }

    @Test
    public void filterTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(null);
        final CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);

        final Node node0 = t0.createNode();
        final Element elem0_0 = t0.createElement();
        node0.setElement(elem0_0);

        t0.setRoot(node0, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                final Node node1 = t0.createNode();
                node1.setName("child1");
                final Element elem1_0 = t0.createElement();
                elem1_0.setName("child1_elem1");
                node1.setElement(elem1_0);

                final Node node2 = t0.createNode();
                node2.setName("child2");
                final Element elem2_0 = t0.createElement();
                elem2_0.setName("child2_elem1");
                node2.setElement(elem2_0);

                node0.addChildren(node1);
                node0.addChildren(node2);

                // chained traversal promise
                node0.traversal().traverse((KMetaReference) node0.metaClass().metaByName("children")).filter(new KTraversalFilter() {
                    @Override
                    public boolean filter(KObject obj) {
                        return ((Node) obj).getName().equals("child1");
                    }
                }).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects.length, 1);
                        Assert.assertEquals(((Node) kObjects[0]).getName(), "child1");

                    }
                });

            }
        });
    }

    @Test
    public void parentTest() {
        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                final CloudUniverse universe = model.newUniverse();
                final CloudView t0 = universe.time(0l);
                final Node node0 = t0.createNode();
                final Element elem0_0 = t0.createElement();
                node0.setElement(elem0_0);
                t0.setRoot(node0, new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        final Node node1 = t0.createNode();
                        node1.setName("child1");
                        final Element elem1_0 = t0.createElement();
                        elem1_0.setName("child1_elem1");
                        node1.setElement(elem1_0);
                        final Node node2 = t0.createNode();
                        node2.setName("child2");
                        final Element elem2_0 = t0.createElement();
                        elem2_0.setName("child2_elem1");
                        node2.setElement(elem2_0);
                        node0.addChildren(node1);
                        node0.addChildren(node2);
                    }
                });
            }
        });
    }

    @Test
    public void traverseQueryTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                final CloudUniverse dimension0 = universe.newUniverse();
                final CloudView t0 = dimension0.time(0l);

                final Node node0 = t0.createNode();
                final Element elem0_0 = t0.createElement();
                node0.setElement(elem0_0);

                t0.setRoot(node0, new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {

                        final Node node1 = t0.createNode();
                        node1.setName("child1");
                        final Element elem1_0 = t0.createElement();
                        elem1_0.setName("child1_elem1");
                        node1.setElement(elem1_0);

                        final Node node2 = t0.createNode();
                        node2.setName("child2");
                        final Element elem2_0 = t0.createElement();
                        elem2_0.setName("child2_elem1");
                        node2.setElement(elem2_0);

                        node0.addChildren(node1);
                        node0.addChildren(node2);


                        // chained traversal promise
                        node0.traversal().traverseQuery("children").filter(new KTraversalFilter() {
                            @Override
                            public boolean filter(KObject obj) {
                                return ((Node) obj).getName().equals("child1");
                            }
                        }).then(new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] kObjects) {
                                Assert.assertEquals(kObjects.length, 1);
                                Assert.assertEquals(((Node) kObjects[0]).getName(), "child1");
                            }
                        });

                        // chained traversal promise
                        node0.traversal().traverseQuery("child*").filter(new KTraversalFilter() {
                            @Override
                            public boolean filter(KObject obj) {
                                return ((Node) obj).getName().equals("child1");
                            }
                        }).then(new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] kObjects) {
                                Assert.assertEquals(kObjects.length, 1);
                                Assert.assertEquals(((Node) kObjects[0]).getName(), "child1");
                            }
                        });

                        // chained traversal promise
                        node0.traversal().traverseQuery("*children").filter(new KTraversalFilter() {
                            @Override
                            public boolean filter(KObject obj) {
                                return ((Node) obj).getName().equals("child1");
                            }
                        }).then(new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] kObjects) {
                                Assert.assertEquals(kObjects.length, 1);
                                Assert.assertEquals(((Node) kObjects[0]).getName(), "child1");
                            }
                        });

                        // chained traversal promise
                        node0.traversal().traverseQuery("cc,children").filter(new KTraversalFilter() {
                            @Override
                            public boolean filter(KObject obj) {
                                return ((Node) obj).getName().equals("child1");
                            }
                        }).then(new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] kObjects) {
                                Assert.assertEquals(kObjects.length, 1);
                                Assert.assertEquals(((Node) kObjects[0]).getName(), "child1");
                            }
                        });

                    }
                });
            }
        });
    }

    @Test
    public void attributeQueryTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(null);
        final CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);

        final Node node0 = t0.createNode();
        final Element elem0_0 = t0.createElement();
        node0.setElement(elem0_0);

        t0.setRoot(node0, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                final Node node1 = t0.createNode();
                node1.setName("child1");
                final Element elem1_0 = t0.createElement();
                node1.setElement(elem1_0);

                final Node node2 = t0.createNode();
                node2.setName("child2");
                final Element elem2_0 = t0.createElement();
                node2.setElement(elem2_0);

                node0.addChildren(node1);
                node0.addChildren(node2);


                node0.traversal().traverseQuery("children").attributeQuery("name=*").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(2, kObjects.length);
                    }
                });

                node0.traversal().traverseQuery("children").attributeQuery("name=child1").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(1, kObjects.length);
                        Assert.assertEquals("child1", kObjects[0].get(MetaNode.ATT_NAME));
                    }
                });

                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value=null").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(2, kObjects.length);
                    }
                });

                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value!=null").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(0, kObjects.length);
                    }
                });


                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value=*").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(2, kObjects.length);
                    }
                });


                final Node node3 = t0.createNode();
                node3.setName("child3");
                node3.setValue("3");
                node0.addChildren(node3);

                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value=*").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(3, kObjects.length);
                    }
                });

                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value=null").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(2, kObjects.length);
                    }
                });

                node0.traversal().traverseQuery("children").attributeQuery("name=child*,value!=null").then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(1, kObjects.length);
                    }
                });


            }
        });
    }

    @Test
    public void deepCollectorTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(null);
        final CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);

        final Node node0 = t0.createNode();
        node0.setName("c0");
        final Element elem0_0 = t0.createElement();
        elem0_0.setName("c0_e1");
        node0.setElement(elem0_0);

        t0.setRoot(node0, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                final Node node1 = t0.createNode();
                node1.setName("c1");
                final Element elem1_0 = t0.createElement();
                elem1_0.setName("c1_e1");
                node1.setElement(elem1_0);

                final Node node2 = t0.createNode();
                node2.setName("c2");
                final Element elem2_0 = t0.createElement();
                elem2_0.setName("c2_e1");
                node2.setElement(elem2_0);

                node0.addChildren(node1);
                node0.addChildren(node2);

                final Node node1_1 = t0.createNode();
                node1_1.setName("c1_1");
                node1.addChildren(node1_1);

                node0.traversal().collect(null, null).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] collectedObjs) {
                        Assert.assertEquals(6, collectedObjs.length);
                    }
                });

                node0.traversal().collect(MetaNode.REF_CHILDREN, null).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] collectedObjs) {
                        Assert.assertEquals(3, collectedObjs.length);
                    }
                });

                node0.traversal().collect(null, new KTraversalFilter() {
                    @Override
                    public boolean filter(KObject obj) {
                        if (obj.get(MetaNode.ATT_NAME) != null && obj.get(MetaNode.ATT_NAME).toString().equals("c1")) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] collectedObjs) {
                        Assert.assertEquals(3, collectedObjs.length);
                    }
                });

                node0.traversal().collect(null, new KTraversalFilter() {
                    @Override
                    public boolean filter(KObject obj) {
                        if (obj.get(MetaNode.ATT_NAME) != null && obj.get(MetaNode.ATT_NAME).toString().equals("c1_1")) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] collectedObjs) {
                        Assert.assertEquals(5, collectedObjs.length);
                    }
                });

                node0.traversal().collect(MetaNode.REF_CHILDREN, new KTraversalFilter() {
                    @Override
                    public boolean filter(KObject obj) {
                        if (obj.get(MetaNode.ATT_NAME) != null && obj.get(MetaNode.ATT_NAME).toString().equals("c1_1")) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }).then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] collectedObjs) {
                        Assert.assertEquals(2, collectedObjs.length);
                    }
                });
            }
        });
    }

}
