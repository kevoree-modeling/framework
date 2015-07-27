package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;

import java.util.ArrayList;



public class OneComponentDistribution extends BaseSampleDistribution {

    public OneComponentDistribution(){
        super();
    }

   public void setValues(double w, SimpleMatrix mean, SimpleMatrix covariance, SimpleMatrix bandwidth) {
       mGlobalWeight = 0;
       mForgettingFactor = 0;
        mGlobalWeight = w;
        mGlobalMean = mean;
        mGlobalCovariance = covariance;
        mBandwidthMatrix = bandwidth;
        mForgettingFactor = 1;
    }

    public void setComponent(BaseSampleDistribution oneComponentDistribution) {
        this.mBandwidthMatrix = oneComponentDistribution.getBandwidthMatrix();
        this.mGlobalCovariance = oneComponentDistribution.getGlobalCovariance();
        this.mGlobalMean = oneComponentDistribution.getGlobalMean();
        this.mSubspaceGlobalCovariance = oneComponentDistribution.getSubspaceGlobalCovariance();
        this.mSubspaceInverseCovariance = oneComponentDistribution.getSubspaceInverseCovariance();
        this.mGlobalWeight = oneComponentDistribution.getGlobalWeight();
    }




    public TwoComponentDistribution split(double parentWeight){
        SimpleSVD<?> svd = mGlobalCovariance.svd(true);
        SimpleMatrix S = svd.getW();
        SimpleMatrix V = svd.getV();
        SimpleMatrix d = S.extractDiag();
        double max = CommonOps.maxVectorElement(d);
        int maxIndex = CommonOps.maxVectorElementIndex(d);
        int len = mGlobalCovariance.numRows();
        SimpleMatrix M = new SimpleMatrix(len,1);
        M.setValue2D(maxIndex, 0, 1.0d);
        SimpleMatrix dMean = V.mult(M).scale(0.5*Math.sqrt(max));
        SimpleMatrix meanSplit1 = mGlobalMean.plus(dMean);
        SimpleMatrix meanSplit2 = mGlobalMean.minus(dMean);

        SimpleMatrix dyadMean = mGlobalMean.mult(mGlobalMean.transpose());
        SimpleMatrix dyadMeanSplit1 = meanSplit1.mult(meanSplit1.transpose());
        SimpleMatrix dyadMeanSplit2 = meanSplit2.mult(meanSplit2.transpose());
        SimpleMatrix covSplit = mGlobalCovariance.plus(dyadMean).minus(dyadMeanSplit1.plus(dyadMeanSplit2).scale(0.5));

        SimpleMatrix[] means = {meanSplit1, meanSplit2};
        SimpleMatrix[] covariances = {covSplit, covSplit};
        double[] weights = {0.5, 0.5};
        TwoComponentDistribution splitDist = null;
        try {
            splitDist = new TwoComponentDistribution(weights, means, covariances, mBandwidthMatrix);
            splitDist.setGlobalWeight(parentWeight*mGlobalWeight);
            splitDist.setGlobalCovariance(mGlobalCovariance);
            splitDist.setGlobalMean(mGlobalMean);
        } catch (Exception e) {
            // cant be thrown
        }
        return splitDist;
    }


    @Override
    public double evaluateMatrix(SimpleMatrix pointVector) {
        SimpleMatrix smoothedCov = mGlobalCovariance.plus(mBandwidthMatrix);
        double d = 0d;
        double n = mGlobalMean.numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);
        double tmp = (-0.5d) * pointVector.minus(mGlobalMean).transpose().mult(smoothedCov.invert()).mult(pointVector.minus(mGlobalMean)).trace();
        d += ((1 / (a * Math.sqrt(smoothedCov.determinant()))) * Math.exp(tmp)) * mGlobalWeight;

        return d;
    }


    @Override
    public void setBandwidthMatrix(SimpleMatrix mBandwidthMatrix) {
        this.mBandwidthMatrix = mBandwidthMatrix;
    }
}

