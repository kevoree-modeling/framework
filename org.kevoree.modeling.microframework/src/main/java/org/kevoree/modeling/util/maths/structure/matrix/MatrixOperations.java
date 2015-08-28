package org.kevoree.modeling.util.maths.structure.matrix;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class MatrixOperations {

    public static int BLOCK_WIDTH = 60;
    public static int TRANSPOSE_SWITCH = 375;
    public static int MULT_COLUMN_SWITCH = 15;

    public static KArray2D transpose(KArray2D matA, KBlas blas) {
        KArray2D result = new NativeArray2D(matA.columns(), matA.rows());
        if (matA.columns() == matA.rows()) {
            transposeSquare(matA, result, blas);
        } else if (matA.columns() > TRANSPOSE_SWITCH && matA.rows() > TRANSPOSE_SWITCH) {
            transposeBlock(matA, result, blas);
        } else {
            transposeStandard(matA, result, blas);
        }
        return result;
    }

    public static KArray2D multiply(KArray2D matA, KArray2D matB, KBlas blas) {
        NativeArray2D matC = new NativeArray2D(matA.rows(), matB.columns());
        if (matB.columns() == 1) {
            matrixVectorMult(matA, matB, matC, blas);
        } else if (matB.columns() >= MULT_COLUMN_SWITCH) {
            mult_reorder(matA, matB, matC, blas);
        } else {
            mult_small(matA, matB, matC, blas);
        }
        return matC;
    }

    private static void transposeSquare(KArray2D matA, KArray2D result, KBlas blas) {
        int index = 1;
        int indexEnd = matA.columns();
        for (int i = 0; i < matA.rows();
             i++, index += i + 1, indexEnd += matA.columns()) {
            int indexOther = (i + 1) * matA.columns() + i;
            int n = i * (matA.columns() + 1);
            result.setAtIndex(n, matA.getAtIndex(n));
            for (; index < indexEnd; index++, indexOther += matA.columns()) {
                result.setAtIndex(index, matA.getAtIndex(indexOther));
                result.setAtIndex(indexOther, matA.getAtIndex(index));
            }
        }
    }

    private static void transposeStandard(KArray2D matA, KArray2D result, KBlas blas) {
        int index = 0;
        for (int i = 0; i < result.rows(); i++) {
            int index2 = i;
            int end = index + result.columns();
            while (index < end) {
                result.setAtIndex(index++, matA.getAtIndex(index2));
                index2 += matA.columns();
            }
        }
    }

    private static void transposeBlock(KArray2D matA, KArray2D result, KBlas blas) {
        for (int i = 0; i < matA.rows(); i += BLOCK_WIDTH) {
            int blockHeight = Math.min(BLOCK_WIDTH, matA.rows() - i);
            int indexSrc = i * matA.columns();
            int indexDst = i;
            for (int j = 0; j < matA.columns(); j += BLOCK_WIDTH) {
                int blockWidth = Math.min(BLOCK_WIDTH, matA.columns() - j);
                int indexSrcEnd = indexSrc + blockWidth;
                for (; indexSrc < indexSrcEnd; indexSrc++) {
                    int rowSrc = indexSrc;
                    int rowDst = indexDst;
                    int end = rowDst + blockHeight;
                    for (; rowDst < end; rowSrc += matA.columns()) {
                        result.setAtIndex(rowDst++, matA.getAtIndex(rowSrc));
                    }
                    indexDst += result.columns();
                }
            }
        }
    }


    private static void mult_small(KArray2D matA, KArray2D matB, KArray2D matC, KBlas blas) {
        int aIndexStart = 0;
        int cIndex = 0;
        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matB.columns(); j++) {
                double total = 0;
                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + matB.rows();
                while (indexA < end) {
                    total += matA.getAtIndex(indexA++) * matB.getAtIndex(indexB);
                    indexB += matB.columns();
                }
                matC.setAtIndex(cIndex++, total);
            }
            aIndexStart += matA.columns();
        }
    }

    private static void mult_reorder(KArray2D matA, KArray2D matB, KArray2D matC, KBlas blas) {
        if (matA.columns() == 0 || matA.rows() == 0) {
            matC.setAll(0);
            return;
        }
        double valA;
        int indexCbase = 0;
        int endOfKLoop = matB.rows() * matB.columns();
        for (int i = 0; i < matA.rows(); i++) {
            int indexA = i * matA.columns();
            // need to assign matC.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + matB.columns();
            valA = matA.getAtIndex(indexA++);
            while (indexB < end) {
                matC.setAtIndex(indexC++, valA * matB.getAtIndex(indexB++));
            }
            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + matB.columns();

                valA = matA.getAtIndex(indexA++);

                while (indexB < end) { // j loop
                    matC.addAtIndex(indexC++, valA * matB.getAtIndex(indexB++));
                }
            }
            indexCbase += matC.columns();
        }
    }

    private static void matrixVectorMult(KArray2D matA, KArray2D matB, KArray2D matC, KBlas blas) {
        if (matA.columns() == 0) {
            matC.setAll(0);
            return;
        }
        int indexA = 0;
        int cIndex = 0;
        double b0 = matB.getAtIndex(0);
        for (int i = 0; i < matA.rows(); i++) {
            double total = matA.getAtIndex(indexA++) * b0;
            for (int j = 1; j < matA.columns(); j++) {
                total += matA.getAtIndex(indexA++) * matB.getAtIndex(j);
            }
            matC.setAtIndex(cIndex++, total);
        }
    }

    public static KArray2D identity(int width) {
        KArray2D ret = new NativeArray2D(width, width);
        ret.setAll(0);
        for (int i = 0; i < width; i++) {
            ret.set(i, i, 1);
        }
        return ret;
    }

}
