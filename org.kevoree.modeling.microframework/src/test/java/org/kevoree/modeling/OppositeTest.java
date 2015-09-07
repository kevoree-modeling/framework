package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaRelation;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;


public class OppositeTest {

    private MetaModel metaModel;
    private KModel model;
    private KView factory;
    private KMetaClass A_Class, B_Class;


    public OppositeTest() {
        metaModel = new MetaModel("OppositeMM");
        A_Class = metaModel.addMetaClass("A");
        B_Class = metaModel.addMetaClass("B");

        KMetaRelation singleRef = A_Class.addRelation("singleRef", B_Class, null);
        singleRef.setMaxBound(1);

        A_Class.addRelation("multiRef", B_Class, null);
        KMetaRelation singleA_singleB = A_Class.addRelation("singleA_singleB", B_Class, "singleA_singleB");
        singleA_singleB.setMaxBound(1);

        KMetaRelation singleA_multiB = A_Class.addRelation("singleA_multiB", B_Class, "singleA_multiB");
        singleA_multiB.setMaxBound(1);

        A_Class.addRelation("multiA_multiB", B_Class, "multiA_multiB");

        KMetaRelation singleA_singleB_2 = B_Class.addRelation("singleA_singleB", A_Class, "singleA_singleB");
        singleA_singleB_2.setMaxBound(1);

        B_Class.addRelation("singleA_multiB", A_Class, "singleA_multiB");
        B_Class.addRelation("multiA_multiB", A_Class, "multiA_multiB");


        model = metaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
    }

