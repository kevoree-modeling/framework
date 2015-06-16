package org.kevoree.modeling.test.opposite;

import junit.framework.TestCase;
import kmf.opposite.test.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class OppositeTest {

    private static OppositeModel model;
    private static OppositeUniverse localUniverse;
    private static OppositeView factory;

    @BeforeClass
    public static void setUp() {
        model = new OppositeModel();
        model.connect(null);
        localUniverse = model.newUniverse();
        factory = localUniverse.time(0l);
    }


    @Test
    public void A_singleRef() { // single ref, not contained, no apposite

        A a = factory.createA();
        B b = factory.createB();
        B b2 = factory.createB();

        a.setSingleRef(b);
        a.getSingleRef((b_cb) -> {
            assertNotNull(b_cb);
            assertEquals(b_cb, b);
        });

        a.setSingleRef(b);
        a.getSingleRef((b_cb) -> {
            assertNotNull(b_cb);
            assertEquals(b_cb, b);
        });

        a.setSingleRef(b2);
        a.getSingleRef((b2_cb) -> {
            assertNotNull(b2_cb);
            assertEquals(b2_cb, b2);
        });

        a.setSingleRef(null);
        a.getSingleRef(org.junit.Assert::assertNull);

    }

    @Test
    public void A_multiRef() { // multi ref, not contained, no apposite
        A a = factory.createA();
        B b = factory.createB();
        B b2 = factory.createB();

        a.addMultiRef(b);
        a.getMultiRef((refs) -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], b);
            }
        });
        assertEquals(1, a.sizeOfMultiRef());

        a.addMultiRef(b);
        a.getMultiRef((refs) -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], b);
            }
        });
        assertEquals(1, a.sizeOfMultiRef());

        a.addMultiRef(b2);

        assertEquals(2, a.sizeOfMultiRef());

        a.addMultiRef(b2);
        assertEquals(2, a.sizeOfMultiRef());

        a.removeMultiRef(b);
        a.getMultiRef((refs) -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], b2);
            }
        });
        assertEquals(1, a.sizeOfMultiRef());

        a.removeMultiRef(b);
        a.getMultiRef((refs) -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], b2);
            }
        });
        assertEquals(1, a.sizeOfMultiRef());

        a.removeMultiRef(b2);
        assertEquals(0, a.sizeOfMultiRef());

        a.removeMultiRef(b2);
        assertEquals(0, a.sizeOfMultiRef());

    }


    @Test
    public void B_singleRef() { // single ref, contained, no opposite
        B b = factory.createB();
        A a = factory.createA();
        A a2 = factory.createA();

        b.setSingleRef(a);
        b.getSingleRef((a_cb) -> {
            assertNotNull(a_cb);
            assertEquals(a_cb, a);

        });

        b.setSingleRef(a);
        b.getSingleRef((a_cb) -> {
            assertNotNull(a_cb);
            assertEquals(a_cb, a);

        });

        b.setSingleRef(a2);
        b.getSingleRef((a_cb) -> {
            assertNotNull(a_cb);
            assertEquals(a_cb, a2);

        });

        b.setSingleRef(null);
        b.getSingleRef(TestCase::assertNull);

    }


    @Test
    public void B_StarList() { // multi ref, contained, no opposite
        B b = factory.createB();
        A a = factory.createA();
        A a2 = factory.createA();

        b.addMultiRef(a);

        b.getMultiRef(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a);
            }
        });

        assertEquals(1, b.sizeOfMultiRef());


        b.addMultiRef(a);
        b.getMultiRef(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a);
            }
        });

        assertEquals(1, b.sizeOfMultiRef());

        b.addMultiRef(a2);

        assertEquals(2, b.sizeOfMultiRef());

        b.addMultiRef(a2);

        assertEquals(2, b.sizeOfMultiRef());

        b.removeMultiRef(a);
        b.getMultiRef(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a2);
            }
        });

        assertEquals(1, b.sizeOfMultiRef());

        b.removeMultiRef(a);
        b.getMultiRef(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a2);
            }
        });

        assertEquals(1, b.sizeOfMultiRef());

        b.removeMultiRef(a2);
        assertEquals(0, b.sizeOfMultiRef());

        b.removeMultiRef(a2);
        assertEquals(0, b.sizeOfMultiRef());

    }


    @Test
    public void singleA_singleB_Test() {  // single ref, contained, opposite
        //val container = TestFactory.createContainer
        B b = factory.createB();
        A a = factory.createA();

        //Set a in B
        b.setSingleA_singleB(a);
        b.getSingleA_singleB((a_cb) -> assertEquals(a_cb, a));
        a.getSingleA_singleB((b_cb) -> {
            assertNotNull(b_cb);
            assertEquals(b_cb, b);
        });

        //Again, should be equivalent to noop
        b.setSingleA_singleB(a);
        b.getSingleA_singleB((a_cb) -> assertEquals(a_cb, a));
        a.getSingleA_singleB((b_cb) -> {
            assertNotNull(b_cb);
            assertEquals(b_cb, b);
        });

        //Remove A from B
        b.setSingleA_singleB(null);
        a.getSingleA_singleB(TestCase::assertNull);

        //Set B in A
        a.setSingleA_singleB(b);
        a.getSingleA_singleB((b_cb) -> assertEquals(b_cb, b));
        b.getSingleA_singleB((a_cb) -> {
            assertNotNull(a_cb);
            assertEquals(a_cb, a);
        });

        //Again, should be equivalent to noop
        a.setSingleA_singleB(b);
        a.getSingleA_singleB((b_cb) -> assertEquals(b_cb, b));
        b.getSingleA_singleB((a_cb) -> {
            assertNotNull(a_cb);
            assertEquals(a_cb, a);
        });

        //Remove B from A
        a.setSingleA_singleB(null);
        b.getSingleA_singleB(TestCase::assertNull);
    }


    @Test
    public void singleA_multiB_Test() {
        //val container = TestFactory.createContainer
        B b = factory.createB();
        A a = factory.createA();
        A a2 = factory.createA();

        b.addSingleA_multiB(a);
        b.getSingleA_multiB(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a);
            }
        });

        assertEquals(1, b.sizeOfSingleA_multiB());
        a.getSingleA_multiB((b_cb) -> {
            assertNotNull(b_cb);
            assertEquals(b_cb, b);
        });

        b.addSingleA_multiB(a2);


        assertEquals(2, b.sizeOfSingleA_multiB());

        b.addSingleA_multiB(a2);

        assertEquals(2, b.sizeOfSingleA_multiB());

        b.removeSingleA_multiB(a);
        b.getSingleA_multiB(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a2);
            }
        });

        assertEquals(1, b.sizeOfSingleA_multiB());
        a.getSingleA_multiB(TestCase::assertNull);

        b.removeSingleA_multiB(a);
        b.getSingleA_multiB(refs -> {
            for (int i = 0; i < refs.length; i++) {
                assertEquals(refs[i], a2);
            }
        });

        assertEquals(1, b.sizeOfSingleA_multiB());
        a.getSingleA_multiB(TestCase::assertNull);


        b.removeSingleA_multiB(a2);
        assertEquals(0, b.sizeOfSingleA_multiB());
        a.getSingleA_multiB(TestCase::assertNull);
        a2.getSingleA_multiB(TestCase::assertNull);

    }


    @Test
    public void multiA_multiB_Test() {

        B b = factory.createB();
        B b2 = factory.createB();
        A a = factory.createA();
        A a2 = factory.createA();

        a.addMultiA_multiB(b);
        assert (b.sizeOfMultiA_multiB() == 1);
        assert (a.sizeOfMultiA_multiB() == 1);


        a.addMultiA_multiB(b2);
        assert (b2.sizeOfMultiA_multiB() == 1);
        assert (b.sizeOfMultiA_multiB() == 1);
        assert (a.sizeOfMultiA_multiB() == 2);

        b.addMultiA_multiB(a);
        assert (b.sizeOfMultiA_multiB() == 1);
        assert (b2.sizeOfMultiA_multiB() == 1);
        assert (a.sizeOfMultiA_multiB() == 2);


        b.addMultiA_multiB(a2);
        assertEquals(2, b.sizeOfMultiA_multiB());
        assertEquals(1, b2.sizeOfMultiA_multiB());
        assertEquals(2, a.sizeOfMultiA_multiB());
        assertEquals(1, a2.sizeOfMultiA_multiB());


        b2.addMultiA_multiB(a2);
        assertEquals(2, b.sizeOfMultiA_multiB());
        assertEquals(2, b2.sizeOfMultiA_multiB());
        assertEquals(2, a.sizeOfMultiA_multiB());
        assertEquals(2, a2.sizeOfMultiA_multiB());


        b2.addMultiA_multiB(a2);
        assertEquals(2, b.sizeOfMultiA_multiB());
        assertEquals(2, b2.sizeOfMultiA_multiB());
        assertEquals(2, a.sizeOfMultiA_multiB());
        assertEquals(2, a2.sizeOfMultiA_multiB());


        b.removeMultiA_multiB(a);
        assert (b.sizeOfMultiA_multiB() == 1);
        assert (b2.sizeOfMultiA_multiB() == 2);
        assert (a.sizeOfMultiA_multiB() == 1);
        assert (a2.sizeOfMultiA_multiB() == 2);

    }


    @Test
    public void oppositeSimpleA_oppositeSimpleB_Test() {

        Container c = factory.createContainer();
        B b = factory.createB();
        B b2 = factory.createB();
        A a = factory.createA();
        A a2 = factory.createA();

        c.addAees(a);
        c.addAees(a2);
        c.addBees(b);
        c.addBees(b2);

        assert (c.sizeOfAees() == 2);
        assert (c.sizeOfBees() == 2);

        assert (a.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);
        assert (b.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);
        assert (a2.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);
        assert (b2.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);

        a.addOppositeSimpleA_oppositeSimpleB(b);

        assert (a.sizeOfOppositeSimpleA_oppositeSimpleB() == 1);
        assert (b.sizeOfOppositeSimpleA_oppositeSimpleB() == 1);
        assert (a2.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);
        assert (b2.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);

        a.addOppositeSimpleA_oppositeSimpleB(b2);

        assert (a.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        assert (b.sizeOfOppositeSimpleA_oppositeSimpleB() == 1);
        assert (a2.sizeOfOppositeSimpleA_oppositeSimpleB() == 0);
        assert (b2.sizeOfOppositeSimpleA_oppositeSimpleB() == 1);

        b.addOppositeSimpleA_oppositeSimpleB(a2);

        assert (a.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        assert (b.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        TestCase.assertEquals(1, a2.sizeOfOppositeSimpleA_oppositeSimpleB());
        assert (b2.sizeOfOppositeSimpleA_oppositeSimpleB() == 1);

        b2.addOppositeSimpleA_oppositeSimpleB(a2);

        assert (a.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        assert (b.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        assert (a2.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
        assert (b2.sizeOfOppositeSimpleA_oppositeSimpleB() == 2);
    }


}