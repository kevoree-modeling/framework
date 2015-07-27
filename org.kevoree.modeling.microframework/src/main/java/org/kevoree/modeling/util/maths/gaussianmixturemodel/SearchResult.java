/*
 * Copyright 2014 Jonas Luethke
 */


package org.kevoree.modeling.util.maths.gaussianmixturemodel;

import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

public class SearchResult {

	public SimpleMatrix point;
	public double probability;
	public SearchResult(SimpleMatrix point, double probability) {
		this.point = point;
		this.probability = probability;
	}
	
	

}
