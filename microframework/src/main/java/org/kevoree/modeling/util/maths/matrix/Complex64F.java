package org.kevoree.modeling.util.maths.matrix;



public class Complex64F{


    public double real;
    public double imaginary;


    public Complex64F() {
    }

    public double getReal() {
        return real;
    }

    public double getMagnitude() {
        return Math.sqrt(real*real + imaginary*imaginary);
    }

    public double getMagnitude2() {
        return real*real + imaginary*imaginary;
    }

    public void setReal(double real) {
        this.real = real;
    }

    /*
    public double getImaginary() {
        return imaginary;
    }

    public void setImaginary(double imaginary) {
        this.imaginary = imaginary;
    }
    */

    public void setValues(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /*
    public void setComplex(Complex64F a) {
        this.real = a.real;
        this.imaginary = a.imaginary;
    }
*/
    public boolean isReal() {
        return imaginary == 0.0;
    }

    public String toString() {
        if( imaginary == 0 ) {
            return ""+real;
        } else {
            return real+" "+imaginary+"i";
        }
    }

    public Complex64F times( Complex64F a ) {
        Complex64F ret = new Complex64F();
        ComplexMath64F.multiply(this,a,ret);
        return ret;
    }

    /*
    public Complex64F plus( Complex64F a ) {
        Complex64F ret = new Complex64F();
        ComplexMath64F.plus(this, a, ret);
        return ret;
    }

    public Complex64F minus( Complex64F a ) {
        Complex64F ret = new Complex64F();
        ComplexMath64F.minus(this, a, ret);
        return ret;
    }

    public Complex64F divide( Complex64F a ) {
        Complex64F ret = new Complex64F();
        ComplexMath64F.divide(this,a,ret);
        return ret;
    }
    */
}
