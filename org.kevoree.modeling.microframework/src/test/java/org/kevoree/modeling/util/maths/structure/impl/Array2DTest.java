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
import org.kevoree.modeling.util.maths.structure.KArray2D;

public class Array2DTest {

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
        int nbLines = 5;
        int nbColumn = 3;
        //allocate for 5 elem
        segment.extendInfer(mc.dependencies().index(), nbLines * nbColumn, mc);
        //attach a wrapper
        KArray2D array = new Array2D(nbLines, nbColumn, 0, mc.dependencies().index(), segment, mc);
        //fill it
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                array.set(i, j, i * j);
            }
        }
        //test content
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumn; j++) {
                Assert.assertTrue(array.get(i,j) == i*j);
            }
        }

        //test raw array
        Assert.assertTrue(nbLines * nbColumn == segment.getInferSize(mc.dependencies().index(), mc));
        double[] copyArray = segment.getInfer(mc.dependencies().index(), mc);
        Assert.assertTrue(nbLines * nbColumn == copyArray.length);
    }

}
