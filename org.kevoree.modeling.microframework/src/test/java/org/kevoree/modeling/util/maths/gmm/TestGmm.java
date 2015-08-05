package org.kevoree.modeling.util.maths.gmm;

import org.junit.Test;
import org.kevoree.modeling.util.maths.matrix.SimpleMatrix;

import java.util.Random;


/**
 * Created by assaad on 29/07/15.
 */
public class TestGmm {

    // disable the forgetting factor
    private static final double forgettingFactor = 1;
    // set the compression threshold
    private static final double compressionThreshold = 0.02;


    public void testgmm(){
        SampleModel sm= new SampleModel();
        sm.setSampleModelForget(forgettingFactor,compressionThreshold);

        Random random=new Random();

        try {

            SimpleMatrix[] pos = new SimpleMatrix[10];
            SimpleMatrix[] c = new SimpleMatrix[10];
            double[] dd= new double[10];
            for(int i=0;i<10;i++) {
                pos[i] = new SimpleMatrix(2, 1);
                pos[i].setValue1D(0, random.nextGaussian() * 40 + 100);
                pos[i].setValue1D(1, random.nextGaussian() * 200 + 2000);
                c[i] = new SimpleMatrix(2, 2);
                dd[i]=1;
            }

            sm.updateDistributionArrayMatrix(pos, c, dd);


            for(int i=10;i<1000;i++) {
                SimpleMatrix pos2 = new SimpleMatrix(2, 1);
                pos2.setValue1D(0, random.nextGaussian() * 40 + 100);
                pos2.setValue1D(1, random.nextGaussian() * 200 + 2000);
                SimpleMatrix c2 = new SimpleMatrix(2, 2);
                sm.updateDistributionValues(pos2, c2, 1d);
            }
            int size = 100;
            double[] x = new double[size];
            double[] y= new double[size];

            for(int i=0;i<size;i++){
                x[i]=300*i/size;
                y[i]=4000*i/size;
            }

            double[][] proba = getProbabilities(x,y,sm);
            int sx=0;



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double[][] getProbabilities(double[] x, double[] y, SampleModel sm) {
        double[][] z = new double[y.length][x.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                SimpleMatrix pointVector = new SimpleMatrix(2,1);
                pointVector.setValue1D(0,x[i]);
                pointVector.setValue1D(1,y[j]);
                z[j][i] = sm.evaluateMatrix(pointVector);
            }
        }
        return z;
    }
}
