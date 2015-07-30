package org.kevoree.modeling;

import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.impl.DataManager;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.openjdk.jmh.annotations.Benchmark;

public class MassiveInsertTest {

    @Benchmark
    public void test() {

        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass metaClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute attribute = metaClass.addAttribute("value", KPrimitiveTypes.LONG);
        KModel model = metaModel.createModel(DataManagerBuilder.buildDefault());


        model.connect(new KCallback() {
            @Override
            public void on(Object o) {

                KObject sensor = model.create(metaClass, 0, 0);
                long before = System.currentTimeMillis();
                for (int i = 0; i < 1000000; i++) {

                    //if(i % 1000000==0){ System.err.println(i); }

                    sensor.jump(i, new KCallback<KObject>() {
                        @Override
                        public void on(KObject timedObject) {
                            timedObject.set(attribute, System.currentTimeMillis());
                        }
                    });
                }
                long after = System.currentTimeMillis();
                System.out.println((after - before) + " ms");

            }
        });

    }

}
