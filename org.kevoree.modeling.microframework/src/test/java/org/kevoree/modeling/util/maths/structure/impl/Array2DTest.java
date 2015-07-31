package org.kevoree.modeling.util.maths.structure.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.HeapMemoryChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.util.maths.structure.KArray2D;

public class Array2DTest {

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
        KMemoryChunk segment = new HeapMemoryChunk();
        segment.initMetaClass(mc);
        segment.init(null, mm, -1);
        int nbLines = 5;
        int nbColumn = 3;
        //allocate for 5 elem
        segment.extendDoubleArray(mc.dependencies().index(), nbLines * nbColumn, mc);
        //attach a wrapper
        KArray2D array = new Array2D(nbLines, nbColumn, 0, mc.dependencies().index(), segment, mc);

        //fill it
        int count=0;

        //fill it
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                array.set(i, j, count);
                count++;
            }
        }
        //test content
        count=0;
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                Assert.assertTrue(array.get(i,j) == count);
                Assert.assertTrue(segment.getDoubleArrayElem(mc.dependencies().index(), count, mc)==count);
                count++;
            }
        }

        //test raw array
        Assert.assertTrue(nbLines * nbColumn == segment.getDoubleArraySize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getDoubleArray(mc.dependencies().index(), mc);
        Assert.assertTrue(nbLines * nbColumn == copyArray.length);
    }

}
