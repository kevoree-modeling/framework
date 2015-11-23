package org.kevoree.modeling;


import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class IndexTest {

    @Test
    public void test() {
        final CloudModel model = new CloudModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {

                    model.indexByName(0, 0, "root", new KCallback<KObjectIndex>() {
                        @Override
                        public void on(KObjectIndex index) {
                            long empty = index.get("root");
                            Assert.assertEquals(empty, KConfig.NULL_LONG);
                            index.set("root", 42);
                            long nonEmpty = index.get("root");
                            Assert.assertEquals(nonEmpty, 42);
                            model.lookup(0, 0, index.uuid(), new KCallback<KObject>() {
                                @Override
                                public void on(KObject kObject) {
                                    Assert.assertEquals(index.uuid(), kObject.uuid());
                                }
                            });
                            model.indexByName(0, 100, "root", new KCallback<KObjectIndex>() {
                                @Override
                                public void on(KObjectIndex index2) {
                                    Assert.assertEquals(index2.get("root"), 42);
                                }
                            });

                            model.save(new KCallback() {
                                @Override
                                public void on(Object o) {

                                }
                            });

                        }
                    });

                    MetaNode.ATT_NAME.setKey(true);
                    Node node0 = (Node) model.create(MetaNode.getInstance(), 0, 0);
                    node0.setName("node0");
                    node0.setName("node0");

                    model.find(MetaNode.getInstance(), 0, 0, "name=node0", new KCallback<KObject>() {
                        @Override
                        public void on(KObject resolvedObject) {
                            Assert.assertEquals(resolvedObject.uuid(), node0.uuid());
                        }
                    });

                    model.find(MetaNode.getInstance(), 0, 10, "name=node0", new KCallback<KObject>() {
                        @Override
                        public void on(KObject resolvedObject) {
                            Assert.assertEquals(resolvedObject.uuid(), node0.uuid());
                        }
                    });

                    node0.jump(10, new KCallback<KObject>() {
                        @Override
                        public void on(KObject t10elem) {
                            Node t10node0 = (Node) t10elem;
                            Assert.assertEquals(t10node0.getName(), "node0");
                            t10node0.setName("n0");
                            Assert.assertEquals(t10node0.getName(), "n0");
                            //the old name is not indexed anymore
                            model.find(MetaNode.getInstance(), 0, 10, "name=node0", new KCallback<KObject>() {
                                @Override
                                public void on(KObject resolvedObject) {
                                    Assert.assertEquals(resolvedObject, null);
                                }
                            });
                            //the new name is now indexed
                            model.find(MetaNode.getInstance(), 0, 10, "name=n0", new KCallback<KObject>() {
                                @Override
                                public void on(KObject resolvedObject) {
                                    Assert.assertEquals(resolvedObject.uuid(), node0.uuid());
                                }
                            });
                            //the old name is still available in the past
                            model.find(MetaNode.getInstance(), 0, 5, "name=node0", new KCallback<KObject>() {
                                @Override
                                public void on(KObject resolvedObject) {
                                    Assert.assertEquals(resolvedObject.uuid(), node0.uuid());
                                }
                            });

                            model.universe(0).time(10).select("@org.kevoree.modeling.microframework.test.cloud.Node[name=n0]", new KCallback<Object[]>() {
                                @Override
                                public void on(Object[] resolvedObject) {
                                    Assert.assertEquals(resolvedObject.length, 1);
                                    Assert.assertEquals(((KObject) resolvedObject[0]).uuid(), node0.uuid());
                                }
                            });

                        }
                    });


                    /*
                    CloudUniverse universe = model.newUniverse();
                    CloudView time0 = universe.time(0);
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
                    time0.lookup(root.uuid(), new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            Assert.assertNotNull(kObject);
                            Assert.assertEquals(kObject, root);
                        }
                    });

                    //System.err.println("Yop");

*/

                }
            }
        });
    }

}
