package org.kevoree.modeling.infer;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.infer.impl.StatInferAlg;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class BasicEnumInferTest {

    private KMetaModel createMetaModel() {
        KMetaModel metaModel = new MetaModel("InferTestMM");

        KMetaEnum metaEnumState = metaModel.addMetaEnum("State");
        metaEnumState.addLiteral("OK");
        metaEnumState.addLiteral("NOK");

        KMetaClass metaClassSensor = metaModel.addMetaClass("Sensor");
        metaClassSensor.addAttribute("name", KPrimitiveTypes.STRING);
        metaClassSensor.addAttribute("state", metaEnumState);

        KMetaClass inferAvg = metaModel.addInferMetaClass("SensorProfile", new StatInferAlg());
        inferAvg.addDependency("sensors", metaClassSensor.index());
        inferAvg.addInput("sensors", "=state");
        inferAvg.addOutput("avg_state", metaEnumState);
        return metaModel;
    }

    @Test
    public void test() {
        KMetaModel mm = createMetaModel();
        KModel model = mm.createModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject sensor1 = model.createByName("Sensor", 0, 0);
                sensor1.setByName("name", "sensor#1");
                sensor1.setByName("state", mm.metaTypeByName("State").literalByName("OK"));
                Assert.assertEquals(sensor1.getByName("state"), mm.metaTypeByName("State").literalByName("OK"));
                sensor1.setByName("state", "NOK");
                Assert.assertEquals(sensor1.getByName("state"), mm.metaTypeByName("State").literalByName("NOK"));
                sensor1.setByName("state", "OK");

                KObject sensor2 = model.createByName("Sensor", 0, 0);
                sensor2.setByName("name", "sensor#2");
                sensor2.setByName("state", mm.metaTypeByName("State").literalByName("NOK"));

                KObjectInfer sensorProfile = (KObjectInfer) model.createByName("SensorProfile", 0, 0);
                sensorProfile.genericTrain(new KObject[]{sensor1}, null, null);
                sensorProfile.genericTrain(new KObject[]{sensor2}, null, null);
                sensorProfile.genericTrain(new KObject[]{sensor2}, null, null);

                sensorProfile.genericInfer(new KObject[]{sensor2}, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        Assert.assertEquals(objects[0], mm.metaTypeByName("State").literalByName("NOK"));
                    }
                });

            }
        });
    }

}
