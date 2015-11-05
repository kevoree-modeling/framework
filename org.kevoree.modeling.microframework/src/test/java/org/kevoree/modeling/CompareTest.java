package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class CompareTest {

    @Test
    public void test() {
        MetaModel metaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = metaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addRelation("siblings", sensorMetaClass, null);

        final KModel universe = metaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                sensor.setByName("name", "Name1");

                KObject sensor2 = universe.universe(0).time(0).create(sensorMetaClass);
                sensor2.setByName("name", "Name1");

                KMeta[] emptyResult = sensor.compare(sensor2);
                Assert.assertEquals(emptyResult.length, 0);

                KObject sensor3 = universe.universe(0).time(0).create(sensorMetaClass);
                sensor3.setByName("name", "Name3");
                KMeta[] diffOne = sensor.compare(sensor3);
                Assert.assertEquals(diffOne.length, 1);

                sensor3.setByName("name", "Name3");
                sensor3.setByName("value", 3.0d);
                KMeta[] diffTwo = sensor.compare(sensor3);
                Assert.assertEquals(diffTwo.length, 2);

                
            }
        });

    }

}

