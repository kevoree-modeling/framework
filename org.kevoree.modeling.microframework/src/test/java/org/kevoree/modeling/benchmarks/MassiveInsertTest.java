package org.kevoree.modeling.benchmarks;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

public class MassiveInsertTest {

    /**
     * @native ts
     */
    @Test
    public void test() {

        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass metaClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute attribute = metaClass.addAttribute("value", KPrimitiveTypes.LONG);
        KModel model = metaModel.model();


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
