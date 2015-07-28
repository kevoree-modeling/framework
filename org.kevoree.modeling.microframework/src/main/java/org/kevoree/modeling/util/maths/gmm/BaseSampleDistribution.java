package org.kevoree.modeling.util.maths.gmm;


import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public abstract class BaseSampleDistribution {

    // bandwidth matrix
    protected SimpleMatrix mBandwidthMatrix;

    // overall weight sum
    protected double mGlobalWeight;

    // overall mean
    protected SimpleMatrix mGlobalMean;

    // overall covariance
    protected SimpleMatrix mGlobalCovariance;

    // overall covariance in subspace
    protected SimpleMatrix mSubspaceGlobalCovariance;

    // overall inverse covariance in subspace
    protected SimpleMatrix mSubspaceInverseCovariance;

    // forgetting factor, to be used for non-stationary distributions
    protected double mForgettingFactor;

    public double getForgettingFactor() {
        return mForgettingFactor;
    }

    public void setForgettingFactor(double forgettingFactor) {
        this.mForgettingFactor = forgettingFactor;
    }



    public double getGlobalWeight() {
        return mGlobalWeight;
    }

    public SimpleMatrix getBandwidthMatrix() {
        return mBandwidthMatrix;
    }

    abstract public void setBandwidthMatrix(SimpleMatrix mBandwidthMatrix);

    public SimpleMatrix getGlobalCovariance() {
        return mGlobalCovariance;
    }

    public void setGlobalCovariance(SimpleMatrix globalCovariance) {
        this.mGlobalCovariance = globalCovariance;
    }

    public SimpleMatrix getSubspaceGlobalCovariance() {
        return mSubspaceGlobalCovariance;
    }

    public void setGlobalWeight(double weight) {
        this.mGlobalWeight = weight;
    }

    public void scaleGlobalWeight(double scaleFactor) {
        this.mGlobalWeight = this.mGlobalWeight*scaleFactor;
    }

    public void setSubspaceGlobalCovariance(SimpleMatrix subspaceCovariance) {
        this.mSubspaceGlobalCovariance = subspaceCovariance;
    }

    public SimpleMatrix getSubspaceInverseCovariance() {
        return mSubspaceInverseCovariance;
    }

    public void setSubspaceInverseCovariance(SimpleMatrix subspaceInverseCovariance) {
        this.mSubspaceInverseCovariance = subspaceInverseCovariance;
    }


    abstract public double evaluateMatrix(SimpleMatrix pointVector);


    public double[] evaluate(SimpleMatrix[] points) {
        double[] resultPoints = new double[points.length];
        for (int i=0;i<points.length;i++) {
            resultPoints[i]= evaluateMatrix(points[i]);
        }
        return resultPoints;
    }

    public SimpleMatrix getGlobalMean() {
        return mGlobalMean;
    }

    public void setGlobalMean(SimpleMatrix globalMean) {
        this.mGlobalMean = globalMean;
    }

    public SimpleMatrix getmGlobalCovarianceSmoothed() {
        if (mBandwidthMatrix == null)
            mBandwidthMatrix = mGlobalCovariance.scale(0);
        return (mGlobalCovariance.plus(mBandwidthMatrix));
    }

}
