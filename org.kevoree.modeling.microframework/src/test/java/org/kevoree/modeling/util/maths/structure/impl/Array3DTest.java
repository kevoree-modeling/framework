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
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.KArray3D;

public class Array3DTest {

    @Test
    public void test() {
        KMetaModel mm = new MetaModel("test");
        KMetaClass mc = mm.addInferMetaClass("infer_class", new KInferAlg() {
            @Override
            public void train(double[][] trainingSet, double[] expectedResultSet, KObject currentInferObject, KMetaDependencies meta) {

            }

            @Override
            public double[] infer(double[] features, KObject origin, KMetaDependencies meta) {
                return new double[0];
            }
        });
        KMemorySegment segment = new HeapMemorySegment();
        segment.initMetaClass(mc);
        segment.init(null, mm);
        int nbLines = 5;
        int nbColumn = 3;
        int nbDeep = 2;
        //allocate for 5 elem
        segment.extendInfer(mc.dependencies().index(), nbLines * nbColumn * nbDeep, mc);
        //attach a wrapper
        KArray3D array = new Array3D(nbLines, nbColumn, nbDeep, 0, mc.dependencies().index(), segment, mc);
        //fill it
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                for (int h = 0; h < nbDeep; h++) {
                    array.set(i, j, h, (i + j + h));
                }
            }
        }
        //test content
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                for (int h = 0; h < nbDeep; h++) {
                    Assert.assertTrue(array.get(i, j, h) == (i + j + h));
                }
            }
        }

        //test raw array
        Assert.assertTrue(nbLines * nbColumn * nbDeep == segment.getInferSize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getInfer(mc.dependencies().index(), mc);
        Assert.assertTrue(nbLines * nbColumn * nbDeep == copyArray.length);
    }

}
