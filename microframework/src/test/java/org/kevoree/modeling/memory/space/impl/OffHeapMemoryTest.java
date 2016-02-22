package org.kevoree.modeling.memory.space.impl;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

/**
 * @ignore ts
 */
public class OffHeapMemoryTest {

    @Test
    public void monoThreadFullTest() {

        int size = 1000;

        MetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.LONG);


        for (int iterations = 0; iterations < 100; iterations++) {
            KModel model = dynamicMetaModel.createModel(DataManagerBuilder.create().withSpace(new PressOffHeapChunkSpace(size * 4)).withScheduler(new DirectScheduler()).build());

            long start = System.currentTimeMillis();

            model.connect((c) -> {
                for (int i = 0; i < size; i++) {
                    try {

                        KObject object = model.universe(0).time(i).create(sensorMetaClass);
//                        object.set(object.metaClass().attribute("value"), 1);

                        for (int j = 0; j < 100; j++) {

                            model.lookup(0, i, object.uuid(), new KCallback<KObject>() {
                                @Override
                                public void on(KObject kObject) {
                                    //  System.out.println("Inserting " + ii + " done");
                                    kObject.set(kObject.metaClass().attribute("value"), 1);
                                }
                            });
                        }

                    } catch (Throwable t) {

                        System.out.println("probably never happens anyway!");
                        System.out.println(t);
                    }
                }
                long end = System.currentTimeMillis();
//                System.out.println(end - start + " ms");

            });
        }

//        PressOffHeapChunkSpace space = new PressOffHeapChunkSpace(4 * size);
//        //Fill the entire cache
//        for (int i = 0; i < size; i++) {
//            KChunk chunk = space.create(i, 0, 10, KChunkTypes.OBJECT_CHUNK,dynamicMetaModel);
//            chunk.setFlags(KChunkFlags.DIRTY_BIT, 0);
//        }
        //space.create(1, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(2, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(3, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(4, 1, 1, KChunkTypes.OBJECT_CHUNK);

//        for (int i = 0; i < 3; i++) {
//            space.get(i, 0, 10).inc();
//        }
//
//        for (int i = 0; i < 8; i++) {
//            space.create(i, 1, 1, KChunkTypes.OBJECT_CHUNK,null);
//        }

//        System.out.println(space);


    }

}
