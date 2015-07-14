package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

import java.util.Random;


public class BinaryPerceptronAlg implements KInferAlg {
    private int iterations=1;

    private Random rand =new Random();

    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject origin) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial segment if empty
        int size=origin.metaClass().inputs().length;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendInfer(origin.metaClass().dependencies().index(),size,origin.metaClass());
            for(int i=0;i<size;i++){
                ks.setInferElem(dependenciesIndex,i,rand.nextDouble(),origin.metaClass());
            }
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

    }

    @Override
    public double[][] infer(double[][] features, KObject currentInferObject) {
        return new double[0][];
    }
}
