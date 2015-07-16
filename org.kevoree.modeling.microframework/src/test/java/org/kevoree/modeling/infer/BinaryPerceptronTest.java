package org.kevoree.modeling.infer;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.infer.impl.BinaryPerceptronAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.Random;

/**
 * Created by assaad on 15/07/15.
 */
public class BinaryPerceptronTest {
        private KMetaModel createMetaModel() {
            KMetaModel metaModel = new MetaModel("PerceptronClassificationTestMM");


            KMetaClass metaClassPerson= metaModel.addMetaClass("Person");
            metaClassPerson.addAttribute("height", KPrimitiveTypes.DOUBLE);
            metaClassPerson.addAttribute("weight", KPrimitiveTypes.DOUBLE);

            KMetaEnum menaEnumHealthy = metaModel.addMetaEnum("BMI");
            menaEnumHealthy.addLiteral("HEALTHY");
            menaEnumHealthy.addLiteral("UNHEALTHY");


            metaClassPerson.addAttribute("healthy", menaEnumHealthy);

            KMetaClass PerceptronClassification= metaModel.addInferMetaClass("PerceptronProfile", new BinaryPerceptronAlg());
            PerceptronClassification.addDependency("Person", metaClassPerson, null);


            PerceptronClassification.addInput("weight", "@Person | =weight");
            PerceptronClassification.addInput("height", "@Person | =height^2");

            PerceptronClassification.addOutput("bmi", menaEnumHealthy);

            return metaModel;
        }


    private Random rand=new Random();
    int h=0;
    int uh=0;

    private KObject createPerson(KModel model, KMetaModel mm ){
        KObject personInstance = model.createByName("Person", 0, 0);
        double weight= rand.nextDouble()*100+40; //random weight from 40 to 140 kg
        double height= rand.nextDouble()*1.5+1; //random height from 1 to 2.5 kg
        boolean healthy =(weight/(height*height))<25;

        personInstance.setByName("weight",weight);
        personInstance.setByName("height",height);
        if(healthy){
            personInstance.setByName("healthy",((KMetaEnum) mm.metaTypeByName("BMI")).literalByName("HEALTHY"));
            h++;
        }
        else{
            personInstance.setByName("healthy",((KMetaEnum) mm.metaTypeByName("BMI")).literalByName("UNHEALTHY"));
            uh++;
        }

        return personInstance;

    }

    @Test
    public void test2(){
        double result=0;
        for(int i=0;i<10;i++){
            result=result+test();
        }
        result=result/10;
        //System.out.println("h: "+h+" uh: "+uh);
        //System.out.println(result);
        Assert.assertTrue(result>75);
    }

    public int test() {

        KMetaModel mm = createMetaModel();
        KModel model = mm.model();
        final int[] correct=new int[1];
        correct[0]=0;

        model.connect(new KCallback() {
            @Override
            public void on(Object o) {

                KObjectInfer perceptronProfile = (KObjectInfer) model.createByName("PerceptronProfile", 0, 0);
                int size=10000;

                for (int i = 0; i < size; i++) {
                    KObject[] person = new KObject[1];
                    person[0]=createPerson(model,mm);

                    Object[] output = new Object[1];
                    output[0] = person[0].getByName("healthy");
                    perceptronProfile.train(person,output,null);
                }


                for(int i=0;i<100;i++) {
                    KObject[] testPerson = new KObject[1];
                    testPerson[0] = createPerson(model, mm);
                    perceptronProfile.infer(testPerson, new KCallback<Object[]>() {
                        @Override
                        public void on(Object[] objects) {
                            if(objects[0] == ((MetaLiteral) testPerson[0].getByName("healthy"))){
                                correct[0]++;
                            }
                        }
                    });
                }
                //System.out.println(correct[0]);
                //Assert.assertTrue(correct[0]>50);
            }
        });
        return correct[0];
    }
}
