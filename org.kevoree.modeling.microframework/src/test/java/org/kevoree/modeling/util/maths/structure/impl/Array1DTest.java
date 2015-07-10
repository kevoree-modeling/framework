package org.kevoree.modeling.util.maths.structure.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaDependencies;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.util.maths.structure.KArray1D;

public class Array1DTest {

    @Test
    public void test() {
        KMetaModel mm = new MetaModel("test");
        KMetaClass mc = mm.addInferMetaClass("infer_class", new KInferAlg() {

            @Override
            public void train(double[][] trainingSet, double[][] expectedResultSet, KObject currentInferObject) {

            }

            @Override
            public double[][] infer(double[][] features, KObject currentInferObject) {
                return new double[0][];
            }

        });
        KMemorySegment segment = new HeapMemorySegment();
        segment.initMetaClass(mc);
        segment.init(null, mm);
        int arraySize = 5;
        //allocate for 5 elem
        segment.extendInfer(mc.dependencies().index(), arraySize, mc);
        //attach a wrapper
        KArray1D array = new Array1D(arraySize, 0, mc.dependencies().index(), segment, mc);
        //fill it
        for (int i = 0; i < arraySize; i++) {
            array.set(i, i);
        }
        //test content
        for (int i = 0; i < arraySize; i++) {
            Assert.assertTrue(array.get(i) == i);
        }
        //test raw array
        Assert.assertTrue(arraySize == segment.getInferSize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getInfer(mc.dependencies().index(), mc);
        Assert.assertTrue(arraySize == copyArray.length);
        for (int i = 0; i < copyArray.length; i++) {
            Assert.assertTrue(copyArray[i] == i);
        }
    }

}
