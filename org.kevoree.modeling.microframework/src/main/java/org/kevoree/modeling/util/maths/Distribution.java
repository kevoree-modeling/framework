package org.kevoree.modeling.util.maths;

public class Distribution {

    public static double inverseNormalCDF(double q) {
        double b[] = {
                1.570796288,
                0.03706987906,
                -0.8364353589e-3,
                -0.2250947176e-3,
                0.6841218299e-5,
                0.5824238515e-5,
                -0.104527497e-5,
                0.8360937017e-7,
                -0.3231081277e-8,
                0.3657763036e-10,
                0.6936233982e-12
        };
        if (q < 0.0 || 1.0 < q || q == 0.5) {
            return 0.0;
        }

        double w1 = q;
        if (q > 0.5) {
            w1 = 1.0 - q;
        }

        double w3 = -Math.log(4.0 * w1 * (1.0 - w1));
        w1 = b[0];
        for (int i = 1; i < 11; i++) {
            w1 += b[i] * Math.pow(w3, i);
        }

        return q > 0.5 ? Math.sqrt(w1 * w3) : -Math.sqrt(w1 * w3);
    }

    public static double normal(double[] features, double[] means, double[] variances) {
        int dim = features.length;
        double p = 1;

        for (int i = 0; i < dim; i++) {
            p = p * (1 / Math.sqrt(2 * Math.PI * variances[i])) * Math.exp(-((features[i] - means[i]) * (features[i] - means[i])) / (2 * variances[i]));
        }
        return p;
    }

    public static double[] parrallelNormal(double[] features, double[] means, double[] variances) {
        int dim = features.length;
        double[] p = new double[dim];

        for (int i = 0; i < dim; i++) {
            p[i] = (1 / Math.sqrt(2 * Math.PI * variances[i])) * Math.exp(-((features[i] - means[i]) * (features[i] - means[i])) / (2 * variances[i]));
        }
        return p;
    }

    public static double singleNormal(double feature, double mean, double variance) {
        return (1 / Math.sqrt(2 * Math.PI * variance)) * Math.exp(-((feature - mean) * (feature - mean)) / (2 * variance));
    }
}
