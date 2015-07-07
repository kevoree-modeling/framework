package org.kevoree.modeling.util.maths;

public class Statistic {

    public static void calcHistogram(double[] data, double[] dataratings, int numBins) {
        final int[] result = new int[numBins];
        double max;
        double min;
        min = data[0];
        max = data[0];
        for (int i = 0; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
            }
            if (data[i] > max) {
                max = data[i];
            }
        }
        final double binSize = (max - min) / numBins;
        for (int i = 0; i < data.length; i++) {
            int bin = (int) ((data[i] - min) / binSize);
            result[bin]++;
        }
    }
}
