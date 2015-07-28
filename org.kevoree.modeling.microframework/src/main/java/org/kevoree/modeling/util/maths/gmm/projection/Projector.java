/*
 * Copyright 2014 Jonas Luethke
 */


package org.kevoree.modeling.util.maths.gmm.projection;


import org.kevoree.modeling.util.maths.gmm.BaseSampleDistribution;
import org.kevoree.modeling.util.maths.gmm.MomentMatcher;
import org.kevoree.modeling.util.maths.gmm.SampleModel;
import org.kevoree.modeling.util.maths.gmm.TwoComponentDistribution;
import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;



/**
 * This class transforms a sample distribution that is lying in a degenerate
 * space into the non-degenerate subspace by projecting
 * 
 * @author Jonas Luethke
 * 
 */
public class Projector {
	private static final double MIN_VALUE = 1E-7d;

	private static final double CONST_SMALL_FACTOR = 1E-10d;


	public static ProjectionData projectSampleDistToSubspace(SampleModel distribution) throws RuntimeException {
		ProjectionData projectionData = new ProjectionData();
		MomentMatcher.matchMoments(distribution);
		SimpleMatrix globalSmoothedCov = distribution.getmGlobalCovarianceSmoothed();
		SimpleMatrix globalMean = distribution.getGlobalMean();
		int globalCovSize = globalSmoothedCov.numRows();
		SimpleMatrix identity = SimpleMatrix.identity(globalCovSize);
		globalSmoothedCov = globalSmoothedCov.plus(identity.scale(CONST_SMALL_FACTOR));
		int d = globalSmoothedCov.numCols();
		// calculate eigen directions --> determine the subspace
		projectionData.mSVD = globalSmoothedCov.svd(true);
		SimpleSVD<?> svd = globalSmoothedCov.svd(true);
		SimpleMatrix U = svd.getU();
		SimpleMatrix S = svd.getW();
		SimpleMatrix V = svd.getV();

		V = U;

		SimpleMatrix s = S.extractDiag();
		s = s.scale(1 / S.elementMaxAbs());

		double[] validElements = new double[d];
		int countValidElements = 0;
		SimpleMatrix invS = null;
		// if S is almost zero --> singular
		if (s.elementMaxAbs() < MIN_VALUE || Double.isNaN(s.elementSum())) {
			S = SimpleMatrix.identity(S.numCols()).transpose();
			invS = SimpleMatrix.identity(d).scale(2d / MIN_VALUE);
			for (int i = 0; i < validElements.length; i++)
				validElements[i] = 1d;
			countValidElements = validElements.length;
		} else {
			// if S ins not completely singular: check what elements are >0
			// store them in validElements
			for (int i = 0; i < validElements.length; i++) {
				if (s.getValue2D(i, 0) > MIN_VALUE) {
					validElements[i] = 1d;
					countValidElements++;
				} else
					validElements[i] = s.getValue2D(i, 0);
			}
			// create inverse matrix of S
			S = CommonOps.elemPow(S, -1);
			invS = SimpleMatrix.identity(d).scale(0);
			for (int i = 0; i < validElements.length; i++) {
				if (validElements[i] == 1)
					invS.setValue2D(i, i, S.getValue2D(i, i));
				else
					invS.setValue2D(i, i, 1d / validElements[i]);
			}
		}
		// TODO: remove this test data
		/*
		 * validElements = new Double[2]; validElements[0] = 1d;
		 * validElements[1] = 0d; countValidElements = 1;
		 */

		SimpleMatrix trnsF = CommonOps.elemSqrt(CommonOps.abs(invS)).mult(V.invert());

		// forward transform the pdf and remove non-valid eigendirections
		SimpleMatrix trnsBandwidthMatrix = transformMatrix(trnsF, distribution.getBandwidthMatrix(), validElements, countValidElements);
		BaseSampleDistribution[] subDistributions = distribution.getSubDistributions();
		for (int i = 0; i < subDistributions.length; i++) {
			SimpleMatrix[] subSubMeans = new SimpleMatrix[0];
			SimpleMatrix[] subSubCovs = new SimpleMatrix[0];
			if (subDistributions[i] instanceof TwoComponentDistribution) {
				TwoComponentDistribution subDist = (TwoComponentDistribution) subDistributions[i];
				subSubMeans = subDist.getSubMeans();
				subSubCovs = subDist.getSubCovariances();
			}
			// if subcomponent has subcomponents transform also the means and
			// covariances of these subsubcomponents:
			if (subSubMeans.length > 1) {
				for (int j = 0; j < subSubMeans.length; j++) {
					subSubCovs[j] = transformMatrix(trnsF, subSubCovs[j], validElements, countValidElements);
					SimpleMatrix tmp = trnsF.mult(subSubMeans[j].minus(globalMean));
					tmp = CommonOps.deleteElementsFromVector(tmp, validElements, countValidElements);
					subSubMeans[j] = tmp;
				}
				try {
					((TwoComponentDistribution) subDistributions[i]).setSubMeans(subSubMeans);
					((TwoComponentDistribution) subDistributions[i]).setSubCovariances(subSubCovs);
				} catch (Exception e) {
					// This exception can't be thrown because sample models can
					// only contain subcomponents with at most 2 components
					e.printStackTrace();
				}
				//MomentMatcher.matchMoments(((TwoComponentDistribution) subDistributions[i]));
			}
			// transform subcomponents
			SimpleMatrix subMean = subDistributions[i].getGlobalMean();
			SimpleMatrix tmp = trnsF.mult(subMean.minus(globalMean));
			tmp = CommonOps.deleteElementsFromVector(tmp, validElements, countValidElements);
			subDistributions[i].setGlobalMean(tmp);
			SimpleMatrix subCov = subDistributions[i].getGlobalCovariance();
			subCov = transformMatrix(trnsF, subCov, validElements, countValidElements);
			subDistributions[i].setGlobalCovariance(subCov);
			subDistributions[i].setBandwidthMatrix(trnsBandwidthMatrix);
		}
		// transform also the global covariance
		globalSmoothedCov = transformMatrix(trnsF, globalSmoothedCov, validElements, countValidElements);
		distribution.setBandwidthMatrix(trnsBandwidthMatrix);
		projectionData.mCountValidElements = countValidElements;
		projectionData.mValidElements = validElements;
		projectionData.mGlobalMean = globalMean;
		return projectionData;
	}

