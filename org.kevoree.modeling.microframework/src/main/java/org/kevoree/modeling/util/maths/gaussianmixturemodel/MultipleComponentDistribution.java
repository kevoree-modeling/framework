package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

import java.util.ArrayList;

abstract public class MultipleComponentDistribution extends BaseSampleDistribution {

    // component distributions
    private OneComponentDistribution[] mSubDistributions;

    public MultipleComponentDistribution(){
        super();
    }

    public void setValues(double[] weights, SimpleMatrix[] means, SimpleMatrix[] covariances, SimpleMatrix bandwidth) {
        mGlobalWeight = 0;
        mForgettingFactor = 0;
        if(bandwidth == null)
            bandwidth = covariances[0].scale(0);
        mBandwidthMatrix = bandwidth;
        // add components to distribution
        mSubDistributions = new OneComponentDistribution[weights.length];
        for(int i=0; i<mSubDistributions.length; i++){
            OneComponentDistribution res=new OneComponentDistribution();
            res.setValues(weights[i], means[i], covariances[i], bandwidth);
            mSubDistributions[i] = res;
        }
        mGlobalWeight = 0;
        for (int j=0;j<weights.length;j++) {
            mGlobalWeight += weights[j];
        }
        mForgettingFactor = 1;
    }


    public void setComponent(MultipleComponentDistribution dist) {
        OneComponentDistribution[] subDists = dist.getSubComponents();
        OneComponentDistribution[] copy = new OneComponentDistribution[subDists.length];
        for(int i=0; i<subDists.length; i++) {
            copy[i] = new OneComponentDistribution();
            copy[i].setComponent(subDists[i]);
        }
        this.mSubDistributions = copy;
        this.mBandwidthMatrix = dist.getBandwidthMatrix();
        this.mGlobalCovariance = dist.getGlobalCovariance();
        this.mGlobalMean = dist.getGlobalMean();
        this.mSubspaceGlobalCovariance = dist.getSubspaceGlobalCovariance();
        this.mSubspaceInverseCovariance = dist.getSubspaceInverseCovariance();
        this.mGlobalWeight = dist.getGlobalWeight();
    }


    @Override
    public double evaluateMatrix(SimpleMatrix pointVector) {
        SimpleMatrix[] means = this.getSubMeans();
        SimpleMatrix[] covs = this.getSubCovariances();
        Double[] weights = this.getSubWeights();
        double d = 0d;
        double n = means[0].numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);
        for (int i = 0; i < means.length; i++) {
            SimpleMatrix m = means[i];
            SimpleMatrix c = covs[i].plus(this.mBandwidthMatrix);
            double w = weights[i];
            double tmp = (-0.5d) * pointVector.minus(m).transpose().mult(c.invert()).mult(pointVector.minus(m)).trace();
            d += ((1 / (a * Math.sqrt(c.determinant()))) * Math.exp(tmp)) * w;
        }
        return d;
    }





    public void setSubComponents(OneComponentDistribution[] subComponents) {
        this.mSubDistributions = subComponents;
    }

    public OneComponentDistribution[] getSubComponents() {
        return mSubDistributions;
    }

    public SimpleMatrix[] getSubMeans() {
        SimpleMatrix[] means = new SimpleMatrix[mSubDistributions.length];
        for (int i=0; i<mSubDistributions.length; i++)
            means[i] = mSubDistributions[i].getGlobalMean();
        return means;
    }

    public SimpleMatrix[] getSubCovariances() {
        SimpleMatrix[] covs = new SimpleMatrix[mSubDistributions.length];
        for (int i=0; i<mSubDistributions.length; i++)
            covs[i] = mSubDistributions[i].getGlobalCovariance();
        return covs;
    }

    public Double[] getSubWeights() {
        Double[] weights = new Double[mSubDistributions.length];
        for (int i=0; i<mSubDistributions.length; i++)
            weights[i] = mSubDistributions[i].getGlobalWeight();
        return weights;
    }

    public void setSubMeans(SimpleMatrix[] means) {
        for (int i=0; i<mSubDistributions.length; i++)
            mSubDistributions[i].setGlobalMean(means[i]);
    }

    public void setSubCovariances(SimpleMatrix[] covariances) {
        for (int i=0; i<mSubDistributions.length; i++)
            mSubDistributions[i].setGlobalCovariance(covariances[i]);
    }

    @Override
    public void setBandwidthMatrix(SimpleMatrix mBandwidthMatrix) {
        this.mBandwidthMatrix = mBandwidthMatrix;
        for(int i=0;i< mSubDistributions.length;i++){
            BaseSampleDistribution d=mSubDistributions[i];
            d.setBandwidthMatrix(mBandwidthMatrix);
        }
    }
}

