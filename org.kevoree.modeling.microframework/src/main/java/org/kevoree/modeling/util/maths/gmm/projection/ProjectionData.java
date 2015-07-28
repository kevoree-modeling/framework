/*
 * Copyright 2014 Jonas Luethke
 */


package org.kevoree.modeling.util.maths.gmm.projection;


import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;
import org.kevoree.modeling.util.maths.matrix.solvers.SimpleSVD;

public class ProjectionData {

	public SimpleSVD<?> mSVD;
	public double[] mValidElements;
	public int mCountValidElements;
	public SimpleMatrix mBandwidthMatrix;
	public SimpleMatrix mGlobalMean;
}