	private static SimpleMatrix transformMatrix(SimpleMatrix trnsF, SimpleMatrix matrix, double[] validElements, int countValidElements) {
		// forward transform the pdf and remove non-valid eigendirections
		//if(matrix.getNumRows()!= countValidElements)
		//	System.out.println("projection was necessary!!!");
		SimpleMatrix tmp = trnsF.mult(matrix).mult(trnsF.transpose());
		SimpleMatrix trnsMatrix = new SimpleMatrix(countValidElements, countValidElements);
		int row=0, column=0;
		for(int i=0; i< validElements.length; i++){
			for(int j=0; j< validElements.length; j++){
				if(validElements[i] == 1 && validElements[j] == 1)
					trnsMatrix.setValue2D(row, column++, tmp.getValue2D(i, j));
			}
			column = 0;
			row++;
		}
		return trnsMatrix;
	}

	private static SimpleMatrix backTransformMatrix(SimpleMatrix matrix, SimpleMatrix matrixToTransform, double[] validElements) {
		// add removed eigendirections and backwards transform
		int row=0, column=0;
		for(int i=0; i< validElements.length; i++){
			for(int j=0; j< validElements.length; j++){
				if(validElements[i] == 1 && validElements[j] == 1)
					matrix.setValue2D(i, j, matrixToTransform.getValue2D(row, column++));
			}
			column = 0;
			row++;
		}
		
		/*
		
		int a=0,b=0;
		for (int row = 0; row < matrix.getNumRows(); row++) {
			for (int column = 0; column < matrix.getNumCols(); column++) {
				if (validElements[row] == 1)
					matrix.set(row, column, matrixToTransform.get(row, column));
				else if (validElements[column] == 1)
					matrix.set(row, column, matrixToTransform.get(row, column));
				else
					matrix.set(row, column, 0);
			}
		}*/
		return matrix;
	}

