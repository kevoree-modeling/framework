package org.kevoree.modeling.util.maths.gmm;


import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;

import java.util.ArrayList;
import java.util.List;

public class Hellinger {

    private static final double MIN_TOL = 1e-5;

    private static final double HALF = 0.5;

    public static double calculateUnscentedHellingerDistance(OneComponentDistribution dist1, TwoComponentDistribution dist2) throws Exception {
        ThreeComponentDistribution dist0 = mergeSampleDists(dist1, dist2, HALF, HALF);

        List<SigmaPoint> sigmaPoints = getAllSigmaPoints(dist0, 3);
        //System.out.println("sigmapoints: " + sigmaPoints.size());
        SimpleMatrix[] points = new  SimpleMatrix[sigmaPoints.size()];
        double[] weights = new double[sigmaPoints.size()];
        for (int i=0;i<sigmaPoints.size();i++) {
           points[i]=sigmaPoints.get(i).getmPointVecor();
            weights[i]=sigmaPoints.get(i).getmWeight();
            //System.out.println(p.getmPointVecor() + " - " + p.getmWeight());
        }

        double[] dist1Ev = dist1.evaluate(points);
        double[] dist2Ev = dist2.evaluate(points);
        dist1Ev = CommonOps.setNegativeValuesToZero(dist1Ev);
        dist2Ev = CommonOps.setNegativeValuesToZero(dist2Ev);

        double[] dist0Ev = dist0.evaluate(points);
        dist0Ev = CommonOps.setNegativeValuesToZero(dist0Ev);

        SimpleMatrix mat0 = CommonOps.doubleListToMatrix(dist0Ev);
        SimpleMatrix mat1 = CommonOps.doubleListToMatrix(dist1Ev);
        SimpleMatrix mat2 = CommonOps.doubleListToMatrix(dist2Ev);
        SimpleMatrix weightsMatrix = CommonOps.doubleListToMatrix(weights);
        SimpleMatrix g = CommonOps.elemPow((CommonOps.elemSqrt(mat1).minus(CommonOps.elemSqrt(mat2))), 2);
        SimpleMatrix tmp = weightsMatrix.elementMult(g);
        CommonOps.elementDiv(tmp.getMatrix(), mat0.getMatrix(), tmp.getMatrix());
        double val = tmp.elementSum();
        double H = Math.sqrt(Math.abs(val / 2));
        //System.out.println("Hellinger dist: " + H);
        return H;
    }

    private static ThreeComponentDistribution mergeSampleDists(OneComponentDistribution dist1, TwoComponentDistribution dist2, double w1, double w2) {
        SimpleMatrix[] means = new SimpleMatrix[3];
        means[0] = dist1.getGlobalMean();
        for (int i = 1; i < dist2.getSubMeans().length + 1; i++) {
            means[i] = dist2.getSubMeans()[i - 1];
        }

        SimpleMatrix[] covs = new SimpleMatrix[3];
        covs[0] = dist1.getGlobalCovariance();
        for (int i = 1; i < dist2.getSubCovariances().length + 1; i++) {
            covs[i] = dist2.getSubCovariances()[i - 1];
        }

        double[] weights = new double[3];
        weights[0] = w1;
        for (int i = 1; i < dist2.getSubWeights().length + 1; i++) {
            weights[i] = dist2.getSubWeights()[i - 1] * w2;
        }

        ThreeComponentDistribution dist = null;
        dist = new ThreeComponentDistribution(weights, means, covs, dist1.getBandwidthMatrix());

        return dist;
    }

    public static List<SigmaPoint> getAllSigmaPoints(ThreeComponentDistribution distribution, int max) throws Exception {
        ArrayList<SigmaPoint> sigmaPoints = new ArrayList<SigmaPoint>();
        int noOfComponents = distribution.getSubMeans().length;
        int dim = distribution.getSubMeans()[0].numRows();
        int k = max - dim;
        int noOfSigmaPoints;
        if (k != 0)
            noOfSigmaPoints = 2 * dim + 1;
        else
            noOfSigmaPoints = 2 * dim;
        ArrayList<Double> weights = new ArrayList<Double>();
        for (int i = 0; i < (2 * dim); i++) {
            weights.add(1d / (2 * ((double) dim + k)));
        }
        if (k != 0)
            weights.add((double) k / (double) (dim + k));
        double sum = 0;
        for (int j=0;j<weights.size();j++) {
            sum += weights.get(j);
        }
        if ((sum - 1) > MIN_TOL)
            throw new Exception("Weights in the unscented transform should sum to one!");

        for (int i = 0; i < noOfComponents; i++) {
            List<SimpleMatrix> x = getSigmaPoints(distribution.getSubMeans()[i], distribution.getSubCovariances()[i], noOfSigmaPoints, k);
            int count = 0;
            double componentWeight = distribution.getSubWeights()[i];
            for (int d=0;d<x.size();d++) {
                SimpleMatrix m=x.get(d);
                sigmaPoints.add(new SigmaPoint(m, weights.get(count) * componentWeight, weights.get(count)));
                count++;
            }
        }

        return sigmaPoints;
    }

    /**
     * Returns 2n+k sigma points starting with mean as the first point
     *
     * @param mean
     * @param cov
     * @param no
     * @param k
     * @return
     */
    private static List<SimpleMatrix> getSigmaPoints(SimpleMatrix mean, SimpleMatrix cov, int no, int k) {
        List<SimpleMatrix> resultVectors = new ArrayList<SimpleMatrix>();

        int n = cov.numRows();
        SimpleSVD<?> svd = cov.svd(true);
        SimpleMatrix U = svd.getU();
        SimpleMatrix S = svd.getW();

        S = U.mult(CommonOps.elemSqrt(S)).scale(Math.sqrt(n + k));

        for (int i = 0; i < S.numCols(); i++) {
            SimpleMatrix columnVector = S.extractVector(false, i);
            SimpleMatrix negColumnVector = S.extractVector(false, i).scale(-1);
            resultVectors.add(columnVector.plus(mean));
            resultVectors.add(negColumnVector.plus(mean));
        }
        if (k != 0)
            resultVectors.add(mean);

        return resultVectors;
    }

}
