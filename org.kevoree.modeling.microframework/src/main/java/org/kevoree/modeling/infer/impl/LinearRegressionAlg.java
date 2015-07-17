package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

import java.util.Random;


public class LinearRegressionAlg implements KInferAlg {

    //TODO to replace by meta-learning parameters
    private double alpha=0.005; //learning rate
    private double gamma =0.000; //regularization parameter
    private int iterations=10; //iterations

    private static Random rand= new Random();
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

        for(int i=0;i<iterations;i++){
            for(int row=0;row<trainingSet.length;row++){
                double h=estimate(trainingSet[row],state);
                double error=-alpha*(h-expectedResultSet[row][0]);

                for (int feature = 0; feature < origin.metaClass().inputs().length; feature++) {
                    state.set(feature, state.get(feature) * (1 - alpha * gamma) + error * trainingSet[row][feature]);
                }
                state.add(origin.metaClass().inputs().length,error);
            }
        }
    }

    private double estimate(double[] training, Array1D state){
        double result=0;
        for(int i=0;i<training.length;i++){
            result=result+training[i]*state.get(i);
        }
        result=result+state.get(training.length);
        return result;
    }

    @Override
    public double[][] infer(double[][] features, KObject origin) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int size=origin.metaClass().inputs().length+1;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

        double[][] results = new double[features.length][1];
        for(int i=0;i<features.length;i++){
            results[i]=new double[1];
            results[i][0]=estimate(features[i],state);
        }
        return results;
    }
}