	public static void projectSampleDistToOriginalSpace(SampleModel distribution, ProjectionData projectionData)  {
		// add missing nullspace to pdf
		/*
		 * num_nullDir = size(svdRes.S,1) - length(svdRes.id_valid) ; F_trns =
		 * svdRes.V*sqrt(svdRes.S) ; C_prot = zeros(size(svdRes.S)) ;
		 */
		SimpleMatrix bandwidth = distribution.getBandwidthMatrix();
		SimpleSVD<?> svd = projectionData.mSVD;
		SimpleMatrix U = svd.getU();
		SimpleMatrix S = svd.getW();
		SimpleMatrix V = svd.getV();
		SimpleMatrix globalMean = projectionData.mGlobalMean;
		double[] validElements = projectionData.mValidElements;
		int countValidElements = projectionData.mCountValidElements;
		int noOfNullDirs = S.numCols() - countValidElements;
		SimpleMatrix trnsF = V.mult(CommonOps.elemSqrt(S));
		SimpleMatrix protC = S.scale(0);
		// transform bandwidth
		protC = backTransformMatrix(protC, bandwidth, validElements);
		bandwidth = trnsF.mult(protC).mult(trnsF.transpose());
		// set bandwidth in distribution
		distribution.setBandwidthMatrix(bandwidth);

		// transform means an covariances of sub components
		BaseSampleDistribution[] subDistributions = distribution.getSubDistributions();
		for (int i = 0; i < subDistributions.length; i++) {
			SimpleMatrix[] subSubMeans = new SimpleMatrix[0];
			SimpleMatrix[] subSubCovs = new SimpleMatrix[0];
			if (subDistributions[i] instanceof TwoComponentDistribution) {
				TwoComponentDistribution subDist = (TwoComponentDistribution) subDistributions[i];
				subSubMeans = subDist.getSubMeans();
				subSubCovs = subDist.getSubCovariances();
			}
			// if subcomponent has subcomponents transform also the means and
			// covariances of these subsubcomponents:
			if (subSubMeans.length > 1) {
				for (int j = 0; j < subSubMeans.length; j++) {
					//resize mean
					SimpleMatrix newMean = new SimpleMatrix(subSubMeans[j].numRows()+noOfNullDirs,1);
					newMean = setVectorElements(newMean, subSubMeans[j],validElements);
					// transform mean
					SimpleMatrix tmp = trnsF.mult(newMean).plus(globalMean);
					subSubMeans[j] = tmp;
					
					// transform covariance
					protC = protC.scale(0);
					subSubCovs[j] = backTransformMatrix(protC, subSubCovs[j], validElements);
					subSubCovs[j] = trnsF.mult(subSubCovs[j]).mult(trnsF.transpose());
				}
				try {
					((TwoComponentDistribution) subDistributions[i]).setSubMeans(subSubMeans);
					((TwoComponentDistribution) subDistributions[i]).setSubCovariances(subSubCovs);
				} catch (Exception e) {
					// This exception can't be thrown because sample models can
					// only contain subcomponents with at most 2 components
					e.printStackTrace();
				}
				//MomentMatcher.matchMoments(((TwoComponentDistribution) subDistributions[i]));
			}
			// transform subcomponents
			SimpleMatrix subMean = subDistributions[i].getGlobalMean();
			SimpleMatrix newMean = new SimpleMatrix(subMean.numRows()+noOfNullDirs,1);
			newMean = setVectorElements(newMean, subMean,validElements);
			SimpleMatrix tmp = trnsF.mult(newMean).plus(globalMean);
			subDistributions[i].setGlobalMean(tmp);
			SimpleMatrix subCov = subDistributions[i].getGlobalCovariance();
			// transform covariance
			protC = protC.scale(0);
			subCov = backTransformMatrix(protC, subCov, validElements);
			subCov = trnsF.mult(subCov).mult(trnsF.transpose());
			subDistributions[i].setGlobalCovariance(subCov);
			subDistributions[i].setBandwidthMatrix(bandwidth);
		}
	}
	private static SimpleMatrix setVectorElements(SimpleMatrix v1, SimpleMatrix v2, double[] elementsInV1) {
		int j = 0;
		for (int i = 0; i < v1.numRows(); i++)
			if (elementsInV1[i] == 1)
				v1.setValue2D(i, 0, v2.getValue1D(j++));
		return v1;
	}
}
