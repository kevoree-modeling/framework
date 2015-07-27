package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

import java.util.ArrayList;

public class MomentMatcher {


    public static void matchMoments(SampleModel distribution) {
        // Array of covariance matrices of components
        SimpleMatrix[] smCovariances = distribution.getSubCovariances();
        // Array of mean vectors of components
        SimpleMatrix[] smMeans = distribution.getSubMeans();

        // Array of component weights of components
        double[] smWeights = distribution.getSubWeights();

        // if the given distribution has only one component
        // just return empty covariance
        if (smWeights.length == 0) {
            return;
        }
        if (smWeights.length == 1) {
            SimpleMatrix newMean = smMeans[0];
            SimpleMatrix newCovariance = null;
            if (smCovariances.length > 0)
                newCovariance = smCovariances[0];
            distribution.setGlobalCovariance(newCovariance);
            distribution.setGlobalMean(newMean);
            distribution.setGlobalWeight(smWeights[0]);
            return;
        }

        // calculate new weight
        double newWeight = 0;
        for (int i=0; i<smWeights.length;i++) {
            newWeight += smWeights[i];
        }
        // calculate new mean vector
        SimpleMatrix newMean = new SimpleMatrix(smMeans[0].numRows(), smMeans[0].numCols());
        for (int i = 0; i < smMeans.length; i++) {
            newMean = newMean.plus((smMeans[i].scale(smWeights[i])));
        }
        newMean = newMean.scale(1 / newWeight);
        // calculate new covariance matrix
        SimpleMatrix newCovariance = new SimpleMatrix(smCovariances[0].numRows(), smCovariances[0].numCols());
        for (int i = 0; i < smCovariances.length; i++) {
            SimpleMatrix dyadSmMean = smMeans[i].mult(smMeans[i].transpose());
            SimpleMatrix S = smCovariances[i].plus(dyadSmMean);
            newCovariance = newCovariance.plus(S.scale(smWeights[i]));
        }
        newCovariance = newCovariance.scale(1 / newWeight);
        SimpleMatrix dyadNewMean = newMean.mult(newMean.transpose());
        newCovariance = newCovariance.minus(dyadNewMean);
        //System.out.println("matching moments");
        //System.out.println(newCovariance);
        //System.out.println(newMean);
        // set calculated parameters to distribution
        distribution.setGlobalCovariance(newCovariance);
        distribution.setGlobalMean(newMean);
        distribution.setGlobalWeight(newWeight);
    }

    public static void matchMoments2Comp(TwoComponentDistribution distribution)  {
        // Array of covariance matrices of components
        SimpleMatrix[] smCovariances = distribution.getSubCovariances();
        // Array of mean vectors of components
        SimpleMatrix[] smMeans = distribution.getSubMeans();

        // Array of component weights of components
        Double[] smWeights = distribution.getSubWeights();

        // if the given distribution has only one component
        // just return empty covariance
        if (smWeights.length == 0) {
            return;
        }
        if (smWeights.length == 1) {
            SimpleMatrix newMean = smMeans[0];
            SimpleMatrix newCovariance = null;
            if (smCovariances.length > 0)
                newCovariance = smCovariances[0];
            distribution.setGlobalCovariance(newCovariance);
            distribution.setGlobalMean(newMean);
            distribution.setGlobalWeight(smWeights[0]);
            return;
        }

        // calculate new weight
        double newWeight = 0;
        for (int i=0; i<smWeights.length;i++) {
            newWeight += smWeights[i];
        }
        // calculate new mean vector
        SimpleMatrix newMean = new SimpleMatrix(smMeans[0].numRows(), smMeans[0].numCols());
        for (int i = 0; i < smMeans.length; i++) {
            newMean = newMean.plus((smMeans[i].scale(smWeights[i])));
        }
        newMean = newMean.scale(1 / newWeight);
        // calculate new covariance matrix
        SimpleMatrix newCovariance = new SimpleMatrix(smCovariances[0].numRows(), smCovariances[0].numCols());
        for (int i = 0; i < smCovariances.length; i++) {
            SimpleMatrix dyadSmMean = smMeans[i].mult(smMeans[i].transpose());
            SimpleMatrix S = smCovariances[i].plus(dyadSmMean);
            newCovariance = newCovariance.plus(S.scale(smWeights[i]));
        }
        newCovariance = newCovariance.scale(1 / newWeight);
        SimpleMatrix dyadNewMean = newMean.mult(newMean.transpose());
        newCovariance = newCovariance.minus(dyadNewMean);
        //System.out.println("matching moments");
        //System.out.println(newCovariance);
        //System.out.println(newMean);
        // set calculated parameters to distribution
        distribution.setGlobalCovariance(newCovariance);
        distribution.setGlobalMean(newMean);
        distribution.setGlobalWeight(newWeight);
    }
}