package org.kevoree.modeling.addons.nunjucks;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

public class TemplateTest {

    //@Test
    public void test() {
        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass sensorClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute sensorValueAtt = sensorClass.addAttribute("value", KPrimitiveTypes.LONG);
        KModel model = metaModel.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject sensor = model.create(sensorClass, 0, 0);
                sensor.set(sensorValueAtt, "42");
                model.save(new KCallback() {
                    @Override
                    public void on(Object o) {

                    }
                });

            }
        });
        WebSocketGateway gateway = WebSocketGateway.exposeModelAndResources(model, 8080, TemplateTest.class.getClassLoader());
        gateway.start();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
