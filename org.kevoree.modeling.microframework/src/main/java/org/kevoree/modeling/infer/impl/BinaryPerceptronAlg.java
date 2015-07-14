package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

import java.util.Random;


public class BinaryPerceptronAlg implements KInferAlg {
    private int iterations=20;

    private Random rand =new Random();

    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject origin) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial segment if empty
        int size=origin.metaClass().inputs().length+1;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendInfer(origin.metaClass().dependencies().index(),size,origin.metaClass());
            for(int i=0;i<size;i++){
                ks.setInferElem(dependenciesIndex,i,rand.nextDouble(),origin.metaClass());
            }
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

        for(int iter=0; iter<iterations; iter++){
            for(int i=0;i<trainingSet.length;i++){
                double a= calculate(trainingSet[i],state);
                if(a==0){
                    a=-1;
                }
                if(a*expectedResultSet[i][0]<=0){
                    for(int j=0; j<origin.metaClass().inputs().length;j++){
                        state.add(j,expectedResultSet[i][0]*trainingSet[i][j]);
                    }
                    state.add(origin.metaClass().inputs().length,expectedResultSet[i][0]);
                }
            }
        }

    }

    private double calculate(double[] features, Array1D state) {
        double res=0;

        for(int i=0; i<features.length;i++){
            res = res + state.get(i)*features[i];
        }
        res = res + state.get(features.length);
        if(res>=0){
            return 1.0;
        }
        else {
            return 0;
        }
    }

    @Override
    public double[][] infer(double[][] features, KObject origin) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index()+1;
        int size=origin.metaClass().inputs().length;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

        double[][] result = new double[features.length][1];

        for(int inst=0;inst<features.length;inst++){
            result[inst]=new double[1];
            result[inst][0]=calculate(features[inst],state);
        }
        return result;
    }
}
