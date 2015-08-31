package org.kevoree.modeling.util.maths.structure.blas.impl;

import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;

public class JavaBlas implements KBlas {
    public static int BLOCK_WIDTH = 60;
    public static int TRANSPOSE_SWITCH = 375;

    @Override
    public void dscal(double alpha, KArray2D matA) {
        if (alpha == 0) {
            matA.setAll(0);
        }
        for (int i = 0; i < matA.rows() * matA.columns(); i++) {
            matA.setAtIndex(i, alpha * matA.getAtIndex(i));
        }
    }

    public static void dgemm2(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, KArray2D paramArrayOfDouble1, int paramInt4, int paramInt5, KArray2D paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, KArray2D paramArrayOfDouble3, int paramInt8, int paramInt9) {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        boolean bool1 = false;
        boolean bool2 = false;
        bool1 = paramString1.equals(KBlasTransposeType.NOTRANSPOSE);
        bool2 = paramString2.equals(KBlasTransposeType.NOTRANSPOSE);
        if (bool1) {
            i1 = paramInt1;
            n = paramInt3;
        } else {
            i1 = paramInt3;
            n = paramInt1;
        }
        if (bool2) {
            i2 = paramInt3;
        } else {
            i2 = paramInt2;
        }
        j = 0;
        if ((((bool1 ^ true)) && ((paramString1.equals(KBlasTransposeType.CONJUGATE) ^ true)) ? 1 : 0) != 0) {
        }
        if (((paramString1.equals(KBlasTransposeType.TRANSPOSE) ^ true) ? 1 : 0) != 0) {
            j = 1;
        } else {
            if ((((bool2 ^ true)) && ((paramString2.equals(KBlasTransposeType.CONJUGATE) ^ true)) ? 1 : 0) != 0) {
            }
            if (((paramString2.equals(KBlasTransposeType.TRANSPOSE) ^ true) ? 1 : 0) != 0) {
                j = 2;
            } else if ((paramInt1 >= 0 ? 0 : 1) != 0) {
                j = 3;
            } else if ((paramInt2 >= 0 ? 0 : 1) != 0) {
                j = 4;
            } else if ((paramInt3 >= 0 ? 0 : 1) != 0) {
                j = 5;
            } else if ((paramInt5 >= Math.max(1, i1) ? 0 : 1) != 0) {
                j = 8;
            } else if ((paramInt7 >= Math.max(1, i2) ? 0 : 1) != 0) {
                j = 10;
            } else if ((paramInt9 >= Math.max(1, paramInt1) ? 0 : 1) != 0) {
                j = 13;
            }
        }

        if ((paramInt1 != 0 ? 0 : 1) == 0) {
        }
        if (((paramInt2 != 0 ? 0 : 1) == 0 ? 0 : 1) == 0) {
            if ((paramDouble1 != 0.0D ? 0 : 1) == 0) {
            }
            if (((paramInt3 != 0 ? 0 : 1) == 0 ? 0 : 1) == 0) {
            }
        }
        if ((((paramDouble2 != 1.0D ? 0 : 1) != 0 ? 1 : 0) == 0 ? 0 : 1) != 0) {
            return;
        }
        int i3;
        int i4;
        if ((paramDouble1 != 0.0D ? 0 : 1) != 0) {
            if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                k = 1;
                for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                        paramArrayOfDouble3.setAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8) , 0.0D);
                        i += 1;
                    }
                    k += 1;
                }
            } else {
                k = 1;
                for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                        paramArrayOfDouble3.setAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8),(paramDouble2 * paramArrayOfDouble3.getAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8))));
                        i += 1;
                    }
                    k += 1;
                }
            }
            return;
        }
        int i5;
        if (bool2) {
            if (bool1) {
                k = 1;
                for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                    if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                        i = 1;
                        for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                            paramArrayOfDouble3.setAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8), 0.0D);
                            i += 1;
                        }
                    } else if ((paramDouble2 == 1.0D ? 0 : 1) != 0) {
                        i = 1;
                        i4 = paramInt1 - 1 + 1;
                        for (; ; ) {
                            paramArrayOfDouble3.setAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8), (paramDouble2 * paramArrayOfDouble3.getAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8))));
                            i += 1;
                            i4--;
                            if (i4 <= 0) {
                                break;
                            }
                        }
                    }
                    m = 1;
                    for (i4 = paramInt3 - 1 + 1; i4 > 0; i4--) {
                        if ((paramArrayOfDouble2.getAtIndex((m - 1 + (k - 1) * paramInt7 + paramInt6)) == 0.0D ? 0 : 1) != 0) {
                            d = paramDouble1 * paramArrayOfDouble2.getAtIndex((m - 1 + (k - 1) * paramInt7 + paramInt6));
                            i = 1;
                            for (i5 = paramInt1 - 1 + 1; i5 > 0; i5--) {
                                paramArrayOfDouble3.addAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8), d * paramArrayOfDouble1.getAtIndex(i - 1 + (m - 1) * paramInt5 + paramInt4));
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    k += 1;
                }
            } else {
                k = 1;
                for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                        d = 0.0D;
                        m = 1;
                        for (i5 = paramInt3 - 1 + 1; i5 > 0; i5--) {
                            d += paramArrayOfDouble1.getAtIndex((m - 1 + (i - 1) * paramInt5 + paramInt4)) * paramArrayOfDouble2.getAtIndex(m - 1 + (k - 1) * paramInt7 + paramInt6);
                            m += 1;
                        }
                        if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                            paramArrayOfDouble3.setAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8, (paramDouble1 * d));
                        } else {
                            paramArrayOfDouble3.setAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8 , (paramDouble1 * d + paramDouble2 * paramArrayOfDouble3.getAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8)));
                        }
                        i += 1;
                    }
                    k += 1;
                }
            }
        } else if (bool1) {
            k = 1;
            for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                    i = 1;
                    for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                        paramArrayOfDouble3.setAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8), 0.0D);
                        i += 1;
                    }
                } else if ((paramDouble2 == 1.0D ? 0 : 1) != 0) {
                    i = 1;
                    i4 = paramInt1 - 1 + 1;
                    for (; ; ) {
                        paramArrayOfDouble3.setAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8, (paramDouble2 * paramArrayOfDouble3.getAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8)));
                        i += 1;
                        i4--;
                        if (i4 <= 0) {
                            break;
                        }
                    }
                }
                m = 1;
                for (i4 = paramInt3 - 1 + 1; i4 > 0; i4--) {
                    if ((paramArrayOfDouble2.getAtIndex(k - 1 + (m - 1) * paramInt7 + paramInt6) == 0.0D ? 0 : 1) != 0) {
                        d = paramDouble1 * paramArrayOfDouble2.getAtIndex(k - 1 + (m - 1) * paramInt7 + paramInt6);
                        i = 1;
                        for (i5 = paramInt1 - 1 + 1; i5 > 0; i5--) {
                            paramArrayOfDouble3.addAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8, d * paramArrayOfDouble1.getAtIndex(i - 1 + (m - 1) * paramInt5 + paramInt4));
                            i += 1;
                        }
                    }
                    m += 1;
                }
                k += 1;
            }
        } else {
            k = 1;
            for (i3 = paramInt2 - 1 + 1; i3 > 0; i3--) {
                i = 1;
                for (i4 = paramInt1 - 1 + 1; i4 > 0; i4--) {
                    d = 0.0D;
                    m = 1;
                    for (i5 = paramInt3 - 1 + 1; i5 > 0; i5--) {
                        d += paramArrayOfDouble1.getAtIndex((m - 1 + (i - 1) * paramInt5 + paramInt4)) * paramArrayOfDouble2.getAtIndex((k - 1 + (m - 1) * paramInt7 + paramInt6));
                        m += 1;
                    }
                    if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                        paramArrayOfDouble3.setAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8, (paramDouble1 * d));
                    } else {
                        paramArrayOfDouble3.setAtIndex(i - 1 + (k - 1) * paramInt9 + paramInt8, (paramDouble1 * d + paramDouble2 * paramArrayOfDouble3.getAtIndex((i - 1 + (k - 1) * paramInt9 + paramInt8))));
                    }
                    i += 1;
                }
                k += 1;
            }
        }
    }



    @Override
    public void dgemm(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {
      //  mult_small(alpha, matA, matB, beta, matC); //todo to optimize later
        dgemm2(transa, transb, matA.rows(), matB.columns(), matA.columns(), alpha, matA, 0, matA.rows(), matB, 0, matB.rows(), beta, matC, 0, matC.rows());

    }





    @Override
    public void trans(KArray2D matA, KArray2D result) {
        if (matA.columns() == matA.rows()) {
            transposeSquare(matA, result);
        } else if (matA.columns() > TRANSPOSE_SWITCH && matA.rows() > TRANSPOSE_SWITCH) {
            transposeBlock(matA, result);
        } else {
            transposeStandard(matA, result);
        }
    }

    @Override
    public void shutdown() {

    }

    private void transposeSquare(KArray2D matA, KArray2D result) {
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

    private void transposeStandard(KArray2D matA, KArray2D result) {
        int index = 0;
        for (int i = 0; i < result.columns(); i++) {
            int index2 = i;
            int end = index + result.rows();
            while (index < end) {
                result.setAtIndex(index++, matA.getAtIndex(index2));
                index2 += matA.rows();
            }
        }
    }

    private void transposeBlock(KArray2D matA, KArray2D result) {
        for (int j = 0; j < matA.columns(); j += BLOCK_WIDTH) {
            int blockWidth = Math.min(BLOCK_WIDTH, matA.columns() - j);
            int indexSrc = j * matA.rows();
            int indexDst = j;

            for (int i = 0; i < matA.rows(); i += BLOCK_WIDTH) {
                int blockHeight = Math.min(BLOCK_WIDTH, matA.rows() - i);
                int indexSrcEnd = indexSrc + blockHeight;

                for (; indexSrc < indexSrcEnd; indexSrc++) {
                    int colSrc = indexSrc;
                    int colDst = indexDst;
                    int end = colDst + blockWidth;
                    for (; colDst < end; colDst ++) {
                        result.setAtIndex(colDst, matA.getAtIndex(colSrc));
                        colSrc+=matA.rows();
                    }
                    indexDst += result.rows();
                }

            }

        }
    }


    private void mult_small(double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {

        int cIndex = 0;
        double[] datA = matA.data();
        double[] datB = matB.data();
        double[] datC = matC.data();


        for (int j = 0; j < matB.columns(); j++) {

            for (int i = 0; i < matA.rows(); i++) {
                double total = 0;
                int indexA = i;
                int indexB = j * matB.rows();
                int end = indexA + (matB.rows() - 1) * matA.rows();
                while (indexA <= end) {
                    total += datA[indexA] * datB[indexB];
                    indexA += matA.rows();
                    indexB++;
                }
                datC[cIndex] = alpha * total + beta * matC.getAtIndex(cIndex);
                cIndex++;
            }
        }
    }


}
