package org.kevoree.modeling.util.maths;

/**
 * Created by assaad on 02/07/15.
 */
public class Correlations {

    public static double pearson(double[] x, double[] y) {
        double meanX = 0.0, meanY = 0.0;
        for (int i = 0; i < x.length; i++) {
            meanX += x[i];
            meanY += y[i];
        }

        meanX /= x.length;
        meanY /= x.length;

        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;
        for (int i = 0; i < x.length; i++) {
            sumXY += ((x[i] - meanX) * (y[i] - meanY));
            sumX2 += (x[i] - meanX)*(x[i] - meanX);
            sumY2 += (y[i] - meanY)*(y[i] - meanY);
        }

        return (sumXY / (Math.sqrt(sumX2) * Math.sqrt(sumY2)));
    }
}
