package org.kevoree.modeling.util.maths.gmm;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrixHashable;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleEVD;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class SampleModel{

    private static final double DEFAULT_NO_OF_COMPS_THRES = 6;
    private HashMap<SimpleMatrixHashable, Double> mProbabilityCache = new HashMap<SimpleMatrixHashable, Double>();
    // When mahalanobis distance gets bigger than this the component does not contribute
    // to the density that should be calculated
    // exp(-40) ~ 5E-18
    private static final double MAX_MAHALANOBIS_DIST = 40;
    // compression threshold (maximal hellinger distance)
    public double mCompressionThreshold;
    // effective number of observed samples
    protected double mEffectiveNoOfSamples;
    // component distributions
    protected BaseSampleDistribution[] mSubDistributions;

    // threshold to determine when compression is necessary
    public double mNoOfCompsThreshold;

    public double mEMError=0;
    public double mEMCount=0;

   // public long bwtime = 0;

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

    public double[] evaluateArray(SimpleMatrix[] points) {
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


   public void setSampleModelForget(double forgettingFactor, double compressionThreshold) {
        mGlobalWeight = 0;
        mForgettingFactor = 0;
        this.mSubDistributions = new BaseSampleDistribution[0];
        this.mBandwidthMatrix = null;
        this.mGlobalCovariance = null;
        this.mGlobalMean = null;
        this.mSubspace = null;
        this.mSubspaceGlobalCovariance = null;
        this.mSubspaceInverseCovariance = null;
        this.mGlobalWeight = 0;
        this.mEffectiveNoOfSamples = 0;
        this.mForgettingFactor = forgettingFactor;
        this.mCompressionThreshold = compressionThreshold;
        mNoOfCompsThreshold = DEFAULT_NO_OF_COMPS_THRES;
    }

    public SampleModel(){

    }

    public void setSampleModel(SampleModel dist)  {
        BaseSampleDistribution[] subDists = dist.getSubDistributions();
        BaseSampleDistribution[] copy = new BaseSampleDistribution[subDists.length];

        for(int i=0;i<subDists.length;i++){
          copy[i]=subDists[i];
          /*  if(subDists[i] instanceof OneComponentDistribution){
                copy[i]=new OneComponentDistribution();

            }*/
        }
        //todo fill copy

        // copy the list of sub components
        // todo by assaad removed
        /*
        for (BaseSampleDistribution d : subDists) {
            // We don't know if a sub component is of type
            // OneComponentDistribution or TwoComponentDistribution.
            // Thus we have to call the constructor using reflection api. This
            // way it is generic and works in both cases.
            Constructor<? extends BaseSampleDistribution> ctor = d.getClass().getDeclaredConstructor(d.getClass());
            ctor.setAccessible(true);
            BaseSampleDistribution tmp = (BaseSampleDistribution) ctor.newInstance(d);
            copy.add(tmp);
        }*/
        this.mSubDistributions = copy;
        this.mBandwidthMatrix = dist.getBandwidthMatrix();
        this.mGlobalCovariance = dist.getGlobalCovariance();
        this.mGlobalMean = dist.getGlobalMean();
        this.mSubspace = dist.getmSubspace();
        this.mSubspaceGlobalCovariance = dist.getSubspaceGlobalCovariance();
        this.mSubspaceInverseCovariance = dist.getSubspaceInverseCovariance();
        this.mGlobalWeight = dist.getGlobalWeight();
        this.mEffectiveNoOfSamples = dist.mEffectiveNoOfSamples;
    }

    public void overWirite(SampleModel dist) throws RuntimeException, InvocationTargetException, NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException {
        BaseSampleDistribution[] subDists = dist.getSubDistributions();
        BaseSampleDistribution[] copy = new BaseSampleDistribution[subDists.length];
        //todo fill copy


        // todo by assaad removed
       /* for (BaseSampleDistribution d : subDists) {
            Constructor<? extends BaseSampleDistribution> ctor = d.getClass().getDeclaredConstructor(d.getClass());
            ctor.setAccessible(true);
            BaseSampleDistribution tmp = (BaseSampleDistribution) ctor.newInstance(d);
            copy.add(tmp);
        }*/
        this.mSubDistributions = copy;
        this.mBandwidthMatrix = dist.getBandwidthMatrix();
        this.mGlobalCovariance = dist.getGlobalCovariance();
        this.mGlobalMean = dist.getGlobalMean();
        this.mSubspace = dist.getmSubspace();
        this.mSubspaceGlobalCovariance = dist.getSubspaceGlobalCovariance();
        this.mSubspaceInverseCovariance = dist.getSubspaceInverseCovariance();
        this.mGlobalWeight = dist.getGlobalWeight();
        this.mEffectiveNoOfSamples = dist.mEffectiveNoOfSamples;
    }


    // subspace: row/column ids
    private int[] mSubspace;

    public int[] getmSubspace() {
        return mSubspace;
    }

    public void setmSubspace(int[] mSubspace) {
        this.mSubspace = mSubspace;
    }

    public void updateDistributionArrayMatrix(SimpleMatrix[] means, SimpleMatrix[] covariances, double[] weights) {
        // at first check input parameters!
        checkInputParams(means, covariances, weights);

        // augment distribution
        addDistributions(weights, means, covariances);

        // save indices of new components for em updates
        int[] newPoints = new int[mSubDistributions.length-(mSubDistributions.length-means.length)];
        int j=0;
        for(int i=(mSubDistributions.length-means.length); i<mSubDistributions.length; i++) {
            newPoints[j]=i;
        }
        updateDistributionArray(newPoints);
    }

    public void updateDistributionValues(SimpleMatrix mean, SimpleMatrix covariance, double weight) {

        // augment distribution
        addDistribution(weight, mean, covariance);
        int[] newPoints = new int[1];
        // only one component was added, save index for em updates
        newPoints[0]=(mSubDistributions.length-1);
        updateDistributionArray(newPoints);
    }

    private void updateDistributionArray(int[] newPoints) {
        BaseSampleDistribution[] subDists = getSubDistributions();
        double[] weights = new double[subDists.length];
        for (int i = 0; i < subDists.length; i++)
            weights[i] = subDists[i].getGlobalWeight();

        SampleModel subSpaceDist = null;
        try {
            subSpaceDist = projectToSubspace(this);
        }
        catch (Exception ex){
            ex.printStackTrace();

        }

        // reestimate bandwidth as explained in oKDE paper
     //   long time = System.currentTimeMillis();
        double bandwidthFactor = reestimateBandwidth(subSpaceDist.getSubMeans(), subSpaceDist.getSubCovariances(), weights, subSpaceDist.getSubspaceGlobalCovariance(), mEffectiveNoOfSamples);
     //   bwtime += (System.currentTimeMillis()-time);
        //System.out.println("BANDW" + bandwidthFactor);
        // project Bandwidth into original space
        SimpleMatrix bandwidthMatrix = projectBandwidthToOriginalSpace(subSpaceDist, bandwidthFactor);
        this.mBandwidthMatrix = bandwidthMatrix;
        for (int i = 0; i < this.getSubDistributions().length; i++) {
            this.getSubDistributions()[i].setBandwidthMatrix(bandwidthMatrix);
        }
        //System.out.println("BW: " + bandwidthMatrix);
        //System.out.println(bandwidthMatrix.get(0, 0) + " " + bandwidthMatrix.get(1, 1));
        if (mGlobalCovariance == null) {
            mGlobalCovariance = new SimpleMatrix(2, 2);
            //System.out.println("globcov null");
        }
        try {
            Compressor.compress(this, newPoints);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkInputParams(SimpleMatrix[] means, SimpleMatrix[] covariances, double[] weights) {
        if (weights == null || weights.length == 0 || means == null || means.length == 0|| covariances == null || covariances.length == 0) {
            return false;
        }
        else{
            return true;
        }

    }


    /**
     * Takes new incoming sample weights and updates this distribution using a
     * forgetting factor.
     *
     * @param weights
     */
    private void addDistributions(double[] weights, SimpleMatrix[] means, SimpleMatrix[] covariances) {
        double sumOfNewWeights = 0;

        int len=mSubDistributions.length;
        BaseSampleDistribution[] array2= new BaseSampleDistribution[mSubDistributions.length+weights.length];
        System.arraycopy(mSubDistributions,0,array2,0,mSubDistributions.length);
        mSubDistributions=array2;


        for (int i = 0; i < weights.length; i++) {
            sumOfNewWeights += weights[i];
            OneComponentDistribution res=new OneComponentDistribution();
            res.setValues(weights[i], means[i], covariances[i], mBandwidthMatrix);
            mSubDistributions[i+len]=(res);
            }

        // calculate mixing weights for old and new weights
        double mixWeightOld = (mEffectiveNoOfSamples* mForgettingFactor) / (mEffectiveNoOfSamples * mForgettingFactor + sumOfNewWeights);
        double mixWeightNew = sumOfNewWeights / (mEffectiveNoOfSamples * mForgettingFactor + sumOfNewWeights);

        mEffectiveNoOfSamples = mEffectiveNoOfSamples * mForgettingFactor + weights.length;

        // mGlobalWeight = mGlobalWeight * mForgettingFactor + sumOfNewWeights;
        mGlobalWeight = mixWeightOld + mixWeightNew;

        for (int i = 0; i < mSubDistributions.length - weights.length; i++) {
            double tmpWeight = mSubDistributions[i].getGlobalWeight();
            mSubDistributions[i].setGlobalWeight(tmpWeight * mixWeightOld);
        }
        for (int i = mSubDistributions.length - weights.length; i < mSubDistributions.length; i++) {
            double tmpWeight = mSubDistributions[i].getGlobalWeight();
            mSubDistributions[i].setGlobalWeight(tmpWeight * mixWeightNew * (1d / weights.length));
        }
        // mEffectiveNoOfSamples = mSubDistributions.length;
    }


    private void addDistribution(double weight, SimpleMatrix mean, SimpleMatrix covariance) {
        double sumOfNewWeights = 0;
        sumOfNewWeights += weight;
        OneComponentDistribution res=new OneComponentDistribution();
        res.setValues(weight, mean, covariance, mBandwidthMatrix);
     //   mSubDistributions.add(res);

        BaseSampleDistribution[] array2= new BaseSampleDistribution[mSubDistributions.length+1];
        System.arraycopy(mSubDistributions,0,array2,0,mSubDistributions.length);
        array2[mSubDistributions.length]=res;
        mSubDistributions=array2;



        // calculate mixing weights for old and new weights
        double mixWeightOld = (mEffectiveNoOfSamples* mForgettingFactor) / (mEffectiveNoOfSamples * mForgettingFactor + sumOfNewWeights);
        double mixWeightNew = sumOfNewWeights / (mEffectiveNoOfSamples * mForgettingFactor + sumOfNewWeights);

        mEffectiveNoOfSamples = mEffectiveNoOfSamples * mForgettingFactor + 1;

        // mGlobalWeight = mGlobalWeight * mForgettingFactor + sumOfNewWeights;
        mGlobalWeight = mixWeightOld + mixWeightNew;

        for (int i = 0; i < mSubDistributions.length - 1; i++) {
            double tmpWeight = mSubDistributions[i].getGlobalWeight();
            mSubDistributions[i].setGlobalWeight(tmpWeight * mixWeightOld);
        }
        for (int i = mSubDistributions.length - 1; i < mSubDistributions.length; i++) {
            double tmpWeight = mSubDistributions[i].getGlobalWeight();
            mSubDistributions[i].setGlobalWeight(tmpWeight * mixWeightNew * (1d / 1));
        }
        // mEffectiveNoOfSamples = mSubDistributions.length;
    }


    private static SampleModel projectToSubspace(SampleModel dist) throws Exception{
        double minBW = 1e-7;
        SampleModel distribution = new SampleModel();
        distribution.setSampleModel(dist);
        int[] subSpace = new int[1];
        MomentMatcher.matchMoments(distribution);
        SimpleMatrix overallCovariance = distribution.getGlobalCovariance();
        //System.out.println("cov: " + overallCovariance);
        SimpleSVD<?> svd = overallCovariance.svd(true);
        SimpleMatrix U = svd.getU();
        SimpleMatrix S = svd.getW();
        S = S.extractDiag();

        SimpleMatrix F = new SimpleMatrix(0, 0);
        double count = 0, mean = 0;
        for (int i = 0; i < U.numRows(); i++) {
            if (S.getValue2D(i, 0) > minBW) {
                int[] array2= new int[subSpace.length+1];
                System.arraycopy(subSpace,0,array2,0,subSpace.length);
                array2[subSpace.length]=i;
                subSpace=array2;

                SimpleMatrix colU = U.extractVector(false, i);
                double rowW = Math.pow(S.getValue2D(i, 0), -0.5);
                colU = colU.scale(rowW);
                F = F.combine(0, F.numCols(), colU);
                mean += S.getValue2D(i, 0);
                count++;
            }
        }
        mean = (mean / count) * 1e-2;
        for (int i = 0; i < S.numRows(); i++) {
            if (S.getValue2D(i, 0) < minBW) {
                S.setValue2D(i, 0, mean);
            }
        }
        SimpleMatrix iF = new SimpleMatrix(0, 0);
        for (int i = 0; i < U.numCols(); i++) {
            SimpleMatrix coliF = U.extractVector(false, i);
            double rowW = Math.pow(S.getValue2D(i, 0), 0.5);
            coliF = coliF.scale(rowW).transpose();
            iF = iF.combine(iF.numRows(), 0, coliF);
        }
        SimpleMatrix subspaceCov = F.transpose().mult(overallCovariance).mult(F);
        distribution.setSubspaceGlobalCovariance(subspaceCov);

        SimpleMatrix[] originalMeans = distribution.getSubMeans();
        SimpleMatrix subspaceMean = distribution.getGlobalMean();
        for (int i = 0; i < originalMeans.length; i++) {
            originalMeans[i]= originalMeans[i].minus(subspaceMean);
        }
        SimpleMatrix[] covariances = distribution.getSubCovariances();
        for (int i = 0; i < originalMeans.length; i++) {
            originalMeans[i]= F.transpose().mult(originalMeans[i]);
            covariances[i]= F.transpose().mult(covariances[i]).mult(F);
        }
        distribution.setSubCovariances(covariances);
        distribution.setSubMeans(originalMeans);

        distribution.setSubspaceInverseCovariance(iF);
        distribution.setmSubspace(subSpace);
        return distribution;
    }

    public static boolean containsVal(int i,int j, int[] subspace){
        boolean conti=false;
        boolean contj=false;

        for(int k=0;k<subspace.length;k++){
            conti= conti||(subspace[k]==i);
            contj=contj||(subspace[k]==j);
        }
        return conti && contj;
    }

    private static SimpleMatrix projectBandwidthToOriginalSpace(SampleModel distribution, double bandwidthFactor) {
        SimpleMatrix bandwidth = SimpleMatrix.identity(distribution.getGlobalCovariance().numCols());
        SimpleMatrix subSpaceBandwidth = distribution.getSubspaceGlobalCovariance().scale(Math.pow(bandwidthFactor, 2));
        int[] subspace = distribution.getmSubspace();
        for (int i = 0; i < subSpaceBandwidth.numRows(); i++) {
            for (int j = 0; j < subSpaceBandwidth.numCols(); j++) {
                if (containsVal(i,j,subspace))
                    bandwidth.setValue2D(i, j, subSpaceBandwidth.getValue2D(i, j));
            }
        }
        SimpleMatrix invSubspaceCov = distribution.getSubspaceInverseCovariance();
        bandwidth = invSubspaceCov.transpose().mult(bandwidth).mult(invSubspaceCov);
        return bandwidth;
    }

    private double reestimateBandwidth(SimpleMatrix[] means, SimpleMatrix[] covariance, double[] weights, SimpleMatrix Cov_smp, double N_eff) {

        double d = means[0].numRows();

        // Silverman
        // SimpleMatrix G = Cov_smp.scale(Math.pow((4 / ((d + 2) * N_eff)), (2 /
        // (d + 4))));

        // other
        // Cov_smp *(2/(2+d))^(2/(4+d)) * 4 *N_eff^(-2/(4+d))
        SimpleMatrix G = Cov_smp.scale(Math.pow((2d / (d + 2d)), (2d / (d + 4d))) * 4 * Math.pow(N_eff, -2d / (4d + d)));

        double alphaScale = 1;
        SimpleMatrix F = Cov_smp.scale(alphaScale);

        double Rf2 = getIntSquaredHessian(means, weights, covariance, F, G);
        double hAmise = Math.pow((Math.pow(N_eff, (-1)) * Math.pow(F.determinant(), (-1 / 2)) / (Math.pow(Math.sqrt(4 * Math.PI), d) * Rf2 * d)),
                (1 / (d + 4)));
        return hAmise;
    }

    private double getIntSquaredHessian(SimpleMatrix[] means, double[] weights, SimpleMatrix[] covariance, SimpleMatrix F, SimpleMatrix g) {
       // long time = System.currentTimeMillis();
        long d = means[0].numRows();
        long N = means.length;
        // normalizer
        double constNorm = Math.pow((1d / (2d * Math.PI)), (d / 2d));

        // test if F is identity for speedup
        SimpleMatrix Id = SimpleMatrix.identity(F.numCols());
        double deltaF = F.minus(Id).elementSum();

        double w1, w2, m, I = 0, eta, f_t, c;
        SimpleMatrix s1, s2, mu1, mu2, dm, ds, B, b, C;
        for (int i1 = 0; i1 < N; i1++) {
            s1 = covariance[i1].plus(g);
            mu1 = means[i1];
            w1 = weights[i1];
            for (int i2 = i1; i2 < N; i2++) {
                s2 = covariance[i2];
                mu2 = means[i2];
                w2 = weights[i2];
                SimpleMatrix A = s1.plus(s2).invert();
                dm = mu1.minus(mu2);

                // if F is not identity
                if (deltaF > 1e-3) {
                    ds = dm.transpose().mult(A);
                    b = ds.transpose().mult(ds);
                    B = A.minus(b.scale(2));
                    C = A.minus(b);
                    f_t = constNorm * Math.sqrt(A.determinant()) * Math.exp(-0.5 * ds.mult(dm).trace());
                    c = 2 * F.mult(A).mult(F).mult(B).trace() + Math.pow(F.mult(C).trace(), 2);
                } else {
                    m = dm.transpose().mult(A).mult(dm).getValue1D(0); //replaced here by assaad
                    f_t = constNorm * Math.sqrt(A.determinant()) * Math.exp(-0.5 * m);

                    DenseMatrix64F A_sqr = new DenseMatrix64F(A.numRows(), A.numCols());
                    CommonOps.elementMult(A.getMatrix(), A.transpose().getMatrix(), A_sqr);
                    double sum = CommonOps.elementSum(A_sqr);
                    c = 2d * sum * (1d - 2d * m) + Math.pow((1d - m), 2d) * Math.pow(A.trace(), 2);
                }

                // determine the weight of the current term
                if (i1 == i2)
                    eta = 1;
                else
                    eta = 2;
                I = I + f_t * c * w2 * w1 * eta;
            }
        }
		/*time = System.currentTimeMillis()-time;
		if((time) > 100)
			System.out.println("Time for IntSqrdHessian: "+ ((double)time/1000)+"s"+"  loopcount: "+N);*/
        return I;
    }

    public void setSubDistributions(BaseSampleDistribution[] subDistributions) {
        this.mSubDistributions = subDistributions;
    }

    public BaseSampleDistribution[] getSubDistributions() {
        return mSubDistributions;
    }
    
    public void addToSubDistribution(BaseSampleDistribution dist){
        BaseSampleDistribution[] array2=new BaseSampleDistribution[mSubDistributions.length+1];
        System.arraycopy(mSubDistributions,0,array2,0,mSubDistributions.length);
        array2[array2.length-1]=dist;
        mSubDistributions=array2;

    }

    public void setSubMeans(SimpleMatrix[] means) {
        for (int i = 0; i < mSubDistributions.length; i++) {
            mSubDistributions[i].setGlobalMean(means[i]);
        }
    }

    public void setSubCovariances(SimpleMatrix[] covariances) {
        for (int i = 0; i < mSubDistributions.length; i++) {
            mSubDistributions[i].setGlobalCovariance(covariances[i]);
        }
    }

    public SimpleMatrix[] getSubSmoothedCovariances() {
        SimpleMatrix[] covs = new SimpleMatrix[mSubDistributions.length] ;
        for (int i=0;i<mSubDistributions.length;i++) {
            BaseSampleDistribution d = mSubDistributions[i];
            covs[i]=(d.getmGlobalCovarianceSmoothed());
        }
        return covs;
    }

    public SimpleMatrix[] getSubMeans() {
        SimpleMatrix[] means = new SimpleMatrix[mSubDistributions.length] ;
        for (int i=0;i<mSubDistributions.length;i++) {
            BaseSampleDistribution d = mSubDistributions[i];
            try {
                means[i] = (d.getGlobalMean());
            }
            catch (Exception ex){
                int x=9;

            }
        }
        return means;
    }

    public SimpleMatrix[] getSubCovariances() {
        SimpleMatrix[] covs = new SimpleMatrix[mSubDistributions.length] ;
        for (int i=0;i<mSubDistributions.length;i++) {
            BaseSampleDistribution d = mSubDistributions[i];
            covs[i]=(d.getGlobalCovariance());
        }
        return covs;
    }

    public double[] getSubWeights() {
        double[] weights = new double[mSubDistributions.length] ;
        for (int i=0;i<mSubDistributions.length;i++) {
            BaseSampleDistribution d = mSubDistributions[i];
            weights[i]=(d.getGlobalWeight());
        }
        return weights;
    }

    public void setSubWeights(double[] weights) {
        for (int i = 0; i < mSubDistributions.length; i++) {
            mSubDistributions[i].setGlobalWeight(weights[i]);
        }
    }


    public ConditionalDistribution getMarginalDistribution(int n){
        SimpleMatrix[] means = this.getSubMeans();
        SimpleMatrix[] marginalMeans = new SimpleMatrix[means.length] ;

        SimpleMatrix[] covs = this.getSubSmoothedCovariances();
        SimpleMatrix[] marginalCovs = new SimpleMatrix[means.length] ;

        double[] weights = this.getSubWeights();
     //   double[] marginalWeights = new double[] ;

        ConditionalDistribution result = null;

        double a = Math.pow(Math.sqrt(2 * Math.PI), n);


        for(int i=0; i<means.length; i++) {
            SimpleMatrix c = covs[i];
            SimpleMatrix m = means[i];
            SimpleMatrix m1 = new SimpleMatrix(n,1);

            // extract all elements from covariance that correspond only to m1
            // that means extract the block in the left top corner with height=width=n
            SimpleMatrix newC1 = new SimpleMatrix(n,n);
            for(int j=0; j<n; j++) {
                for(int k=0; k<n; k++) {
                    newC1.setValue2D(j, k, c.getValue2D(j, k));
                }
            }
            //extract last rows from mean to m1
            for(int j=0; j<n; j++) {
                m1.setValue2D(j, 0, m.getValue2D(j, 0));
            }

            marginalMeans[i]=(m1);
            marginalCovs[i]=(newC1);

        }
        result = new ConditionalDistribution(marginalMeans, marginalCovs, weights);
        return result;
    }

    /**
     * This method derives the conditional distribution of the actual sample createModel kde with distribution p(x).
     * It takes a condition parameter that is a vector c of dimension m. Using this vector
     * it finds the conditional distribution p(x*|c) where c=(x_0,...,x_m), x*=(x_m+1,...,x_n).
     * For detailed description see:
     * @param condition A vector that defines c in p(x*|c)
     * @return The conditional distribution of this sample createModel under the given condition
     */
    public ConditionalDistribution getConditionalDistribution(SimpleMatrix condition){
        int lenCond = condition.numRows();

        SimpleMatrix[] means = this.getSubMeans();
        SimpleMatrix[] conditionalMeans = new SimpleMatrix[means.length] ;

        SimpleMatrix[] covs = this.getSubSmoothedCovariances();
        SimpleMatrix[] conditionalCovs = new SimpleMatrix[means.length] ;

        double[] weights = this.getSubWeights();
        double[] conditionalWeights = new double[means.length] ;

        ConditionalDistribution result = null;

        double n = condition.numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);


        for(int i=0; i<means.length; i++) {
            SimpleMatrix c = covs[i];
            SimpleMatrix invC = c.invert();
            SimpleMatrix m = means[i];
            int lenM1 = m.numRows()-lenCond;
            SimpleMatrix m1 = new SimpleMatrix(lenM1,1);
            SimpleMatrix m2 = new SimpleMatrix(lenCond,1);

            // extract all elements from inverse covariance that correspond only to m1
            // that means extract the block in the right bottom corner with height=width=lenM1
            SimpleMatrix newC1 = new SimpleMatrix(lenM1,lenM1);
            for(int j=0; j<lenM1; j++) {
                for(int k=0; k<lenM1; k++) {
                    newC1.setValue2D(j, k, invC.getValue2D(j + lenCond, k + lenCond));
                }
            }
            // extract all elements from inverse covariance that correspond to m1 and m2
            // from the the block in the left bottom corner with height=width=lenM1
            SimpleMatrix newC2 = new SimpleMatrix(lenM1,lenCond);
            for(int j=0; j<lenM1; j++) {
                for(int k=0; k<lenCond; k++) {
                    newC2.setValue2D(j, k, invC.getValue2D(j + lenCond, k));
                }
            }

            //extract first rows from mean to m2
            for(int j=0; j<lenCond; j++) {
                m2.setValue2D(j, 0, m.getValue2D(j, 0));
            }
            //extract last rows from mean to m1
            for(int j=0; j<lenM1; j++) {
                m1.setValue2D(j, 0, m.getValue2D(j + lenCond, 0));
            }
            SimpleMatrix invNewC1 = newC1.invert();
            // calculate new mean and new covariance of conditional distribution
            SimpleMatrix condMean = m1.minus( invNewC1.mult(newC2).mult( condition.minus(m2) ) );
            SimpleMatrix condCovariance = invNewC1;
            conditionalMeans[i]=(condMean);
            conditionalCovs[i]=(condCovariance);

            // calculate new weights

            // extract all elements from inverse covariance that correspond only to m2
            // that means extract the block in the left top corner with height=width=lenCond
            SimpleMatrix newC22 = new SimpleMatrix(lenCond,lenCond);
            for(int j=0; j<lenCond; j++) {
                for(int k=0; k<lenCond; k++) {
                    newC22.setValue2D(j, k, c.getValue2D(j, k));
                }
            }
            double mahalanobisDistance = condition.minus(m2).transpose().mult(newC22.invert()).mult(condition.minus(m2)).trace();
            double newWeight = ((1 / (a * Math.sqrt(newC22.determinant()))) * Math.exp((-0.5d) * mahalanobisDistance))* weights[i];
            conditionalWeights[i]=(newWeight);
        }
        // normalize weights
        double weightSum = 0;
        for(int i=0; i<conditionalWeights.length; i++) {
            weightSum += conditionalWeights[i];
        }
        for(int i=0; i<conditionalWeights.length; i++) {
            double weight = conditionalWeights[i];
            weight = weight /weightSum;
            conditionalWeights[i]=weight;
        }
        result = new ConditionalDistribution(conditionalMeans, conditionalCovs, conditionalWeights);
        return result;
    }


    /**
     * Find Maximum by gradient-quadratic search.
     * First a conditional distribution is derived from the kde.
     * @param start
     * @return
     */
    public SearchResult gradQuadrSearch(SimpleMatrix start){


        SimpleMatrix condVector = new SimpleMatrix(4,1);
        for(int i=0; i<condVector.numRows(); i++){
            condVector.setValue2D(i, 0, start.getValue2D(i, 0));
        }
        ConditionalDistribution conditionalDist = getConditionalDistribution(condVector);

        SimpleMatrix[] means = conditionalDist.conditionalMeans;
        SimpleMatrix[] covs = conditionalDist.conditionalCovs;
        double[] weights = conditionalDist.conditionalWeights;

        SimpleMatrix gradient = new SimpleMatrix(2,1);
        SimpleMatrix hessian = new SimpleMatrix(2,2);
        double n = means[0].numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);

        SimpleMatrix x = new SimpleMatrix(2,1);
        x.setValue2D(0, 0, start.getValue2D(start.numRows() - 2, 0));
        x.setValue2D(1, 0, start.getValue2D(start.numRows() - 1, 0));
        double[] mahalanobisDistances;
        double step = 1;
        double probability = 0;
        SimpleMatrix gradStep = null;
        do {
            mahalanobisDistances = mahalanobis(x, means, covs);
            //calculate gradient and hessian:
            double prob = 0;
            for (int i = 0; i < means.length; i++) {
                // check wether the component actually contributes to to the density at given point
                if(mahalanobisDistances[i] < MAX_MAHALANOBIS_DIST) {
                    SimpleMatrix m = means[i];

                    SimpleMatrix dm = m.minus(x);
                    SimpleMatrix c = covs[i];


                    SimpleMatrix invC = c.invert();
                    double w = weights[i];
                    //probability p(x,m)
                    double p = ((1 / (a * Math.sqrt(c.determinant()))) * Math.exp((-0.5d) * mahalanobisDistances[i])) * w;
                    prob += p;
                    gradient = gradient.plus( invC.mult(dm).scale(p) );
                    hessian = hessian.plus( invC.mult( dm.mult(dm.transpose()).minus(c) ).mult(invC).scale(p) );
                }


            }
            // save x
            SimpleMatrix xOld = x.copy();
            SimpleEVD hessianEVD = hessian.eig();
            int maxEVIndex = hessianEVD.getIndexMax();
            if(hessianEVD.getEigenvalue(maxEVIndex).getReal() < 0){
                gradStep = hessian.invert().mult(gradient);
                x = xOld.minus(gradStep);
            }
            double prob1 = 	evaluate(x, means, covs, weights);
            if( prob1 <= prob || hessianEVD.getEigenvalue(maxEVIndex).getReal() >= 0) {
                gradStep = gradient.scale(step);
                x = xOld.plus(gradStep);
                while(evaluate(x, means, covs, weights) < prob){
                    step = step/2;
                    gradStep = gradient.scale(step);
                    x = xOld.plus(gradStep);
                }
            }
            probability =	evaluate(x, means, covs, weights);
        }while(gradStep.elementMaxAbs() > 1E-10);

        return new SearchResult(x, probability);
    }


    public double evaluateMatrix(SimpleMatrix pointVector) {
        SimpleMatrix[] means = this.getSubMeans();
        SimpleMatrix[] covs = this.getSubSmoothedCovariances();
        double[] weights =  this.getSubWeights();
        double d = 0d;
        double n = means[0].numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);

        double[] mahalanobisDistances = mahalanobis(pointVector, means, covs);

        for (int i = 0; i < means.length; i++) {
            // check wether the component actually contributes to to the density at given point
            if(mahalanobisDistances[i] < MAX_MAHALANOBIS_DIST) {
                SimpleMatrix m = means[i];
                SimpleMatrix c = covs[i];
                double w = weights[i];
                d += ((1 / (a * Math.sqrt(c.determinant()))) * Math.exp((-0.5d) * mahalanobisDistances[i])) * w;
            }
        }
        return d;
    }

    public double evaluate(SimpleMatrix pointVector, SimpleMatrix[] means, SimpleMatrix[] covs, double[] weights) {
        double d = 0d;
        double n = means[0].numRows();
        double a = Math.pow(Math.sqrt(2 * Math.PI), n);

        double[] mahalanobisDistances = mahalanobis(pointVector, means, covs);

        for (int i = 0; i < means.length; i++) {
            // check wether the component actually contributes to to the density at given point
            if(mahalanobisDistances[i] < MAX_MAHALANOBIS_DIST) {
                SimpleMatrix m = means[i];
                SimpleMatrix c = covs[i];
                double w = weights[i];
                d += ((1 / (a * Math.sqrt(c.determinant()))) * Math.exp((-0.5d) * mahalanobisDistances[i])) * w;
            }
        }
        return d;
    }

    public double[] mahalanobis(SimpleMatrix x, SimpleMatrix[] means, SimpleMatrix[] covs) {
        double[] mahalanobisDistances = new double[means.length];
        for (int i = 0; i < means.length; i++) {
            SimpleMatrix m = means[i];
            SimpleMatrix c = covs[i];
            double distance = x.minus(m).transpose().mult(c.invert()).mult(x.minus(m)).trace();
            mahalanobisDistances[i]=(distance);
        }
        return mahalanobisDistances;
    }


    public void resetProbabilityCache(){
        mProbabilityCache.clear();
    }





    public void setBandwidthMatrix(SimpleMatrix mBandwidthMatrix) {
        this.mBandwidthMatrix = mBandwidthMatrix;
        for (int i=0;i<mSubDistributions.length;i++) {
            BaseSampleDistribution d = mSubDistributions[i];
            d.setBandwidthMatrix(mBandwidthMatrix);
        }
    }

    public void setNoOfCompsThreshold(double threshold) {
        mNoOfCompsThreshold = threshold;
    }

    public double getNoOfCompsThreshold() {
        return this.mNoOfCompsThreshold;
    }

    public void removeSubDistributions(int index) {
        BaseSampleDistribution[] array2=new BaseSampleDistribution[mSubDistributions.length-1];
        for(int i=0;i<mSubDistributions.length;i++){
            if(i<index){
                array2[i]=mSubDistributions[i];
            }
            else if(i>index){
                array2[i]=mSubDistributions[i-1];
            }
        }
        mSubDistributions=array2;
    }
}