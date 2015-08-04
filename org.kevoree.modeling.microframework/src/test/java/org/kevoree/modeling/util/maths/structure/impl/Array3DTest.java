package org.kevoree.modeling.util.maths.structure.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.impl.HeapObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.util.maths.structure.KArray3D;

public class Array3DTest {

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
        KObjectChunk segment = new HeapObjectChunk(-1,-1,-1,null);
        segment.init(null, mm, mc.index());
        int nbLines = 5;
        int nbColumn = 3;
        int nbDeep = 2;
        //allocate for 5 elem
        segment.extendDoubleArray(mc.dependencies().index(), nbLines * nbColumn * nbDeep, mc);
        //attach a wrapper
        KArray3D array = new Array3D(nbLines, nbColumn, nbDeep, 0, mc.dependencies().index(), segment, mc);
        //fill it
        int count=0;
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                for (int h = 0; h < nbDeep; h++) {
                    array.set(i, j, h, count);
                    count++;
                }
            }
        }
        //test content
        count=0;
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                for (int h = 0; h < nbDeep; h++) {
                    Assert.assertTrue(array.get(i, j, h) == count);
                    Assert.assertTrue(segment.getDoubleArrayElem(mc.dependencies().index(), count, mc)==count);
                    count++;
                }
            }
        }
        //test raw array
        Assert.assertTrue(nbLines * nbColumn * nbDeep == segment.getDoubleArraySize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getDoubleArray(mc.dependencies().index(), mc);
        Assert.assertTrue(nbLines * nbColumn * nbDeep == copyArray.length);
    }

}
