package org.kevoree.modeling.memory.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.ArrayList;

public abstract class BaseKObjectChunkTest {

    public abstract KObjectChunk createKObjectChunk();

    public abstract KInternalDataManager createKInternalDataManger();

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

        final KModel model = dynamicMetaModel.createModel(createKInternalDataManger());

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

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {

                        KObjectChunk objectChunk = createKObjectChunk();
                        objectChunk.init(null, dynamicMetaModel, homeMetaClass.index());

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        long attr = (long) objectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                        Assert.assertEquals(10l, attr);

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);
                        String name = (String) objectChunk.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass);
                        Assert.assertEquals("test", name);

                        // add and remove attributes
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                        long[] ref = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref, new long[]{sensor.uuid(), sensor2.uuid()});

                        objectChunk.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        long[] ref2 = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref2, new long[]{sensor2.uuid()});

                        objectChunk.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                        long[] ref3 = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref3, null);

                        // free object chunk
                        objectChunk.free(dynamicMetaModel);

                    }
                });


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

        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

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

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {

                        KObjectChunk objectChunk = createKObjectChunk();
                        objectChunk.init(null, dynamicMetaModel, homeMetaClass.index());

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        objectChunk.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                        // add and remove attributes
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                        long[] ref = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref, new long[]{sensor.uuid(), sensor2.uuid()});

                        Assert.assertEquals(2, objectChunk.getLongArraySize(homeMetaClass.reference("sensors").index(), homeMetaClass));

                        Assert.assertEquals(sensor.uuid(), objectChunk.getLongArrayElem(homeMetaClass.reference("sensors").index(), 0, homeMetaClass));
                        Assert.assertEquals(sensor2.uuid(), objectChunk.getLongArrayElem(homeMetaClass.reference("sensors").index(), 1, homeMetaClass));

                        objectChunk.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        long[] ref2 = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref2, new long[]{sensor2.uuid()});

                        Assert.assertEquals(1, objectChunk.getLongArraySize(homeMetaClass.reference("sensors").index(), homeMetaClass));

                        objectChunk.removeLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);
                        long[] ref3 = objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass);
                        Assert.assertArrayEquals(ref3, null);

                        // free object chunk
                        objectChunk.free(dynamicMetaModel);

                    }
                });


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

        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // createModel
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {

                        // object chunk
                        KObjectChunk objectChunk = createKObjectChunk();
                        objectChunk.init(null, dynamicMetaModel, homeMetaClass.index());

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        objectChunk.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                        // clone
                        KObjectChunk clonedChunk = objectChunk.clone(-1, -1, -1, dynamicMetaModel);

                        Assert.assertEquals(objectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass),
                                clonedChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass));
                        Assert.assertEquals(objectChunk.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass),
                                clonedChunk.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass));
                        Assert.assertArrayEquals(objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass),
                                clonedChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass));

                        Assert.assertTrue((clonedChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        // free object chunk
                        //objectChunk.free(dynamicMetaModel);
                        clonedChunk.free(dynamicMetaModel);

                    }
                });


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

        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                // createModel
                KObject home = model.universe(0).time(0).create(model.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.set(sensor.metaClass().attribute("name"), "Sensor#2");

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {
                        KObjectChunk objectChunkEntry = createKObjectChunk();
                        objectChunkEntry.init(null, dynamicMetaModel, homeMetaClass.index());

                        objectChunkEntry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        objectChunkEntry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                        objectChunkEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        objectChunkEntry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                        ArrayList<KObjectChunk> chunks = new ArrayList<KObjectChunk>();
                        for (int i = 0; i < 50; i++) {
                            chunks.add(objectChunkEntry.clone(-1, -1, -1, dynamicMetaModel));
                        }

                        // free everything
                        KObjectChunk[] loopChunks = chunks.toArray(new KObjectChunk[chunks.size()]);
                        for (int i = 0; i < loopChunks.length; i++) {
                            loopChunks[i].free(dynamicMetaModel);
                        }

                        // free object chunk
                        objectChunkEntry.free(dynamicMetaModel);
                    }
                });


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

        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

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

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {
                        KObjectChunk objectChunk = createKObjectChunk();
                        objectChunk.init(null, dynamicMetaModel, homeMetaClass.index());

                        Assert.assertFalse((objectChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        long attr = (long) objectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                        Assert.assertEquals(10l, attr);

                        Assert.assertTrue((objectChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);
                        String name = (String) objectChunk.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass);
                        Assert.assertEquals("test", name);

                        // free object chunk
                        objectChunk.free(dynamicMetaModel);
                    }
                });



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

        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensorMetaClass.attribute("name"), "Sensor#1");

                KObjectChunk objectChunkEntry = createKObjectChunk();
                objectChunkEntry.init(null, dynamicMetaModel, sensorMetaClass.index());

                Assert.assertFalse((objectChunkEntry.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                double[] inferPayload0 = objectChunkEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNull(inferPayload0);

                objectChunkEntry.extendDoubleArray(sensorMetaClass.attribute("value").index(), 1, sensorMetaClass);
                Assert.assertTrue((objectChunkEntry.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                double[] inferPayload1 = objectChunkEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload1);
                //Assert.assertEquals(1,cacheEntry.getDoubleArraySize(sensorMetaClass.attribute("value").index(), sensorMetaClass));

                objectChunkEntry.setDoubleArrayElem(sensorMetaClass.attribute("value").index(), 0, 42, sensorMetaClass);
                double[] inferPayload2 = objectChunkEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload2);
                Assert.assertEquals(1, objectChunkEntry.getDoubleArraySize(sensorMetaClass.attribute("value").index(), sensorMetaClass));

                Assert.assertTrue(inferPayload2[0] == 42);


                objectChunkEntry.extendDoubleArray(sensorMetaClass.attribute("value").index(), 10, sensorMetaClass);
                double[] inferPayload3 = objectChunkEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertNotNull(inferPayload3);
                // Assert.assertTrue(inferPayload3.length == 10);
                Assert.assertTrue(inferPayload3[0] == 42);


                objectChunkEntry.setDoubleArrayElem(sensorMetaClass.attribute("value").index(), 9, 52, sensorMetaClass);
                double[] inferPayload4 = objectChunkEntry.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertTrue(inferPayload4[9] == 52);
                Assert.assertTrue(inferPayload3[0] == 42);


                // clone object chunk
                KObjectChunk clone = objectChunkEntry.clone(-1, -1, -1, dynamicMetaModel);
                double[] inferPayload5 = clone.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                double[] inferPayload6 = clone.getDoubleArray(sensorMetaClass.attribute("value").index(), sensorMetaClass);
                Assert.assertTrue(inferPayload5[9] == 52);
                Assert.assertTrue(inferPayload6[0] == 42);

                // free object chunk
                objectChunkEntry.free(dynamicMetaModel);
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


        final KModel model = dynamicMetaModel.createModel(DataManagerBuilder.buildDefault());

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

                home.addByName("sensors", sensor, new KCallback() {
                    @Override
                    public void on(Object o) {
                        KObjectChunk objectChunk = createKObjectChunk();
                        objectChunk.init(null, dynamicMetaModel, homeMetaClass.index());

                        Assert.assertFalse((objectChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                        objectChunk.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                        objectChunk.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                        long attr = (long) objectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                        Assert.assertEquals(10l, attr);

                        Assert.assertEquals(objectChunk.getDoubleArraySize(homeMetaClass.attribute("value").index(), homeMetaClass), 0);
                        objectChunk.extendDoubleArray(homeMetaClass.attribute("value").index(), 3, homeMetaClass);
                        //Assert.assertEquals(cacheEntry.getDoubleArraySize(homeMetaClass.attribute("value").index(), homeMetaClass), 3);
                        Assert.assertTrue((objectChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        objectChunk.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 0, 0.1, homeMetaClass);
                        objectChunk.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 1, 1.1, homeMetaClass);
                        objectChunk.setDoubleArrayElem(homeMetaClass.attribute("value").index(), 2, 2.1, homeMetaClass);

                        Assert.assertEquals("{\"attr_long\":\"U\",\"sensors\":[\"E\",\"G\"],\"value\":[\"P7JmZmZmZma\",\"P/BmZmZmZma\",\"QAAzMzMzMzN\"]}", objectChunk.serialize(dynamicMetaModel));

                        KObjectChunk newObjectChunk = createKObjectChunk();
                        try {
                            String serialized = objectChunk.serialize(dynamicMetaModel);
                            newObjectChunk.init(serialized, dynamicMetaModel, homeMetaClass.index());

                            String newSeriliazed = newObjectChunk.serialize(dynamicMetaModel);
                            Assert.assertEquals(serialized, newSeriliazed);

                            Assert.assertEquals(objectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass),
                                    newObjectChunk.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass));
                            //Assert.assertEquals(cacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass),
                            //        newCacheEntry.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass));

                            Assert.assertArrayEquals(objectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass),
                                    newObjectChunk.getLongArray(homeMetaClass.reference("sensors").index(), homeMetaClass));

                            double[] originInfer = objectChunk.getDoubleArray(homeMetaClass.attribute("value").index(), homeMetaClass);
                            double[] newInfer = newObjectChunk.getDoubleArray(homeMetaClass.attribute("value").index(), homeMetaClass);

                            Assert.assertEquals(originInfer.length, newInfer.length);
                            Assert.assertFalse((newObjectChunk.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // free object chunk
                        objectChunk.free(dynamicMetaModel);
                        newObjectChunk.free(dynamicMetaModel);
                    }
                });



            }
        });
    }

}
