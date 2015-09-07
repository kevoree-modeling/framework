package org.kevoree.modeling.spaceship;

import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;

public class SpaceshipDemoServer {

    public static void main(String[] args) {

        KMetaModel metaModel = new MetaModel("SpaceShipDemoMetaModel");
        KMetaClass sensorMetaClass = metaModel.addMetaClass("Sensor");
        sensorMetaClass.addRelation("connectedNodes", sensorMetaClass, null);



    }

}
