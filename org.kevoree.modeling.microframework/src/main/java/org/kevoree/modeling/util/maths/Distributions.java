package org.kevoree.modeling.util.maths;

/**
 * Created by assaad on 02/07/15.
 */
public class Distributions {

    public static double gaussian(double[] features, double[] means, double[] variances){
        int dim= features.length;
        double p=1;

        for(int i=0; i<dim; i++){
            p= p* (1/Math.sqrt(2*Math.PI*variances[i]))*Math.exp(-((features[i]-means[i])*(features[i]-means[i]))/(2*variances[i]));
        }
        return p;
    }

    public static double[] parrallelGaussian(double[] features, double[] means, double[] variances){
        int dim= features.length;
        double[] p=new double[dim];

        for(int i=0; i<dim; i++){
            p[i]=(1/Math.sqrt(2*Math.PI*variances[i]))*Math.exp(-((features[i]-means[i])*(features[i]-means[i]))/(2*variances[i]));
        }
        return p;
    }

    public static double singleGaussian(double feature, double mean, double variance){
            return (1/Math.sqrt(2*Math.PI*variance))*Math.exp(-((feature-mean)*(feature-mean))/(2*variance));
    }
}
