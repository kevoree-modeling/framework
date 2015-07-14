package org.kevoree.modeling.infer.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.impl.Array1D;

public class KMeanClusterAlg implements KInferAlg {

    //TODO to replace by meta-learning parameters
    private int k=3; //number of clusters
    private int iterations=100;

    @Override
    public void train(double[][] trainingSet, double[][] expectedResultSet, KObject origin) {
        if(trainingSet.length<k){
            throw new RuntimeException("training set not enough");
        }
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index();
        //Create initial segment if empty
        int size=k*origin.metaClass().inputs().length;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            ks.extendInfer(origin.metaClass().dependencies().index(),size,origin.metaClass());

            //Start by selecting first K points as centroids
            for(int i=0;i<k;i++){
                for(int j=0;j<origin.metaClass().inputs().length;j++) {
                    ks.setInferElem(dependenciesIndex, j + i * origin.metaClass().inputs().length, trainingSet[i][j], origin.metaClass());
                }
            }
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

        for(int iter=0;iter<iterations;iter++) {
            int temporalClassification;
            double[][] centroids = new double[k][origin.metaClass().inputs().length];
            int[] counters=new int[k];

            for(int j=0;j<k;j++){
                centroids[j]=new double[origin.metaClass().inputs().length];
                counters[j]=0;
            }

            for(int i=0;i<trainingSet.length;i++){
                //Step 1, classify according to current centroids
                temporalClassification=classify(trainingSet[i],state);

                //Step 2 update the centroids in live
                for(int j=0;j<origin.metaClass().inputs().length;j++){
                    centroids[temporalClassification][j]+=trainingSet[i][j];
                }
                counters[temporalClassification]++;
            }

            //Step 3 replace the current state by the new centroids
            for(int i=0;i<k;i++) {
                for (int j = 0; j < origin.metaClass().inputs().length; j++) {
                    state.set(j + i * origin.metaClass().inputs().length,centroids[i][j]/counters[i]);
                }
            }

        }
    }

    private int classify(double[] features, Array1D state) {
        double maxdistance = -1;
        int classNum=-1;
        for(int i=0;i<k;i++){
            double currentdist=0;
            for(int j=0;j<features.length;j++){
                currentdist+=(features[j]-state.get(i*features.length+j))*(features[j]-state.get(i*features.length+j));
            }
            if(maxdistance<0){
                maxdistance=currentdist;
                classNum=i;
            }
            else{
                if(currentdist<maxdistance){
                    maxdistance=currentdist;
                    classNum=i;
                }
            }
        }
        return classNum;
    }

    @Override
    public double[][] infer(double[][] features, KObject origin) {
        KMemorySegment ks = origin.manager().segment(origin.universe(), origin.now(), origin.uuid(), false, origin.metaClass(), null);
        int dependenciesIndex = origin.metaClass().dependencies().index();
        int size=k*origin.metaClass().inputs().length;
        if (ks.getInferSize(dependenciesIndex, origin.metaClass()) == 0) {
            return null;
        }
        Array1D state = new Array1D(size,0,origin.metaClass().dependencies().index(),ks,origin.metaClass());

        double[][] result = new double[features.length][1];

        for(int inst=0;inst<features.length;inst++){
            result[inst]=new double[1];
            result[inst][0]=classify(features[inst],state);
        }
        return result;
    }
}
