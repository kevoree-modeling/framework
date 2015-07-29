package org.kevoree.modeling.memory.struct.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.ArrayList;


public abstract class BaseKMemoryChunkTest {

    public abstract KMemoryChunk createKMemoryChunk();

    @Test
    public void attributeTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // setPrimitiveType and read attributes
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                long attr = (long) cacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                Assert.assertEquals(10l, attr);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);
                String name = (String) cacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass);
                Assert.assertEquals("test", name);

                // add and remove attributes
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                long[] ref = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref, new long[]{sensor.uuid(), sensor2.uuid()});

                cacheEntry.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                long[] ref2 = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref2, new long[]{sensor2.uuid()});

                cacheEntry.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                long[] ref3 = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref3, null);

                // free cache entry
                cacheEntry.free(dynamicMetaModel);

            }
        });
    }

    @Test
    public void referenceTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // setPrimitiveType and read attributes
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");
                //System.out.println("Sensor#1: " + sensor.uuid());

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");
                //System.out.println("Sensor#2: " + sensor2.uuid());

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                cacheEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                // add and remove attributes
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                long[] ref = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref, new long[]{sensor.uuid(), sensor2.uuid()});

                Assert.assertEquals(2, cacheEntry.getLongArraySize(homeMetaClass.reference("sensors").index(), homeMetaClass));

                Assert.assertEquals(sensor.uuid(), cacheEntry.getLongArrayElem(homeMetaClass.reference("sensors").index(), 0, homeMetaClass));
                Assert.assertEquals(sensor2.uuid(), cacheEntry.getLongArrayElem(homeMetaClass.reference("sensors").index(), 1, homeMetaClass));

                cacheEntry.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                long[] ref2 = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref2, new long[]{sensor2.uuid()});

                Assert.assertEquals(1, cacheEntry.getLongArraySize(homeMetaClass.reference("sensors").index(), homeMetaClass));

                cacheEntry.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                long[] ref3 = cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                Assert.assertArrayEquals(ref3, null);

                // free cache entry
                cacheEntry.free(dynamicMetaModel);
            }
        });
    }


    @Test
    public void cloneTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // model
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                // cache entry
                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                cacheEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                // clone
                KMemoryChunk clonedEntry = cacheEntry.clone(homeMetaClass);

                Assert.assertEquals(cacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass),
                        clonedEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass));
                Assert.assertEquals(cacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass),
                        clonedEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass));
                Assert.assertArrayEquals(cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass),
                        clonedEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass));

                Assert.assertTrue(clonedEntry.isDirty());

                // free cache entry
                //cacheEntry.free(dynamicMetaModel);
                clonedEntry.free(dynamicMetaModel);

            }
        });
    }


    @Test
    public void freeMemoryTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // model
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                cacheEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                ArrayList<KMemoryChunk> segments = new ArrayList<KMemoryChunk>();
                for (int i = 0; i < 50; i++) {
                    segments.add(cacheEntry.clone(homeMetaClass));
                }

                // free everything
                KMemoryChunk[] loopSegment = segments.toArray(new KMemoryChunk[segments.size()]);
                for (int i = 0; i < loopSegment.length; i++) {
                    loopSegment[i].free(dynamicMetaModel);
                }

                // free cache entry
                cacheEntry.free(dynamicMetaModel);
            }
        });
    }

    @Test
    public void modifiedIndexesTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // setPrimitiveType and read attributes
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                Assert.assertFalse(cacheEntry.isDirty());

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                long attr = (long) cacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                Assert.assertEquals(10l, attr);

                Assert.assertTrue(cacheEntry.isDirty());

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);
                String name = (String) cacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass);
                Assert.assertEquals("test", name);

                // free cache entry
                cacheEntry.free(dynamicMetaModel);

            }
        });
    }

    @Test
    public void inferTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensorMetaClass.attribute("name"), "Sensor#1");

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(sensorMetaClass);

                Assert.assertFalse(cacheEntry.isDirty());

                double[] inferPayload0 = cacheEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNull(inferPayload0);

                cacheEntry.extendDoubleArray(sensorMetaClass.attribute("value").index(), 1, sensorMetaClass);
                Assert.assertTrue(cacheEntry.isDirty());

                double[] inferPayload1 = cacheEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload1);
                //Assert.assertEquals(1,cacheEntry.getDoubleArraySize(sensorMetaClass.attribute("value").index(), sensorMetaClass));

                cacheEntry.setDoubleArrayElem(sensorMetaClass.attribute("value").index(), 0, 42, sensorMetaClass);
                double[] inferPayload2 = cacheEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload2);
                Assert.assertEquals(1, cacheEntry.getDoubleArraySize(sensorMetaClass.attribute("value").index(), sensorMetaClass));

                Assert.assertTrue(inferPayload2[0] == 42);


                cacheEntry.extendDoubleArray(sensorMetaClass.attribute("value").index(), 10, sensorMetaClass);
                double[] inferPayload3 = cacheEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload3);
                // Assert.assertTrue(inferPayload3.length == 10);
                Assert.assertTrue(inferPayload3[0] == 42);


                cacheEntry.setDoubleArrayElem(sensorMetaClass.attribute("value").index(), 9, 52, sensorMetaClass);
                double[] inferPayload4 = cacheEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertTrue(inferPayload4[9] == 52);
                Assert.assertTrue(inferPayload3[0] == 42);


                // clone cache entry
                KMemoryChunk clone = cacheEntry.clone(sensorMetaClass);
                double[] inferPayload5 = clone.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                double[] inferPayload6 = clone.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertTrue(inferPayload5[9] == 52);
                Assert.assertTrue(inferPayload6[0] == 42);

                // free cache entry
                cacheEntry.free(dynamicMetaModel);
                clone.free(dynamicMetaModel);
            }
        });
    }

    @Test
    public void serializationTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = dynamicMetaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("attr_long", KPrimitiveTypes.LONG);
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);
        homeMetaClass.addAttribute("value", KPrimitiveTypes.CONTINUOUS);


        final KModel model = dynamicMetaModel.model();

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // setPrimitiveType and read attributes
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                KMemoryChunk cacheEntry = createKMemoryChunk();
                cacheEntry.initMetaClass(homeMetaClass);

                Assert.assertFalse(cacheEntry.isDirty());

                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                cacheEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                cacheEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                long attr = (long) cacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                Assert.assertEquals(10l, attr);

                Assert.assertEquals(cacheEntry.getDoubleArraySize(homeMetaClass.attribute("value").index(), homeMetaClass), 0);
                cacheEntry.extendDoubleArray(homeMetaClass.attribute("value").index(), 3, homeMetaClass);
                //Assert.assertEquals(cacheEntry.getDoubleArraySize(homeMetaClass.attribute("value").index(), homeMetaClass), 3);
                Assert.assertTrue(cacheEntry.isDirty());

                cacheEntry.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 0, 0.1, homeMetaClass);
                cacheEntry.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 1, 1.1, homeMetaClass);
                cacheEntry.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 2, 2.1, homeMetaClass);

                Assert.assertEquals("{\"attr_long\":\"U\",\"sensors\":[\"E\",\"G\"],\"value\":[\"P7JmZmZmZma\",\"P/BmZmZmZma\",\"QAAzMzMzMzN\"]}", cacheEntry.serialize(dynamicMetaModel));

                KMemoryChunk newCacheEntry = createKMemoryChunk();
                newCacheEntry.initMetaClass(homeMetaClass);

                try {
                    String serialized = cacheEntry.serialize(dynamicMetaModel);
                    newCacheEntry.init(serialized, dynamicMetaModel);

                    String newSeriliazed = newCacheEntry.serialize(dynamicMetaModel);
                    Assert.assertEquals(serialized, newSeriliazed);

                    Assert.assertEquals(cacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass),
                            newCacheEntry.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass));
                    //Assert.assertEquals(cacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass),
                    //        newCacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass));

                    Assert.assertArrayEquals(cacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass),
                            newCacheEntry.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass));

                    double[] originInfer = cacheEntry.getDoubleArray(homeMetaClass.attribute("value").index(), homeMetaClass);
                    double[] newInfer = newCacheEntry.getDoubleArray(homeMetaClass.attribute("value").index(), homeMetaClass);

                    Assert.assertEquals(originInfer.length, newInfer.length);
                    Assert.assertFalse(newCacheEntry.isDirty());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // free cache entry
                cacheEntry.free(dynamicMetaModel);
                newCacheEntry.free(dynamicMetaModel);

            }
        });
    }

}
