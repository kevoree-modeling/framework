package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.*;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.impl.MetaModel;


/**
 * Created by gnain on 16/06/15.
 */
public class OppositeTest {

    private MetaModel metaModel;
    private KModel model;
    private KView factory;
    private KMetaClass A_Class, B_Class;


    public OppositeTest() {
        metaModel = new MetaModel("OppositeMM");
        A_Class = metaModel.addMetaClass("A");
        B_Class = metaModel.addMetaClass("B");

        A_Class.addReference("singleRef", B_Class, null, false);
        A_Class.addReference("multiRef", B_Class, null, true);
        A_Class.addReference("singleA_singleB", B_Class, "singleA_singleB", false);
        A_Class.addReference("singleA_multiB", B_Class, "singleA_multiB", false);
        A_Class.addReference("multiA_multiB", B_Class, "multiA_multiB", true);

        B_Class.addReference("singleA_singleB", A_Class, "singleA_singleB", false);
        B_Class.addReference("singleA_multiB", A_Class, "singleA_multiB", true);
        B_Class.addReference("multiA_multiB", A_Class, "multiA_multiB", true);


        model = metaModel.model();
        model.connect(null);
        KUniverse localUniverse = model.newUniverse();
        factory = localUniverse.time(0l);

    }


    @Test
    public void A_singleRef() { // single ref, not contained, no apposite
        KObject a = factory.createByName("A");
        KObject b = factory.createByName("B");
        KObject b2 = factory.createByName("B");

        a.mutate(KActionType.SET, A_Class.reference("singleRef"), b);
        a.ref(A_Class.reference("singleRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb[0], b);
            }
        });

