package org.kevoree.modeling.util.maths.structure.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.impl.HeapObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.util.maths.structure.KArray1D;

public class Array1DTest {

    @Test
    public void test() {
        KMetaModel mm = new MetaModel("test");
        KMetaClass mc = mm.addInferMetaClass("infer_class", new KInferAlg() {
            @Override
            public void train(double[][] trainingSet, double[][] expectedResultSet, KObject currentInferObject, KInternalDataManager manager) {

            }

            @Override
            public double[][] infer(double[][] features, KObject currentInferObject, KInternalDataManager manager) {
                return new double[0][];
            }
        });
        KObjectChunk segment = new HeapObjectChunk();
        segment.init(null, mm, mc.index());
        int arraySize = 5;
        //allocate for 5 elem
        segment.extendDoubleArray(mc.dependencies().index(), arraySize, mc);
        //attach a wrapper
        KArray1D array = new Array1D(arraySize, 0, mc.dependencies().index(), segment, mc);
        //fill it
        int count=0;
        for (int i = 0; i < arraySize; i++) {
            array.set(i, count);
            count++;
        }
        //test content

        count=0;
        for (int i = 0; i < arraySize; i++) {
            Assert.assertTrue(array.get(i) == count);
            Assert.assertTrue(segment.getDoubleArrayElem(mc.dependencies().index(), count, mc)==count);
            count++;
        }
        //test raw array
        Assert.assertTrue(arraySize == segment.getDoubleArraySize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getDoubleArray(mc.dependencies().index(), mc);
        Assert.assertTrue(arraySize == copyArray.length);
        for (int i = 0; i < copyArray.length; i++) {
            Assert.assertTrue(copyArray[i] == i);
        }
    }

}
