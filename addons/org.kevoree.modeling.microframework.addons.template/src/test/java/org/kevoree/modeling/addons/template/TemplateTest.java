package org.kevoree.modeling.addons.template;

import org.kevoree.modeling.*;
import org.kevoree.modeling.drivers.websocket.gateway.WebSocketGateway;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TemplateTest {

    //@Test
    public static void main(String[] args) {

        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass sensorClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute sensorValueAtt = sensorClass.addAttribute("value", KPrimitiveTypes.LONG);
        KMetaReference sensorsRef = sensorClass.addReference("sensors", sensorClass, null, true);

        ScheduledExecutorService serviceExecutor = Executors.newSingleThreadScheduledExecutor();


        KModel model = metaModel.createModel(DataManagerBuilder.buildDefault());
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

                sensor.add(sensorsRef, sensor2,null);
                sensor.add(sensorsRef, sensor3,null);

                model.save(new KCallback() {
                    @Override
                    public void on(Object o) {
                        //done
                    }
                });

                long[] uuids = new long[]{sensor.uuid(), sensor2.uuid(), sensor3.uuid()};


                KCallback<KObject[]> jumped = new KCallback<KObject[]>(){
                    public void on(KObject[] kObjects) {
                        for (int i = 0; i < kObjects.length; i++) {
                            kObjects[i].setByName("value", System.currentTimeMillis());
                        }
                        model.save(null);
                    }
                };

                serviceExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        model.manager().lookupAllObjects(0, System.currentTimeMillis(), uuids, jumped);
                    }
                }, 5000, 5000, TimeUnit.MILLISECONDS);

            }
        });

        //WebSocketGateway gateway = WebSocketGateway.exposeModelAndResources(model, 8080, TemplateTest.class.getClassLoader());
        //gateway.start();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