    @Test
    public void mainTest() {
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KUniverse localUniverse = model.newUniverse();
                factory = localUniverse.time(0l);

                A_singleRef();
                A_multiRef();
                singleA_singleB_Test();
                singleA_multiB_Test();
                multiA_multiB_Test();
            }
        });
    }

    public void A_singleRef() { // single ref, not contained, no apposite
        final KObject a = factory.createByName("A");
        final KObject b = factory.createByName("B");
        final KObject b2 = factory.createByName("B");

        a.addByName("singleRef", b);
        a.getRelationByName("singleRef", new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb[0], b);

                a.addByName("singleRef", b);
                a.getRelationByName("singleRef", new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] b_cb) {
                        Assert.assertNotNull(b_cb);
                        Assert.assertEquals(b_cb[0], b);
                        a.removeByName("singleRef", b);
                        a.addByName("singleRef", b2);
                        a.getRelationByName("singleRef", new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] b2_cb) {
                                Assert.assertNotNull(b2_cb);
                                Assert.assertEquals(b2_cb[0], b2);

                                a.removeByName("singleRef", b2);
                                a.getRelationByName("singleRef", new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] b_cb) {
                                        Assert.assertNotNull(b_cb);
                                        Assert.assertEquals(b_cb.length, 0);
                                    }
                                });

                            }
                        });

                    }
                });
            }
        });

    }

    public void A_multiRef() { // multi ref, not contained, no apposite
        KObject a = factory.createByName("A");
        KObject b = factory.createByName("B");
        KObject b2 = factory.createByName("B");

        a.add(A_Class.reference("multiRef"), b);
        a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });
        a.add(A_Class.reference("multiRef"), b);
        a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
                a.add(A_Class.reference("multiRef"), b2);
                a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] b_cb) {
                        Assert.assertEquals(2, b_cb.length);

                        a.add(A_Class.reference("multiRef"), b2);
                        a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] b_cb) {
                                Assert.assertEquals(2, b_cb.length);

                                a.remove(A_Class.reference("multiRef"), b);
                                a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] b_cb) {
                                        Assert.assertEquals(1, b_cb.length);
                                        for (int i = 0; i < b_cb.length; i++) {
                                            Assert.assertEquals(b_cb[i], b2);
                                        }

                                        a.remove(A_Class.reference("multiRef"), b);
                                        a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] b_cb) {
                                                Assert.assertEquals(1, b_cb.length);
                                                for (int i = 0; i < b_cb.length; i++) {
                                                    Assert.assertEquals(b_cb[i], b2);
                                                }

                                                a.remove(A_Class.reference("multiRef"), b2);
                                                a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                                                    @Override
                                                    public void on(KObject[] b_cb) {
                                                        Assert.assertEquals(0, b_cb.length);

                                                        a.remove(A_Class.reference("multiRef"), b2);
                                                        a.getRelation(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
                                                            @Override
                                                            public void on(KObject[] b_cb) {
                                                                Assert.assertEquals(0, b_cb.length);
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

    public void singleA_singleB_Test() {
        // single ref, contained, opposite
        //val container = TestFactory.createContainer
        KObject a = factory.createByName("A");
        KObject b = factory.createByName("B");

        //Set a in B
        b.add(B_Class.reference("singleA_singleB"), a);
        b.getRelation(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertNotNull(a_cb);
                Assert.assertEquals(a_cb[0], a);

                a.getRelation(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] b_cb) {
                        Assert.assertNotNull(b_cb);
                        Assert.assertEquals(b_cb[0], b);

                        //Set a in B
                        b.add(B_Class.reference("singleA_singleB"), a);
                        b.getRelation(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] a_cb) {
                                Assert.assertNotNull(a_cb);
                                Assert.assertEquals(a_cb[0], a);

                                a.getRelation(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] b_cb) {
                                        Assert.assertNotNull(b_cb);
                                        Assert.assertEquals(b_cb[0], b);

                                        b.remove(B_Class.reference("singleA_singleB"), a);
                                        b.getRelation(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] a_cb) {
                                                Assert.assertEquals(a_cb.length, 0);

                                                a.getRelation(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                                                    @Override
                                                    public void on(KObject[] b_cb) {
                                                        Assert.assertEquals(b_cb.length, 0);

                                                        //NOOP
                                                        //b.remove(B_Class.reference("singleA_singleB"), null);
                                                        b.getRelation(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                                                            @Override
                                                            public void on(KObject[] a_cb) {
                                                                Assert.assertEquals(a_cb.length, 0);
                                                                a.getRelation(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
                                                                    @Override
                                                                    public void on(KObject[] b_cb) {
                                                                        Assert.assertEquals(b_cb.length, 0);
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

    public void singleA_multiB_Test() {
        //val container = TestFactory.createContainer

        KObject a = factory.createByName("A");
        KObject a2 = factory.createByName("A");
        KObject b = factory.createByName("B");


        b.add(B_Class.reference("singleA_multiB"), a);
        b.add(B_Class.reference("singleA_multiB"), a);
        b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
                for (int i = 0; i < a_cb.length; i++) {
                    Assert.assertEquals(a_cb[i], a);
                }
                b.add(B_Class.reference("singleA_multiB"), a);
                b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] a_cb) {
                        Assert.assertEquals(1, a_cb.length);
                        for (int i = 0; i < a_cb.length; i++) {
                            Assert.assertEquals(a_cb[i], a);
                        }


                        a.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] b_cb) {
                                Assert.assertEquals(1, b_cb.length);
                                for (int i = 0; i < b_cb.length; i++) {
                                    Assert.assertEquals(b_cb[i], b);
                                }

                                b.add(B_Class.reference("singleA_multiB"), a2);
                                b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] a_cb) {
                                        Assert.assertEquals(2, a_cb.length);


                                        a.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] b_cb) {
                                                Assert.assertEquals(1, b_cb.length);
                                                for (int i = 0; i < b_cb.length; i++) {
                                                    Assert.assertEquals(b_cb[i], b);
                                                }


                                                a2.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                    @Override
                                                    public void on(KObject[] b_cb) {
                                                        Assert.assertEquals(1, b_cb.length);
                                                        for (int i = 0; i < b_cb.length; i++) {
                                                            Assert.assertEquals(b_cb[i], b);
                                                        }

                                                        //NOOP
                                                        b.add(B_Class.reference("singleA_multiB"), a2);
                                                        b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                            @Override
                                                            public void on(KObject[] a_cb) {
                                                                Assert.assertEquals(2, a_cb.length);
                                                                b.remove(B_Class.reference("singleA_multiB"), a);
                                                                b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                    @Override
                                                                    public void on(KObject[] a_cb) {
                                                                        Assert.assertEquals(1, a_cb.length);
                                                                        for (int i = 0; i < a_cb.length; i++) {
                                                                            Assert.assertEquals(a_cb[i], a2);
                                                                        }

                                                                        a.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                            @Override
                                                                            public void on(KObject[] a_cb) {
                                                                                Assert.assertEquals(0, a_cb.length);


                                                                                //NOOP
                                                                                b.remove(B_Class.reference("singleA_multiB"), a);
                                                                                b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                                    @Override
                                                                                    public void on(KObject[] a_cb) {
                                                                                        Assert.assertEquals(1, a_cb.length);
                                                                                        for (int i = 0; i < a_cb.length; i++) {
                                                                                            Assert.assertEquals(a_cb[i], a2);
                                                                                        }

                                                                                        //NOOP
                                                                                        b.remove(B_Class.reference("singleA_multiB"), a2);
                                                                                        b.getRelation(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                                            @Override
                                                                                            public void on(KObject[] a_cb) {
                                                                                                Assert.assertEquals(0, a_cb.length);


                                                                                                a.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                                                    @Override
                                                                                                    public void on(KObject[] b_cb) {
                                                                                                        Assert.assertEquals(0, b_cb.length);

                                                                                                        a2.getRelation(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
                                                                                                            @Override
                                                                                                            public void on(KObject[] b_cb) {
                                                                                                                Assert.assertEquals(0, b_cb.length);
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

                        });

                    }
                });

            }
        });


    }

    public void multiA_multiB_Test() {

        KObject a = factory.createByName("A");
        KObject a2 = factory.createByName("A");
        KObject b = factory.createByName("B");
        KObject b2 = factory.createByName("B");

        a.add(A_Class.reference("multiA_multiB"), b);

        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);

                a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] a_cb) {
                        Assert.assertEquals(1, a_cb.length);


                        a.add(A_Class.reference("multiA_multiB"), b2);

                        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] a_cb) {
                                Assert.assertEquals(1, a_cb.length);

                                b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] a_cb) {
                                        Assert.assertEquals(1, a_cb.length);

                                        a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] a_cb) {
                                                Assert.assertEquals(2, a_cb.length);

                                                b.add(B_Class.reference("multiA_multiB"), a);

                                                b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                    @Override
                                                    public void on(KObject[] a_cb) {
                                                        Assert.assertEquals(1, a_cb.length);


                                                        b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                            @Override
                                                            public void on(KObject[] a_cb) {
                                                                Assert.assertEquals(1, a_cb.length);

                                                                a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                    @Override
                                                                    public void on(KObject[] a_cb) {
                                                                        Assert.assertEquals(2, a_cb.length);

                                                                        b.add(B_Class.reference("multiA_multiB"), a2);
                                                                        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                            @Override
                                                                            public void on(KObject[] a_cb) {
                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                    @Override
                                                                                    public void on(KObject[] a_cb) {
                                                                                        Assert.assertEquals(1, a_cb.length);

                                                                                        a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                            @Override
                                                                                            public void on(KObject[] a_cb) {
                                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                                a2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                    @Override
                                                                                                    public void on(KObject[] a_cb) {
                                                                                                        Assert.assertEquals(1, a_cb.length);

                                                                                                        b2.add(B_Class.reference("multiA_multiB"), a2);
                                                                                                        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                            @Override
                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                                                b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                    @Override
                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                        Assert.assertEquals(2, a_cb.length);
                                                                                                                        a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                            @Override
                                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                                                                a2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                    @Override
                                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                                        Assert.assertEquals(2, a_cb.length);

                                                                                                                                        //NOOP
                                                                                                                                        b2.add(B_Class.reference("multiA_multiB"), a2);
                                                                                                                                        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                            @Override
                                                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                                                                                b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                                                        Assert.assertEquals(2, a_cb.length);

                                                                                                                                                        a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                            @Override
                                                                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                                                                Assert.assertEquals(2, a_cb.length);

                                                                                                                                                                a2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                                    @Override
                                                                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                                                                        Assert.assertEquals(2, a_cb.length);

                                                                                                                                                                        //NOOP
                                                                                                                                                                        b.remove(B_Class.reference("multiA_multiB"), a);
                                                                                                                                                                        b.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                                            @Override
                                                                                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                                                                                Assert.assertEquals(1, a_cb.length);

                                                                                                                                                                                b2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                                                    @Override
                                                                                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                                                                                        Assert.assertEquals(2, a_cb.length);

                                                                                                                                                                                        a.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                                                            @Override
                                                                                                                                                                                            public void on(KObject[] a_cb) {
                                                                                                                                                                                                Assert.assertEquals(1, a_cb.length);

                                                                                                                                                                                                a2.getRelation(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
                                                                                                                                                                                                    @Override
                                                                                                                                                                                                    public void on(KObject[] a_cb) {
                                                                                                                                                                                                        Assert.assertEquals(2, a_cb.length);
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
                                });
                            }
                        });

                    }
                });
            }
        });


    }


}
