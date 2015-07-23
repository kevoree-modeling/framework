package org.kevoree.modeling.addons.template;

import org.kevoree.modeling.*;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaModel;

public class TemplateTest {

    //@Test
    public static void main(String[] args) {

        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass sensorClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute sensorValueAtt = sensorClass.addAttribute("value", KPrimitiveTypes.LONG);
        KMetaReference sensorsRef = sensorClass.addReference("sensors", sensorClass, null, true);


        KModel model = metaModel.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject sensor = model.create(sensorClass, 0, 0);
                sensor.set(sensorValueAtt, "42");

                KListener listener = model.universe(0).createListener();
                listener.listen(sensor);
                listener.then(new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        System.err.println("Update : " + kObject.toJSON());
                    }
                });

                KObject sensor2 = model.create(sensorClass, 0, 0);
                sensor2.set(sensorValueAtt, "43");

                KObject sensor3 = model.create(sensorClass, 0, 0);
                sensor3.set(sensorValueAtt, "44");

                sensor.mutate(KActionType.ADD, sensorsRef, sensor2);
                sensor.mutate(KActionType.ADD, sensorsRef, sensor3);

                model.save(new KCallback() {
                    @Override
                    public void on(Object o) {
                        //done
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
