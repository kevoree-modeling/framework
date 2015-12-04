package org.kevoree.modeling.util.maths.matrix.solvers.decomposition;

import org.kevoree.modeling.util.maths.matrix.CommonOps;
import org.kevoree.modeling.util.maths.matrix.Complex64F;
import org.kevoree.modeling.util.maths.matrix.DenseMatrix64F;

public class SymmetricQRAlgorithmDecomposition_D64{

    // computes a tridiagonal matrix whose eigenvalues are the same as the original
    // matrix and can be easily computed.
    private TridiagonalDecompositionHouseholder_D64 decomp;
    // helper class for eigenvalue and eigenvector algorithms
    private SymmetricQREigenHelper helper;
    // computes the eigenvectors
    private SymmetricQrAlgorithm vector;

    // should it compute eigenvectors at the same time as the eigenvalues?
    private boolean computeVectorsWithValues = false;

    // where the found eigenvalues are stored
    private double values[];

    // where the tridiagonal matrix is stored
    private double diag[];
    private double off[];

    private double diagSaved[];
    private double offSaved[];

    // temporary variable used to store/compute eigenvectors
    private DenseMatrix64F V;
    // the extracted eigenvectors
    private DenseMatrix64F eigenvectors[];

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public SymmetricQRAlgorithmDecomposition_D64(TridiagonalDecompositionHouseholder_D64 decomp,
                                                 boolean computeVectors) {

        this.decomp = decomp;
        this.computeVectors = computeVectors;

        helper = new SymmetricQREigenHelper();

        vector = new SymmetricQrAlgorithm(helper);
    }

 /*   public SymmetricQRAlgorithmDecomposition_D64(boolean computeVectors) {

        this(new TridiagonalDecompositionHouseholder_D64(),computeVectors);
    }*/

    public void setComputeVectorsWithValues(boolean computeVectorsWithValues) {
        if( !computeVectors )
            throw new RuntimeException("Compute eigenvalues has been set to false");

        this.computeVectorsWithValues = computeVectorsWithValues;
    }

    public void setMaxIterations( int max ) {
        vector.setMaxIterations(max);
    }


    public int getNumberOfEigenvalues() {
        return helper.getMatrixSize();
    }


    public Complex64F getEigenvalue(int index) {
        Complex64F c= new Complex64F();
        c.setValues(values[index],0);
        return c;

    }


    public DenseMatrix64F getEigenVector(int index) {
        return eigenvectors[index];
    }


    public boolean decompose(DenseMatrix64F orig) {
        if( orig.numCols != orig.numRows )
            throw new RuntimeException("Matrix must be square.");
        if( orig.numCols <= 0 )
            return false;

        int N = orig.numRows;

        // compute a similar tridiagonal matrix
        if( !decomp.decompose(orig) )
            return false;

        if( diag == null || diag.length < N) {
            diag = new double[N];
            off = new double[N-1];
        }
        decomp.getDiagonal(diag,off);

        // Tell the helper to work with this matrix
        helper.init(diag,off,N);

        if( computeVectors ) {
            if( computeVectorsWithValues ) {
                return extractTogether();
            }  else {
                return extractSeparate(N);
            }
        } else {
            return computeEigenValues();
        }
    }


    public boolean inputModified() {
        return decomp.inputModified();
    }

    private boolean extractTogether() {
        // extract the orthogonal from the similar transform
        V = decomp.getQ(V,true);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        helper.setQ(V);

        vector.setFastEigenvalues(false);

        // extract the eigenvalues
        if( !vector.process3arg(-1,null,null) )
            return false;

        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps.rowsToVector(V, eigenvectors);

        // save a copy of them since this data structure will be recycled next
        values = helper.copyEigenvalues(values);

        return true;
    }

    private boolean extractSeparate(int numCols) {
        if (!computeEigenValues())
            return false;

        // ---- set up the helper to decompose the same tridiagonal matrix
        // swap arrays instead of copying them to make it slightly faster
        helper.reset(numCols);
        diagSaved = helper.swapDiag(diagSaved);
        offSaved = helper.swapOff(offSaved);

        // extract the orthogonal from the similar transform
        V = decomp.getQ(V,true);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        vector.setQ(V);

        // extract eigenvectors
        if( !vector.process(-1,null,null, values) )
            return false;

        // the ordering of the eigenvalues might have changed
        values = helper.copyEigenvalues(values);
        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps.rowsToVector(V,eigenvectors);

        return true;
    }


    private boolean computeEigenValues() {
        // make a copy of the internal tridiagonal matrix data for later use
        diagSaved = helper.copyDiag(diagSaved);
        offSaved = helper.copyOff(offSaved);

        vector.setQ(null);
        vector.setFastEigenvalues(true);

        // extract the eigenvalues
        if( !vector.process3arg(-1,null,null) )
            return false;

        // save a copy of them since this data structure will be recycled next
        values = helper.copyEigenvalues(values);
        return true;
    }
}