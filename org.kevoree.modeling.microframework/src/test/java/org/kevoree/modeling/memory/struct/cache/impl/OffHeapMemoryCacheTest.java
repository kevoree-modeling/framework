package org.kevoree.modeling.memory.struct.cache.impl;

/** @ignore ts*/
public class OffHeapMemoryCacheTest {

    /*
    @Test
    public void putGetTest() {
        final KMetaModel dynamicMetaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = dynamicMetaModel.addMetaClass("Sensor");

        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.FLOAT);
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
                home.setPrimitiveType(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = model.universe(0).time(0).create(sensorMetaClass);
                sensor.setPrimitiveType(sensor.metaClass().attribute("name"), "Sensor#1");

                KObject sensor2 = model.universe(0).time(0).create(sensorMetaClass);
                sensor2.setPrimitiveType(sensor.metaClass().attribute("name"), "Sensor#2");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                // prepare cache element
                KMemorySegment entry = new OffHeapMemorySegment();
                entry.initMetaClass(homeMetaClass);
                entry.setPrimitiveType(homeMetaClass.attribute("attr_long").index(), 10l, homeMetaClass);
                entry.setPrimitiveType(homeMetaClass.attribute("name").index(), "test", homeMetaClass);

                entry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor.uuid(), homeMetaClass);
                entry.addLongToArray(homeMetaClass.reference("sensors").index(), sensor2.uuid(), homeMetaClass);

                KCache cache = new OffHeapMemoryCache();
                cache.putAndReplace(1, 1, 1, entry);

                KMemorySegment resolved = (KMemorySegment) cache.getPrimitiveType(1, 1, 1);
                long attr = (long) resolved.getPrimitiveType(homeMetaClass.attribute("attr_long").index(), homeMetaClass);
                Assert.assertEquals(10l, attr);
                String name = (String) resolved.getPrimitiveType(homeMetaClass.attribute("name").index(), homeMetaClass);
                Assert.assertEquals("test", name);

                Assert.assertEquals(sensor.uuid(), resolved.getLongArrayElem(homeMetaClass.reference("sensors").index(), 0, homeMetaClass));
                Assert.assertEquals(sensor2.uuid(), resolved.getLongArrayElem(homeMetaClass.reference("sensors").index(), 1, homeMetaClass));


            }
        });
    }
    */

}
