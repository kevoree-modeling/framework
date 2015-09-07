package org.kevoree.modeling;

import org.junit.Test;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.concurrent.CountDownLatch;

/**
 * @ignore ts
 */
public class MultiUniverseTest {

    private MetaModel dynamicMetaModel;
    private KMetaClass sensorMetaClass;
    private KModel model;
    private KObject object;

    @Test
    public void testMultiVerse() {
        long timeOrigine = 1000l;

        dynamicMetaModel = new MetaModel("MyMetaModel");

        sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addRelation("siblings", sensorMetaClass, null);

        model = dynamicMetaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                object = model.universe(0).time(timeOrigine).create(sensorMetaClass);


                long unit = 1000;

                insert(0, timeOrigine, 1);
                insert(0, timeOrigine + unit, 5);
                insert(0, timeOrigine + 4 * unit, 8);
                assert (get(0, timeOrigine + 5 * unit) == 8);

                split(0, timeOrigine + 2 * unit);
                assert (get(0, timeOrigine + unit) == 5);
                assert (get(1, timeOrigine + unit) == 5);

                assert (get(0, timeOrigine + 5 * unit) == 8);
                //  assert(getPrimitiveType(1,timeOrigine+5*unit)==5);
                assert (get(1, timeOrigine + 5 * unit) == 8);

                insert(0, timeOrigine + 3 * unit, -2);
                assert (get(0, timeOrigine + 3 * unit) == -2);
                assert (get(1, timeOrigine + 3 * unit) == -2);
                //assert(getPrimitiveType(1,timeOrigine+3*unit)==5);

                insert(1, timeOrigine + 3 * unit, 7);
                assert (get(0, timeOrigine + 3 * unit) == -2);
                assert (get(1, timeOrigine + 3 * unit) == 7);

                split(1, timeOrigine + 20 * unit);
                split(2, timeOrigine + 30 * unit);

                assert (get(2, timeOrigine + 40 * unit) == 7);
                insert(0, timeOrigine + 10 * unit, -20);
                insert(1, timeOrigine + 4 * unit, -15);

                assert (get(1, timeOrigine + 4 * unit) == -15);
                assert (get(2, timeOrigine + 4 * unit) == -15);
                assert (get(3, timeOrigine + 4 * unit) == -15);

                insert(3, timeOrigine + 17 * unit, 80);

                assert (get(0, timeOrigine + 17 * unit) == -20);
                assert (get(1, timeOrigine + 17 * unit) == -15);
                assert (get(2, timeOrigine + 17 * unit) == -15);
                assert (get(3, timeOrigine + 17 * unit) == 80);


                insert(1, timeOrigine + 18 * unit, 100);

                assert (get(0, timeOrigine + 18 * unit) == -20);
                assert (get(1, timeOrigine + 18 * unit) == 100);
                assert (get(2, timeOrigine + 18 * unit) == 100);
                assert (get(3, timeOrigine + 18 * unit) == 80);

                insert(3, timeOrigine + 40 * unit, 78);

                assert (get(0, timeOrigine + 40 * unit) == -20);
                assert (get(1, timeOrigine + 40 * unit) == 100);
                assert (get(2, timeOrigine + 40 * unit) == 100);
                //assert(getPrimitiveType(2,timeOrigine+40*unit)==7);
                assert (get(3, timeOrigine + 40 * unit) == 78);


                insert(2, timeOrigine + 25 * unit, -11);

                assert (get(0, timeOrigine + 25 * unit) == -20);
                assert (get(1, timeOrigine + 25 * unit) == 100);
                assert (get(2, timeOrigine + 25 * unit) == -11);
                assert (get(3, timeOrigine + 25 * unit) == 80);


                insert(2, timeOrigine + 45 * unit, 35);

                assert (get(0, timeOrigine + 45 * unit) == -20);
                assert (get(1, timeOrigine + 45 * unit) == 100);
                assert (get(2, timeOrigine + 45 * unit) == 35);
                assert (get(3, timeOrigine + 45 * unit) == 78);

                assert (get(3, timeOrigine + 1 * unit) == 5);
            }
        });
    }

    private void split(int parent, long splitTime) {
        KUniverse uni = model.universe(parent).diverge();
        //double val = getPrimitiveType(parent,splitTime);
        //insert(uni.key(),splitTime,val);
    }

    private void insert(long uId, long time, final double value) {
        CountDownLatch latch = new CountDownLatch(1);
        model.universe(uId).time(time).lookup(object.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                kObject.set(kObject.metaClass().attribute("value"), value);
                latch.countDown();

            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double get(long uId, long time) {
        final Object[] myvalue = {null};
        CountDownLatch latch = new CountDownLatch(1);
        model.universe(uId).time(time).lookup(object.uuid(), new KCallback<KObject>() {
            @Override
            public void on(KObject kObject) {
                myvalue[0] = kObject.get(kObject.metaClass().attribute("value"));
                latch.countDown();
            }

        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (double) myvalue[0];
    }

}
