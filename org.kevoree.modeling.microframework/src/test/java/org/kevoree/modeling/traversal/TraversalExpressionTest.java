package org.kevoree.modeling.traversal;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class TraversalExpressionTest {

    @Test
    public void test() {
        MetaModel metaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = metaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        final KModel universe = metaModel.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                sensor.setByName("name", "Sensor#1");
                sensor.setByName("value", "42");

                final int[] i = {0};
                sensor.traversal().eval("value/2", new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length,1);
                        Assert.assertEquals(objects[0],21.0);
                        i[0]++;
                    }
                });
                sensor.select("=value/2", new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects.length,1);
                        Assert.assertEquals(objects[0],21.0);
                        i[0]++;
                    }
                });
                Assert.assertEquals(i[0],2);
            }
        });
    }

}
