package org.kevoree.modeling.meta;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

/**
 * Created by duke on 16/01/15.
 */
public class ReflexiveTest {

    @Test
    public void test() {

        MetaModel metaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = metaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.FLOAT);
        sensorMetaClass.addReference("siblings", sensorMetaClass, null, true);

        KMetaClass homeMetaClass = metaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addReference("sensors", sensorMetaClass, null, true);

        final KModel universe = metaModel.model();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                KObject home = universe.universe(0).time(0).create(universe.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                home.mutate(KActionType.ADD, (KMetaReference) home.metaClass().metaByName("sensors"), sensor);

                universe.universe(0).time(0).json().save(home, new KCallback<String>() {
                    @Override
                    public void on(String s) {

                    }
                });

            }
        });


    }

}
