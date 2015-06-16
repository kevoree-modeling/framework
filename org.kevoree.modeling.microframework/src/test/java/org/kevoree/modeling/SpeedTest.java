package org.kevoree.modeling;

//import net.openhft.chronicle.map.ChronicleMapBuilder;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

/**
 * Created by duke on 29/04/15.
 */
public class SpeedTest {

    /**
     * @native ts
     */
    // @Test
    /*
    public void test2() {
        try {
            long before = System.currentTimeMillis();
            Object[] hello = new Object[10];
            //for (int j = 0; j < 5; j++) {
            OOKLongTree tree = new OOKLongTree();
            //LongHashMap helloMap = new LongHashMap(16, KConfig.CACHE_LOAD_FACTOR);

            HashMap helloMap = new HashMap();

           // String tmp = System.getProperty("java.io.tmpdir");
           // String pathname = tmp + "/shm-test/myfile.dat";

            File file = File.createTempFile("chronicle","dat");

            ChronicleMapBuilder<Integer, CharSequence> builder =
                    ChronicleMapBuilder.of(Integer.class, CharSequence.class).entries(5000000);
            ConcurrentMap<Integer, CharSequence> map = builder.createPersistedTo(file);


            for (int i = 0; i < 5000000; i++) {
                Object[] hello2 = new Object[10];

                //boolean[] indexes = new boolean[10];
                String indexes = new String("test");

                hello2[0] = 3;

                tree.insert(i);
                tree.previousOrEqual(i + 1);
                //KObject hello = new DynamicKObject(0,i,3,null,null);
                //helloMap.put(i, indexes);

               map.put(i, indexes);


           // if (i % 10000 == 0) {
           //     helloMap.clear();
           // }

            }
            // }
            long after = System.currentTimeMillis();
            System.out.println(after - before);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    /**
     * @native ts
     */
    // @Test
    public void test() {
        MetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);
        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);
        final KModel universe = dynamicMetaModel.model();

        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                //  universe.manager().setScheduler(new ExecutorServiceScheduler());

                KObject home = universe.universe(0).time(0).create(universe.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                long before = System.currentTimeMillis();
                KMetaAttribute att = sensor.metaClass().attribute("value");
                //   att.setExtrapolation(new PolynomialExtrapolation());
                ((MetaAttribute) att)._precision = 0.1;

                for (int i = 0; i < 5000000; i++) {
                    sensor.jump(i, new KCallback<KObject>() {
                        @Override
                        public void on(KObject timedObject) {
                            timedObject.set(att, 3d);
                        }
                    });
                }
                long middle = System.currentTimeMillis();
                /*
                for(int i=0;i<5000000;i++){
                    sensor.jump2(i, new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            kObject.get(att);
                        }
                    });
                }
                */
                long after = System.currentTimeMillis();
                System.out.println(middle - before);
                System.out.println(after - middle);

            }
        });

    }

}