        a.mutate(KActionType.SET, A_Class.reference("singleRef"), b);
        a.ref(A_Class.reference("singleRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb[0], b);
            }
        });

        a.mutate(KActionType.SET, A_Class.reference("singleRef"), b2);
        a.ref(A_Class.reference("singleRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b2_cb) {
                Assert.assertNotNull(b2_cb);
                Assert.assertEquals(b2_cb[0], b2);
        }});

        a.mutate(KActionType.SET, A_Class.reference("singleRef"), null);
        a.ref(A_Class.reference("singleRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb.length, 0);
        }});

    }


    @Test
    public void A_multiRef() { // multi ref, not contained, no apposite
        KObject a = factory.createByName("A");
        KObject b = factory.createByName("B");
        KObject b2 = factory.createByName("B");

        a.mutate(KActionType.ADD, A_Class.reference("multiRef"), b);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });

        a.mutate(KActionType.ADD, A_Class.reference("multiRef"), b);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });


        a.mutate(KActionType.ADD, A_Class.reference("multiRef"), b2);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(2, b_cb.length);
            }
        });


        a.mutate(KActionType.ADD, A_Class.reference("multiRef"), b2);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(2, b_cb.length);
            }
        });


        a.mutate(KActionType.REMOVE, A_Class.reference("multiRef"), b);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b2);
                }
            }
        });

        a.mutate(KActionType.REMOVE, A_Class.reference("multiRef"), b);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b2);
                }
            }
        });

        a.mutate(KActionType.REMOVE, A_Class.reference("multiRef"), b2);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(0, b_cb.length);
            }
        });

        a.mutate(KActionType.REMOVE, A_Class.reference("multiRef"), b2);
        a.ref(A_Class.reference("multiRef"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(0, b_cb.length);
            }
        });


    }



    @Test
    public void singleA_singleB_Test() {  // single ref, contained, opposite
        //val container = TestFactory.createContainer
        KObject a = factory.createByName("A");
        KObject b = factory.createByName("B");

        //Set a in B
        b.mutate(KActionType.SET, B_Class.reference("singleA_singleB"), a);
        b.ref(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertNotNull(a_cb);
                Assert.assertEquals(a_cb[0], a);
            }
        });
        a.ref(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb[0], b);
            }
        });

        //Set a in B
        b.mutate(KActionType.SET, B_Class.reference("singleA_singleB"), a);
        b.ref(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertNotNull(a_cb);
                Assert.assertEquals(a_cb[0], a);
            }
        });
        a.ref(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertNotNull(b_cb);
                Assert.assertEquals(b_cb[0], b);
            }
        });


        b.mutate(KActionType.SET, B_Class.reference("singleA_singleB"), null);
        b.ref(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(a_cb.length, 0);
            }
        });
        a.ref(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(b_cb.length, 0);
            }
        });

        //NOOP
        b.mutate(KActionType.SET, B_Class.reference("singleA_singleB"), null);
        b.ref(B_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(a_cb.length, 0);
            }
        });
        a.ref(A_Class.reference("singleA_singleB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(b_cb.length, 0);
            }
        });

    }


    @Test
    public void singleA_multiB_Test() {
        //val container = TestFactory.createContainer

        KObject a = factory.createByName("A");
        KObject a2 = factory.createByName("A");
        KObject b = factory.createByName("B");


        b.mutate(KActionType.ADD, B_Class.reference("singleA_multiB"), a);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
                for (int i = 0; i < a_cb.length; i++) {
                    Assert.assertEquals(a_cb[i], a);
                }
            }
        });

        b.mutate(KActionType.ADD, B_Class.reference("singleA_multiB"), a);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
                for (int i = 0; i < a_cb.length; i++) {
                    Assert.assertEquals(a_cb[i], a);
                }
            }
        });
        a.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });

        b.mutate(KActionType.ADD, B_Class.reference("singleA_multiB"), a2);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });
        a2.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(1, b_cb.length);
                for (int i = 0; i < b_cb.length; i++) {
                    Assert.assertEquals(b_cb[i], b);
                }
            }
        });


        //NOOP
        b.mutate(KActionType.ADD, B_Class.reference("singleA_multiB"), a2);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });


        b.mutate(KActionType.REMOVE, B_Class.reference("singleA_multiB"), a);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
                for (int i = 0; i < a_cb.length; i++) {
                    Assert.assertEquals(a_cb[i], a2);
                }
            }
        });
        a.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(0, a_cb.length);
            }
        });


        //NOOP
        b.mutate(KActionType.REMOVE, B_Class.reference("singleA_multiB"), a);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
                for (int i = 0; i < a_cb.length; i++) {
                    Assert.assertEquals(a_cb[i], a2);
                }
            }
        });

        //NOOP
        b.mutate(KActionType.REMOVE, B_Class.reference("singleA_multiB"), a2);
        b.ref(B_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(0, a_cb.length);
            }
        });
        a.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(0, b_cb.length);
            }
        });
        a2.ref(A_Class.reference("singleA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] b_cb) {
                Assert.assertEquals(0, b_cb.length);
            }
        });

    }


    @Test
    public void multiA_multiB_Test() {

        KObject a = factory.createByName("A");
        KObject a2 = factory.createByName("A");
        KObject b = factory.createByName("B");
        KObject b2 = factory.createByName("B");


        a.mutate(KActionType.ADD, A_Class.reference("multiA_multiB"), b);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });

        a.mutate(KActionType.ADD, A_Class.reference("multiA_multiB"), b2);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });


        b.mutate(KActionType.ADD, B_Class.reference("multiA_multiB"), a);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });


        b.mutate(KActionType.ADD, B_Class.reference("multiA_multiB"), a2);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });


        b2.mutate(KActionType.ADD, B_Class.reference("multiA_multiB"), a2);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });

        //NOOP
        b2.mutate(KActionType.ADD, B_Class.reference("multiA_multiB"), a2);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });

        //NOOP
        b.mutate(KActionType.REMOVE, B_Class.reference("multiA_multiB"), a);
        b.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        b2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
        a.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(1, a_cb.length);
            }
        });
        a2.ref(B_Class.reference("multiA_multiB"), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] a_cb) {
                Assert.assertEquals(2, a_cb.length);
            }
        });
/*

        b.removeMultiA_multiB(a);
        assert (b.sizeOfMultiA_multiB() == 1);
        assert (b2.sizeOfMultiA_multiB() == 2);
        assert (a.sizeOfMultiA_multiB() == 1);
        assert (a2.sizeOfMultiA_multiB() == 2);
*/
    }





}
