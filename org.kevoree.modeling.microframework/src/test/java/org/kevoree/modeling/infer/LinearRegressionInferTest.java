package org.kevoree.modeling.infer;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.infer.impl.LinearRegressionAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
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


        KMetaClass metaClassIris = metaModel.addMetaClass("House");
        metaClassIris.addAttribute("length", KPrimitiveTypes.DOUBLE);
        metaClassIris.addAttribute("width", KPrimitiveTypes.DOUBLE);
        metaClassIris.addAttribute("height", KPrimitiveTypes.DOUBLE);
        metaClassIris.addAttribute("numOfRooms", KPrimitiveTypes.DOUBLE);

        metaClassIris.addAttribute("price", KPrimitiveTypes.DOUBLE);

        KMetaClass inferGaussian = metaModel.addInferMetaClass("RegressionProfile", new LinearRegressionAlg());
        inferGaussian.addDependency("House", metaClassIris, null);

        inferGaussian.addInput("length", "@House | =length");
        inferGaussian.addInput("width", "@House | =width");
        inferGaussian.addInput("height", "@House | =height");
        inferGaussian.addInput("numOfRooms", "@House | =numOfRooms");

        inferGaussian.addOutput("type", KPrimitiveTypes.DOUBLE);

        return metaModel;
    }

    private double getPrice(double length, double width, double height, double rooms) {
        return 410*length+2*width+0.01*height+10*rooms-10;
    }

    private static Random rand=new Random();
    private static KMetaModel mm;
    private static KModel model;

    private int count=0;

    private KObject createHouse() {
        KObject house = model.createByName("House", 0, 0);
        double length=rand.nextDouble();
        double width=rand.nextDouble();
        double height=rand.nextDouble();
        double rooms=count;
        double price=getPrice(length, width, height, rooms);
        count++;

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
        model = mm.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObjectInfer regProfile = (KObjectInfer) model.createByName("RegressionProfile", 0, 0);

                int trainingSize=1000000;

                for (int i = 0; i < trainingSize; i++) {
                    KObject house=createHouse();

                    Object[] output=new Object[1];
                    output[0] = Double.parseDouble(house.getByName("price").toString());
                    regProfile.train(new KObject[]{house}, output, null);
                }

                //test
                final KObject[] test = new KObject[1];
                test[0]=createHouse();
                regProfile.infer(test, new KCallback<Object[]>() {
                    @Override
                    public void on(Object[] objects) {
                        double price=Double.parseDouble(test[0].getByName("price").toString());
                        double calcPrice=Double.parseDouble(objects[0].toString());

                        KMemorySegment ks = regProfile.manager().segment(0, 0, regProfile.uuid(), false, regProfile.metaClass(), null);


                    }
                });

            }


        });
    }

}
