package org.kevoree.modeling.util.maths.gmm;


import org.kevoree.modeling.util.maths.gmm.projection.ProjectionData;
import org.kevoree.modeling.util.maths.gmm.projection.Projector;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

import java.lang.reflect.InvocationTargetException;

public class Compressor {
    private final static double CONST_SMALL_TOLERANCE = 1E-10;
    private final static double MAX=1.7976931348623157E308;

    //private final static double D_TH = 0.1;

    private static final double MIN_EM_DISTANCE = 2.34;

    private static final double INC_TH_SCALE = 1.5;
    private static final double DEC_TH_SCALE = 0.6;
    private static final double CHECK_IF_DEC_SCALE = 0.5;

    private static void setNoOfComponentsThreshold(SampleModel dist, int noOfCompsBeforeCompression, int noOfCompsAfterCompression) {
        double threshold = dist.getNoOfCompsThreshold();
        if (noOfCompsAfterCompression > threshold)
            threshold = threshold * INC_TH_SCALE;
        else if (noOfCompsAfterCompression <= threshold * CHECK_IF_DEC_SCALE)
            threshold = threshold * DEC_TH_SCALE;
        dist.setNoOfCompsThreshold(threshold);
    }

    public static boolean emUpdate(SampleModel dist, int[] updatePoints) {
        BaseSampleDistribution[] subDistributions = dist.getSubDistributions();
        SimpleMatrix[] means = dist.getSubMeans();
        SimpleMatrix[] smoothedCovariances = dist.getSubSmoothedCovariances();
        SimpleMatrix[] covariances = dist.getSubCovariances();
        double[] weights = dist.getSubWeights();
        boolean pointMerged = false;
        int count = 0;
        for(int k=0;k<updatePoints.length;k++) {
            int point= updatePoints[k];
            for(int i=0; i<means.length && i!=point && !pointMerged; i++){
                if(subDistributions[i] instanceof TwoComponentDistribution) {
                    TwoComponentDistribution subComponent = (TwoComponentDistribution)subDistributions[i];
                    // calculate mahalanobis distance (x-m)L(x-m)' to each mean until one is small enough
                    double md = means[point].minus(means[i]).transpose().mult( smoothedCovariances[i].invert() ).mult( means[point].minus(means[i]) ).trace();
                    if(md < MIN_EM_DISTANCE) {
                        // just add the new point to sub model

                        // which subcomponent is closest?
                        OneComponentDistribution[] subSubComponents = subComponent.getSubComponents();
                        double distance1 = euclidianDistance(subSubComponents[0].getGlobalMean(), means[point]);
                        double distance2 = euclidianDistance(subSubComponents[1].getGlobalMean(), means[point]);

                        int mergeId = 0;
                        if(distance1 < distance2)
                            mergeId = 0;
                        else
                            mergeId = 1;
                        OneComponentDistribution componentToMerge = subSubComponents[mergeId];


                        SimpleMatrix[] meansArray = { componentToMerge.getGlobalMean(), means[point] };
                        SimpleMatrix[] covarianceArray = { componentToMerge.getGlobalCovariance(), covariances[point] };

                        double subSubweight1 = componentToMerge.getGlobalWeight()*subComponent.getGlobalWeight();
                        double subSubweight2 = weights[point];
                        double globalWeight = subComponent.getGlobalWeight() + subSubweight2;
                        double subSubWeightSum = subSubweight1 + subSubweight2;
                        subSubweight1 /= subSubWeightSum;
                        subSubweight2 /= subSubWeightSum;

                        double[] weightsArray = { subSubweight1, subSubweight2 };


                        OneComponentDistribution oneCompDist = null;
                        try {
                            TwoComponentDistribution twoCompDist = new TwoComponentDistribution(weightsArray, meansArray, covarianceArray, dist.getBandwidthMatrix());
                            double subWeight1 = subSubComponents[0].getGlobalWeight()*subComponent.getGlobalWeight();
                            double subWeight2 = subSubComponents[1].getGlobalWeight()*subComponent.getGlobalWeight();
                            if(mergeId == 0)
                                subWeight1 += weights[point];
                            else
                                subWeight2 += weights[point];
                            double subWeightSum = subWeight1+subWeight2;
                            subWeight1 /= subWeightSum;
                            subWeight2 /= subWeightSum;

                            MomentMatcher.matchMoments2Comp(twoCompDist);
                            oneCompDist = new OneComponentDistribution();
                            oneCompDist.setComponent(twoCompDist);
                            subSubComponents[mergeId] = oneCompDist;
                            subSubComponents[0].setGlobalWeight(subWeight1);
                            subSubComponents[1].setGlobalWeight(subWeight2);
                            MomentMatcher.matchMoments2Comp(subComponent);
                            subComponent.setGlobalWeight(globalWeight);

                            dist.mEMCount++;
							/*double compressionError = Hellinger.calculateUnscentedHellingerDistance(oneCompDist, twoCompDist);
							dist.mEMError = (dist.mEMError*dist.mEMCount + compressionError)/(dist.mEMCount + 1);
							dist.mEMCount++;*/
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
						/*
						Double[] subWeights = twoCompDist.getSubWeights();
						double newWeight1 = subWeights[0] / (subWeights[0] + subWeights[1]);
						double newWeight2 = subWeights[1] / (subWeights[0] + subWeights[1]);
						twoCompDist.getSubComponents()[0].setGlobalWeight(newWeight1);
						twoCompDist.getSubComponents()[1].setGlobalWeight(newWeight2);
						dist.getSubDistributions().set(i, twoCompDist);
						*/

                        dist.removeSubDistributions(point);
                        count++;
                        pointMerged = true;
                    }
                }
            }
        }
        return (count == updatePoints.length);
    }

    public static void compress(SampleModel dist, int[] newComponents) throws Exception {
        // check wether compression is necessary using hysteresis rule
        if (dist.getSubMeans().length <= dist.getNoOfCompsThreshold())
            return;
        // try em update
        boolean successfulEMUpdate = emUpdate(dist, newComponents);
        if(successfulEMUpdate) {
            return;
        }
        ProjectionData projectionData = null;
        try {
            projectionData = Projector.projectSampleDistToSubspace(dist);
        } catch(Exception e) {
            // if projection fails: stop compression
            System.out.println("projection failed. Aborted Compression");
            return;
        }
        revitalizeComponents(dist);
        //System.out.println("COMPRESS");
        int noOfCompsBeforeCompression = dist.getSubMeans().length;
        SampleModel inputModelCopy = new SampleModel();
        inputModelCopy.setSampleModel(dist);
        double compressionError = MAX;
        if(inputModelCopy.getSubDistributions().length > 1)
            compressionError = mergeTwoClosestComps(inputModelCopy);
        while (compressionError < dist.mCompressionThreshold) {
            dist.overWirite(inputModelCopy);
            if(inputModelCopy.getSubDistributions().length > 1)
                compressionError = mergeTwoClosestComps(inputModelCopy);
            else
                compressionError = MAX;
        }
        Projector.projectSampleDistToOriginalSpace(dist, projectionData);
        int noOfCompsAfterCompression = dist.getSubMeans().length;
        setNoOfComponentsThreshold(dist, noOfCompsBeforeCompression, noOfCompsAfterCompression);
    }

    private static void revitalizeComponents(SampleModel dist) throws Exception {
        // check which sub distributions have to be revitalized
        for (int i = 0; i < dist.getSubDistributions().length; i++) {
            if (dist.getSubDistributions()[i] instanceof TwoComponentDistribution) {
                TwoComponentDistribution subDist = (TwoComponentDistribution) dist.getSubDistributions()[i];
                double tmpWeight = subDist.getGlobalWeight();
                MomentMatcher.matchMoments2Comp(subDist);
                OneComponentDistribution oneCompDist = new OneComponentDistribution();
                oneCompDist.setComponent(subDist);
                double compressionError = Hellinger.calculateUnscentedHellingerDistance(oneCompDist, subDist);
                subDist.setGlobalWeight(tmpWeight);
                if (compressionError >= dist.mCompressionThreshold) {
                    OneComponentDistribution subComp1 = subDist.getSubComponents()[0];
                    OneComponentDistribution subComp2 = subDist.getSubComponents()[1];
                    BaseSampleDistribution splitDist1 = null, splitDist2 = null;
                    // check wether covariance of sub component is zero --> no splitting necessary
                    if(subComp1.getGlobalCovariance().elementSum() > CONST_SMALL_TOLERANCE)
                        splitDist1 = subComp1.split(tmpWeight);
                    else{
                        subComp1.scaleGlobalWeight(tmpWeight);
                        splitDist1 = subComp1;
                    }
                    // check wether covariance of sub component is zero --> no splitting necessary
                    if(subComp2.getGlobalCovariance().elementSum() > CONST_SMALL_TOLERANCE)
                        splitDist2 = subComp2.split(tmpWeight);
                    else{
                        subComp2.scaleGlobalWeight(tmpWeight);
                        splitDist2 = subComp2;
                    }
                    dist.getSubDistributions()[i]=splitDist1;
                    dist.addToSubDistribution(splitDist2);
                }
            }
        }
    }

    private static double mergeTwoClosestComps(SampleModel dist) throws RuntimeException, InvocationTargetException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException {
        double compressionError = 0;
        TwoComponentDistribution twoCompDist = null;
        SimpleMatrix[] means = dist.getSubMeans();
        SimpleMatrix[] covs = dist.getSubCovariances();
        double[] weights = dist.getSubWeights();
        double distance = -1d;
        int indexComp1 = 0, indexComp2 = 0;
        for (int i = 0; i < means.length; i++) {
            SimpleMatrix mean1 = means[i];
            for (int j = (i + 1); j < means.length; j++) {
                SimpleMatrix mean2 = means[j];
                double tmpDistance = euclidianDistance(mean1, mean2);
                if ((distance == -1) || (tmpDistance < distance)) {
                    distance = tmpDistance;
                    indexComp1 = i;
                    indexComp2 = j;
                }
                if (distance == 0)
                    break;
            }
            if (distance == 0)
                break;
        }
        SimpleMatrix[] meansArray = { means[indexComp1], means[indexComp2] };
        SimpleMatrix[] covarianceArray = { covs[indexComp1], covs[indexComp2] };
        double[] weightsArray = { weights[indexComp1], weights[indexComp2] };
        try {
            twoCompDist = new TwoComponentDistribution(weightsArray, meansArray, covarianceArray, dist.getBandwidthMatrix());
            MomentMatcher.matchMoments2Comp(twoCompDist);
            OneComponentDistribution oneCompDist = new OneComponentDistribution();
            oneCompDist.setComponent(twoCompDist);
            compressionError = Hellinger.calculateUnscentedHellingerDistance(oneCompDist, twoCompDist);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        Double[] subWeights = twoCompDist.getSubWeights();
        double newWeight1 = subWeights[0] / (subWeights[0] + subWeights[1]);
        double newWeight2 = subWeights[1] / (subWeights[0] + subWeights[1]);
        twoCompDist.getSubComponents()[0].setGlobalWeight(newWeight1);
        twoCompDist.getSubComponents()[1].setGlobalWeight(newWeight2);
        dist.getSubDistributions()[indexComp2]=twoCompDist;
        dist.removeSubDistributions(indexComp1);
        return compressionError;
    }

    public static double euclidianDistance(SimpleMatrix columnVector1, SimpleMatrix columnVector2) {
        double distance = 0;
        SimpleMatrix distVector = columnVector2.minus(columnVector1);
        distance = Math.sqrt(CommonOps.elemPow(distVector, 2).elementSum());
        return distance;
    }



}
