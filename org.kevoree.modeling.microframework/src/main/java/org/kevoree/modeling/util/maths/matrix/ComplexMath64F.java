package org.kevoree.modeling.util.maths.matrix;

/**
 * Created by assaad on 24/07/15.
 */
public class ComplexMath64F {
    public static void conj( Complex64F input , Complex64F conj ) {
        conj.real = input.real;
        conj.imaginary = -input.imaginary;
    }


    public static void plus( Complex64F a , Complex64F b , Complex64F result ) {
        result.real = a.real + b.real;
        result.imaginary = a.imaginary + b.imaginary;
    }

    public static void minus( Complex64F a , Complex64F b , Complex64F result ) {
        result.real = a.real - b.real;
        result.imaginary = a.imaginary - b.imaginary;
    }


    public static void multiply(Complex64F a, Complex64F b, Complex64F result) {
        result.real = a.real * b.real - a.imaginary*b.imaginary;
        result.imaginary = a.real*b.imaginary + a.imaginary*b.real;
    }


    public static void divide(Complex64F a, Complex64F b, Complex64F result) {
        double norm = b.getMagnitude2();
        result.real = (a.real * b.real + a.imaginary*b.imaginary)/norm;
        result.imaginary = (a.imaginary*b.real - a.real*b.imaginary)/norm;
    }

 /*
    public static void convert( Complex64F input , ComplexPolar64F output ) {
        output.r = input.getMagnitude();
        output.theta = Math.atan2(input.imaginary, input.real);
    }

    public static void convert( ComplexPolar64F input , Complex64F output ) {
        output.real = input.r*Math.cos(input.theta);
        output.imaginary = input.r*Math.sin(input.theta);
    }

    public static void multiply(ComplexPolar64F a, ComplexPolar64F b, ComplexPolar64F result)
    {
        result.r = a.r*b.r;
        result.theta = a.theta + b.theta;
    }


    public static void divide(ComplexPolar64F a, ComplexPolar64F b, ComplexPolar64F result)
    {
        result.r = a.r/b.r;
        result.theta = a.theta - b.theta;
    }


    public static void pow( ComplexPolar64F a , int N , ComplexPolar64F result )
    {
        result.r = Math.pow(a.r,N);
        result.theta = N*a.theta;
    }


    public static void root( ComplexPolar64F a , int N , int k , ComplexPolar64F result )
    {
        result.r = Math.pow(a.r,1.0/N);
        result.theta = (a.theta + 2.0*k*Math.PI)/N;
    }
*/

    public static void root( Complex64F a , int N , int k , Complex64F result )
    {
        double r = a.getMagnitude();
        double theta = Math.atan2(a.imaginary,a.real);

        r = Math.pow(r,1.0/N);
        theta = (theta + 2.0*k*Math.PI)/N;

        result.real = r*Math.cos(theta);
        result.imaginary = r*Math.sin(theta);
    }

    public static void sqrt(Complex64F input, Complex64F root)
    {
        double r = input.getMagnitude();
        double a = input.real;

        root.real = Math.sqrt((r+a)/2.0);
        root.imaginary = Math.sqrt((r-a)/2.0);
        if( input.imaginary < 0 )
            root.imaginary = -root.imaginary;
    }
}
