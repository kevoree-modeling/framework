package org.kevoree.modeling.infer;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.infer.impl.LinearRegressionAlg;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.Random;

/**
 * Created by assaad on 10/07/15.
 */
public class LinearRegressionInferTest {
    private KMetaModel createMetaModel() {
        KMetaModel metaModel = new MetaModel("GaussianClassificationTestMM");


        KMetaClass metaClassHouse = metaModel.addMetaClass("House");
        metaClassHouse.addAttribute("length", KPrimitiveTypes.DOUBLE);
        metaClassHouse.addAttribute("width", KPrimitiveTypes.DOUBLE);
        metaClassHouse.addAttribute("height", KPrimitiveTypes.DOUBLE);
        metaClassHouse.addAttribute("numOfRooms", KPrimitiveTypes.DOUBLE);
        metaClassHouse.addAttribute("price", KPrimitiveTypes.DOUBLE);



        KMetaClass regressionProfile = metaModel.addInferMetaClass("RegressionProfile", new LinearRegressionAlg());
        regressionProfile.addDependency("House", metaClassHouse, null);


        regressionProfile.addInput("length", "@House | =length");
        regressionProfile.addInput("width", "@House | =width");
        regressionProfile.addInput("height", "@House | =height");
        regressionProfile.addInput("numOfRooms", "@House | =numOfRooms");

        regressionProfile.addOutput("price", KPrimitiveTypes.DOUBLE);

        return metaModel;
    }

    private double getPrice(double length, double width, double height, double rooms) {
        return 13*length+21*width+0.01*height+15*rooms-20;
    }

    private static Random rand=new Random();
    private static KMetaModel mm;
    private static KModel model;



    private KObject createHouse() {
        KObject house = model.createByName("House", 0, 0);
        double length=rand.nextDouble();
        double width=rand.nextDouble();
        double height=rand.nextDouble();
        double rooms=rand.nextDouble();
        double price=getPrice(length, width, height, rooms);


        house.setByName("length", length);
        house.setByName("width", width) ;
        house.setByName("height", height );
        house.setByName("numOfRooms", rooms);
        house.setByName("price", price);
        return house;
    }

    @Test
    public void test() {
        mm = createMetaModel();
        model = mm.createModel(DataManagerBuilder.buildDefault());
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObjectInfer regProfile = (KObjectInfer) model.createByName("RegressionProfile", 0, 0);

                int trainingSize=10000;

                for (int i = 0; i < trainingSize; i++) {
                    KObject house=createHouse();

                    Object[] output=new Object[1];
                    output[0] = Double.parseDouble(house.getByName("price").toString());
                    regProfile.genericTrain(new KObject[]{house}, output, null);
                }

                //test
                final KObject[] test = new KObject[1];
                test[0]=createHouse();
                regProfile.genericInfer(test, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        double price = Double.parseDouble(test[0].getByName("price").toString());
                        double calcPrice = Double.parseDouble(objects[0].toString());

                        Assert.assertTrue(Math.abs(price - calcPrice) < 10);
                        //    KMemorySegment ks = regProfile.manager().segment(0, 0, regProfile.uuid(), false, regProfile.metaClass(), null);
                    }
                });

            }


        });
    }

}
