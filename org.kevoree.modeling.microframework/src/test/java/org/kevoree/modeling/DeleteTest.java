package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Element;
import org.kevoree.modeling.cloudmodel.Node;

/**
 * Created by gregory.nain on 03/12/14.
 */
public class DeleteTest {

    @Test
    public void basicDeleteTest() {
        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    final CloudUniverse universe = model.newUniverse();
                    CloudView factory = universe.time(0l);
                    Node n = factory.createNode();
                    factory.setRoot(n, new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            model.save(new KCallback<Throwable>() {
                                @Override
                                public void on(Throwable aBoolean) {
                                    model.discard(new KCallback<Throwable>() {
                                        @Override
                                        public void on(Throwable aBoolean) {
                                            CloudView factory1 = universe.time(1l);
                                            final Element e = factory1.createElement();
                                            factory1.select("/", new KCallback<KObject[]>() {
                                                @Override
                                                public void on(KObject[] results) {
                                                    Node n2 = (Node) results[0];
                                                    n2.setElement(e);
                                                    n2.jump(2, new KCallback<KObject>() {
                                                        @Override
                                                        public void on(KObject kObject) {
                                                            Node n2_2 = (Node) kObject;
                                                            n2_2.getElement(new KCallback<Element>() {
                                                                @Override
                                                                public void on(Element element) {
                                                                    element.delete(new KCallback<Throwable>() {
                                                                        @Override
                                                                        public void on(Throwable throwable) {
                                                                            n2_2.jump(3, new KCallback<KObject>() {
                                                                                @Override
                                                                                public void on(KObject kObject) {
                                                                                    Node n2_3 = (Node) kObject;
                                                                                    n2_3.getElement(new KCallback<Element>() {
                                                                                        @Override
                                                                                        public void on(Element element) {
                                                                                            Assert.assertNull(element);
                                                                                            CloudView factory3 = universe.time(3l);
                                                                                            Node n42 = factory3.createNode();
                                                                                            n42.setName("n42");
                                                                                            n2_3.addChildren(n42);

                                                                                            n42.delete(null);

                                                                                            CloudView factory2_2 = universe.time(1l);
                                                                                            factory2_2.select("/", new KCallback<KObject[]>() {
                                                                                                @Override
                                                                                                public void on(KObject[] results) {
                                                                                                    if (results != null && results.length > 0) {
                                                                                                        Node n2 = (Node) results[0];
                                                                                                        n2.getElement(new KCallback<Element>() {
                                                                                                            @Override
                                                                                                            public void on(Element element) {
                                                                                                                Assert.assertNotNull(element);
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    @Test
    public void simpleDeleteTest() {

        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse universe = model.newUniverse();
                CloudView factory = universe.time(0l);
                Node n = factory.createNode();
                n.setName("n");
                factory.setRoot(n, null);
                Node n2 = factory.createNode();
                n2.setName("n2");
                n.addChildren(n2);
                //  n2.delete(null);
                factory.json().save(n, new KCallback<String>() {
                    @Override
                    public void on(String s) {
                        // System.err.println(s);
                    }
                });
            }
        });


    }


}
