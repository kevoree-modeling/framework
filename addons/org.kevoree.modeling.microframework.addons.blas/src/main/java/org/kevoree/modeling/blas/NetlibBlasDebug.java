package org.kevoree.modeling.blas;

import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.*;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.netlib.blas.Lsame;
import org.netlib.util.intW;

public class NetlibBlasDebug implements KBlas {
    private BLAS blas;
    private LAPACK lapack;


    public NetlibBlasDebug() {
        blas = BLAS.getInstance();
        lapack=LAPACK.getInstance();
    }

    @Override
    public void dgemm(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, double[] paramArrayOfDouble3, int paramInt8, int paramInt9) {

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
      /*  if (bool1) {
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
        if ((((!bool1)) && ((!paramString1.equals(KBlasTransposeType.CONJUGATE))) ? 1 : 0) != 0) {
        }
        if (((!paramString1.equals(KBlasTransposeType.TRANSPOSE)) ? 1 : 0) != 0) {
            j = 1;
        } else {
            if ((((!bool2)) && ((!paramString2.equals(KBlasTransposeType.CONJUGATE))) ? 1 : 0) != 0) {
            }
            if (((!paramString2.equals(KBlasTransposeType.TRANSPOSE)) ? 1 : 0) != 0) {
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
        }*/
        /*if ((j == 0 ? 0 : 1) != 0) {
            // Xerbla.xerbla("DGEMM ", j);
            return;
        }*/
       /* if ((paramInt1 != 0 ? 0 : 1) == 0) {}
        if (((paramInt2 != 0 ? 0 : 1) == 0 ? 0 : 1) == 0)
        {
            if ((paramDouble1 != 0.0D ? 0 : 1) == 0) {}
            if (((paramInt3 != 0 ? 0 : 1) == 0 ? 0 : 1) == 0) {}
        }

        if ((((paramDouble2 != 1.0D ? 0 : 1) != 0 ? 1 : 0) == 0 ? 0 : 1) != 0) {
            return;
        }*/
        int i3;
        int i4;
        if ((paramDouble1 != 0.0D ? 0 : 1) != 0) {
            if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                k = 1;
                for (i3 = paramInt2; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1; i4 > 0; i4--) {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = 0.0D;
                        i += 1;
                    }
                    k += 1;
                }
            } else {
                k = 1;
                for (i3 = paramInt2; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1; i4 > 0; i4--) {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble2 * paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)]);
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
                for (i3 = paramInt2; i3 > 0; i3--) {
                    if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                        i = 1;
                        for (i4 = paramInt1; i4 > 0; i4--) {
                            paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = 0.0D;
                            i += 1;
                        }
                    } else {
                        i = 1;
                        i4 = paramInt1;
                        for (; ; ) {
                            paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble2 * paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)]);
                            i += 1;
                            i4--;
                            if (i4 <= 0) {
                                break;
                            }
                        }
                    }
                    m = 1;
                    for (i4 = paramInt3; i4 > 0; i4--) {
                        if ((paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt7 + paramInt6)] == 0.0D ? 0 : 1) != 0) {
                            d = paramDouble1 * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt7 + paramInt6)];
                            i = 1;
                            for (i5 = paramInt1; i5 > 0; i5--) {
                                paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt5 + paramInt4)];
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    k += 1;
                }
            } else {
                k = 1;
                for (i3 = paramInt2; i3 > 0; i3--) {
                    i = 1;
                    for (i4 = paramInt1; i4 > 0; i4--) {
                        d = 0.0D;
                        m = 1;
                        for (i5 = paramInt3; i5 > 0; i5--) {
                            d += paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt5 + paramInt4)] * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt7 + paramInt6)];
                            m += 1;
                        }
                        if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                            paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble1 * d);
                        } else {
                            paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble1 * d + paramDouble2 * paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)]);
                        }
                        i += 1;
                    }
                    k += 1;
                }
            }
        } else if (bool1) {
            k = 1;
            for (i3 = paramInt2; i3 > 0; i3--) {
                if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                    i = 1;
                    for (i4 = paramInt1; i4 > 0; i4--) {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = 0.0D;
                        i += 1;
                    }
                } else if ((paramDouble2 == 1.0D ? 0 : 1) != 0) {
                    i = 1;
                    i4 = paramInt1;
                    for (; ; ) {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble2 * paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)]);
                        i += 1;
                        i4--;
                        if (i4 <= 0) {
                            break;
                        }
                    }
                }
                m = 1;
                for (i4 = paramInt3; i4 > 0; i4--) {
                    if ((paramArrayOfDouble2[(k - 1 + (m - 1) * paramInt7 + paramInt6)] == 0.0D ? 0 : 1) != 0) {
                        d = paramDouble1 * paramArrayOfDouble2[(k - 1 + (m - 1) * paramInt7 + paramInt6)];
                        i = 1;
                        for (i5 = paramInt1; i5 > 0; i5--) {
                            paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt5 + paramInt4)];
                            i += 1;
                        }
                    }
                    m += 1;
                }
                k += 1;
            }
        } else {
            k = 1;
            for (i3 = paramInt2; i3 > 0; i3--) {
                i = 1;
                for (i4 = paramInt1; i4 > 0; i4--) {
                    d = 0.0D;
                    m = 1;
                    for (i5 = paramInt3; i5 > 0; i5--) {
                        d += paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt5 + paramInt4)] * paramArrayOfDouble2[(k - 1 + (m - 1) * paramInt7 + paramInt6)];
                        m += 1;
                    }
                    if ((paramDouble2 != 0.0D ? 0 : 1) != 0) {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble1 * d);
                    } else {
                        paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)] = (paramDouble1 * d + paramDouble2 * paramArrayOfDouble3[(i - 1 + (k - 1) * paramInt9 + paramInt8)]);
                    }
                    i += 1;
                }
                k += 1;
            }
        }
    }



 /*
    public void dgemm(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, double[] paramArrayOfDouble3, int paramInt8, int paramInt9) {
        blas.dgemm(transTypeToChar(paramString1),transTypeToChar(paramString2), paramInt1,  paramInt2,  paramInt3,  paramDouble1, paramArrayOfDouble1, paramInt4, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, paramDouble2, paramArrayOfDouble3, paramInt8, paramInt9);
        }*/

    @Override
    public void dgetrs(KBlasTransposeType paramString, int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetrs(transTypeToChar(paramString), paramInt1, paramInt2, paramArrayOfDouble1, paramInt3, paramInt4, paramArrayOfInt, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, newint);
        paramintW[0] = newint.val;
    }

  /*  @Override
    public void dgetri(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, int[] paramArrayOfInt, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetri(paramInt1, paramArrayOfDouble1,paramInt2, paramInt3, paramArrayOfInt, paramInt4, paramArrayOfDouble2, paramInt5, paramInt6, newint);
        paramintW[0] = newint.val;

    }*/


    public static void dtrmm(KBlasSideType paramString1, KBlasOrientationType paramString2, KBlasTransposeType paramString3, KBlasUnitType paramString4, int paramInt1, int paramInt2, double paramDouble, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6)
    {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        boolean bool1 = false;
        boolean bool2 = false;
        boolean bool3 = false;
        bool1 = paramString1.equals(KBlasSideType.LEFT);
        if (bool1) {
            n = paramInt1;
        } else {
            n = paramInt2;
        }
        bool2 = paramString4.equals(KBlasUnitType.NONUNIT);
        bool3 = paramString2.equals(KBlasOrientationType.UPPER);
       /* j = 0;
        if ((((!bool1)) && ((!paramString1.equals(KBlasSideType.RIGHT))) ? 1 : 0) != 0)
        {
            j = 1;
        }
        else if ((((!bool3)) && ((!paramString2.equals(KBlasOrientationType.LOWER))) ? 1 : 0) != 0)
        {
            j = 2;
        }
        else
        {
           // if ((((!paramString3.equals(KBlasTransposeType.NOTRANSPOSE))) && ((!paramString3.equals(KBlasTransposeType.TRANSPOSE))) ? 1 : 0) != 0) {}
            if (((!paramString3.equals(KBlasTransposeType.CONJUGATE)) ? 1 : 0) != 0) {
                j = 3;
            } else if ((((!paramString4.equals(KBlasUnitType.UNIT))) && ((!paramString4.equals(KBlasUnitType.NONUNIT))) ? 1 : 0) != 0) {
                j = 4;
            } else if ((paramInt1 >= 0 ? 0 : 1) != 0) {
                j = 5;
            } else if ((paramInt2 >= 0 ? 0 : 1) != 0) {
                j = 6;
            } else if ((paramInt4 >= Math.max(1, n) ? 0 : 1) != 0) {
                j = 9;
            } else if ((paramInt6 >= Math.max(1, paramInt1) ? 0 : 1) != 0) {
                j = 11;
            }
        }
        if ((j == 0 ? 0 : 1) != 0)
        {
            //Xerbla.xerbla("DTRMM ", j);
            return;
        }
        if ((paramInt2 != 0 ? 0 : 1) != 0) {
            return;
        }*/
        int i1;
        int i2;
        if ((paramDouble != 0.0D ? 0 : 1) != 0)
        {
            k = 1;
            for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
            {
                i = 1;
                for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                {
                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = 0.0D;
                    i += 1;
                }
                k += 1;
            }
            return;
        }
        int i3;
        if (bool1)
        {
            if (paramString3.equals(KBlasTransposeType.NOTRANSPOSE))
            {
                if (bool3)
                {
                    k = 1;
                    for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
                    {
                        m = 1;
                        for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                        {
                            if ((paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                            {
                                d = paramDouble * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                                i = 1;
                                for (i3 = m - 1; i3 > 0; i3--)
                                {
                                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt4 + paramInt3)];
                                    i += 1;
                                }
                                if (bool2) {
                                    d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                                }
                                paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] = d;
                            }
                            m += 1;
                        }
                        k += 1;
                    }
                }
                else
                {
                    k = 1;
                    for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
                    {
                        m = paramInt1;
                        for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                        {
                            if ((paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                            {
                                d = paramDouble * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                                paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] = d;
                                if (bool2) {
                                    paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                                }
                                i = m + 1;
                                for (i3 = paramInt1 - (m + 1) + 1; i3 > 0; i3--)
                                {
                                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt4 + paramInt3)];
                                    i += 1;
                                }
                            }
                            m += -1;
                        }
                        k += 1;
                    }
                }
            }
            else if (bool3)
            {
                k = 1;
                for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
                {
                    i = paramInt1;
                    for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                    {
                        d = paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)];
                        if (bool2) {
                            d *= paramArrayOfDouble1[(i - 1 + (i - 1) * paramInt4 + paramInt3)];
                        }
                        m = 1;
                        for (i3 = i - 1; i3 > 0; i3--)
                        {
                            d += paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                            m += 1;
                        }
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * d);
                        i += -1;
                    }
                    k += 1;
                }
            }
            else
            {
                k = 1;
                for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
                {
                    i = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        d = paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)];
                        if (bool2) {
                            d *= paramArrayOfDouble1[(i - 1 + (i - 1) * paramInt4 + paramInt3)];
                        }
                        m = i + 1;
                        for (i3 = paramInt1 - (i + 1) + 1; i3 > 0; i3--)
                        {
                            d += paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                            m += 1;
                        }
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * d);
                        i += 1;
                    }
                    k += 1;
                }
            }
        }
        else if (paramString3.equals(KBlasTransposeType.NOTRANSPOSE))
        {
            if (bool3)
            {
                k = paramInt2;
                for (i1 = (1 - paramInt2 + -1) / -1; i1 > 0; i1--)
                {
                    d = paramDouble;
                    if (bool2) {
                        d *= paramArrayOfDouble1[(k - 1 + (k - 1) * paramInt4 + paramInt3)];
                    }
                    i = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                    m = 1;
                    for (i2 = k - 1; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                        {
                            d = paramDouble * paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)];
                            i = 1;
                            for (i3 = paramInt1 - 1 + 1; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    k += -1;
                }
            }
            else
            {
                k = 1;
                for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
                {
                    d = paramDouble;
                    if (bool2) {
                        d *= paramArrayOfDouble1[(k - 1 + (k - 1) * paramInt4 + paramInt3)];
                    }
                    i = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                    m = k + 1;
                    for (i2 = paramInt2 - (k + 1) + 1; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                        {
                            d = paramDouble * paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)];
                            i = 1;
                            for (i3 = paramInt1 - 1 + 1; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    k += 1;
                }
            }
        }
        else if (bool3)
        {
            m = 1;
            for (i1 = paramInt2 - 1 + 1; i1 > 0; i1--)
            {
                k = 1;
                for (i2 = m - 1; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramDouble * paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i3 = paramInt1 - 1 + 1; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                            i += 1;
                        }
                    }
                    k += 1;
                }
                d = paramDouble;
                if (bool2) {
                    d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                }
                if ((d == 1.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                m += 1;
            }
        }
        else
        {
            m = paramInt2;
            for (i1 = (1 - paramInt2 + -1) / -1; i1 > 0; i1--)
            {
                k = m + 1;
                for (i2 = paramInt2 - (m + 1) + 1; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramDouble * paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i3 = paramInt1 - 1 + 1; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] += d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                            i += 1;
                        }
                    }
                    k += 1;
                }
                d = paramDouble;
                if (bool2) {
                    d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                }
                if ((d == 1.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                m += -1;
            }
        }
    }

    public static void dgemv(KBlasTransposeType paramString, int paramInt1, int paramInt2, double paramDouble1, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, double paramDouble2, double[] paramArrayOfDouble3, int paramInt7, int paramInt8)
    {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;

        if (paramString.equals(KBlasTransposeType.NOTRANSPOSE))
        {
            i5 = paramInt2;
            i6 = paramInt1;
        }
        else
        {
            i5 = paramInt1;
            i6 = paramInt2;
        }
        if ((paramInt6 <= 0 ? 0 : 1) != 0) {
            i3 = 1;
        } else {
            i3 = 1 - (i5 - 1) * paramInt6;
        }
        if ((paramInt8 <= 0 ? 0 : 1) != 0) {
            i4 = 1;
        } else {
            i4 = 1 - (i6 - 1) * paramInt8;
        }
        int i7;
        if ((paramDouble2 == 1.0D ? 0 : 1) != 0) {
            if ((paramInt8 != 1 ? 0 : 1) != 0)
            {
                if ((paramDouble2 != 0.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i7 = i6 - 1 + 1; i7 > 0; i7--)
                    {
                        paramArrayOfDouble3[(i - 1 + paramInt7)] = 0.0D;
                        i += 1;
                    }
                }
                else
                {
                    i = 1;
                    for (i7 = i6 - 1 + 1; i7 > 0; i7--)
                    {
                        paramArrayOfDouble3[(i - 1 + paramInt7)] = (paramDouble2 * paramArrayOfDouble3[(i - 1 + paramInt7)]);
                        i += 1;
                    }
                }
            }
            else
            {
                m = i4;
                if ((paramDouble2 != 0.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i7 = i6 - 1 + 1; i7 > 0; i7--)
                    {
                        paramArrayOfDouble3[(m - 1 + paramInt7)] = 0.0D;
                        m += paramInt8;
                        i += 1;
                    }
                }
                else
                {
                    i = 1;
                    for (i7 = i6 - 1 + 1; i7 > 0; i7--)
                    {
                        paramArrayOfDouble3[(m - 1 + paramInt7)] = (paramDouble2 * paramArrayOfDouble3[(m - 1 + paramInt7)]);
                        m += paramInt8;
                        i += 1;
                    }
                }
            }
        }
        if ((paramDouble1 != 0.0D ? 0 : 1) != 0) {
            return;
        }
        int i8;
        if (paramString.equals(KBlasTransposeType.NOTRANSPOSE))
        {
            i1 = i3;
            if ((paramInt8 != 1 ? 0 : 1) != 0)
            {
                n = 1;
                for (i7 = paramInt2 - 1 + 1; i7 > 0; i7--)
                {
                    if ((paramArrayOfDouble2[(i1 - 1 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramDouble1 * paramArrayOfDouble2[(i1 - 1 + paramInt5)];
                        i = 1;
                        for (i8 = paramInt1 - 1 + 1; i8 > 0; i8--)
                        {
                            paramArrayOfDouble3[(i - 1 + paramInt7)] += d * paramArrayOfDouble1[(i - 1 + (n - 1) * paramInt4 + paramInt3)];
                            i += 1;
                        }
                    }
                    i1 += paramInt6;
                    n += 1;
                }
            }
            else
            {
                n = 1;
                for (i7 = paramInt2 - 1 + 1; i7 > 0; i7--)
                {
                    if ((paramArrayOfDouble2[(i1 - 1 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramDouble1 * paramArrayOfDouble2[(i1 - 1 + paramInt5)];
                        m = i4;
                        i = 1;
                        for (i8 = paramInt1 - 1 + 1; i8 > 0; i8--)
                        {
                            paramArrayOfDouble3[(m - 1 + paramInt7)] += d * paramArrayOfDouble1[(i - 1 + (n - 1) * paramInt4 + paramInt3)];
                            m += paramInt8;
                            i += 1;
                        }
                    }
                    i1 += paramInt6;
                    n += 1;
                }
            }
        }
        else
        {
            i2 = i4;
            if ((paramInt6 != 1 ? 0 : 1) != 0)
            {
                n = 1;
                for (i7 = paramInt2 - 1 + 1; i7 > 0; i7--)
                {
                    d = 0.0D;
                    i = 1;
                    for (i8 = paramInt1 - 1 + 1; i8 > 0; i8--)
                    {
                        d += paramArrayOfDouble1[(i - 1 + (n - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(i - 1 + paramInt5)];
                        i += 1;
                    }
                    paramArrayOfDouble3[(i2 - 1 + paramInt7)] += paramDouble1 * d;
                    i2 += paramInt8;
                    n += 1;
                }
            }
            else
            {
                n = 1;
                for (i7 = paramInt2 - 1 + 1; i7 > 0; i7--)
                {
                    d = 0.0D;
                    k = i3;
                    i = 1;
                    for (i8 = paramInt1 - 1 + 1; i8 > 0; i8--)
                    {
                        d += paramArrayOfDouble1[(i - 1 + (n - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(k - 1 + paramInt5)];
                        k += paramInt6;
                        i += 1;
                    }
                    paramArrayOfDouble3[(i2 - 1 + paramInt7)] += paramDouble1 * d;
                    i2 += paramInt8;
                    n += 1;
                }
            }
        }
    }

    public void dgetri(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, int[] paramArrayOfInt, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, int[] paramintW)
    {
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        paramintW[0] = 0;
        i5 = lapack.ilaenv(1, "DGETRI", " ", paramInt1, -1, -1, -1);
        i4 = paramInt1 * i5;
        paramArrayOfDouble2[paramInt5] = i4;
        i = paramInt6 != -1 ? 0 : 1;
        if ((paramInt1 >= 0 ? 0 : 1) != 0)
        {
            paramintW[0] = -1;
        }
        else if ((paramInt3 >= Math.max(1, paramInt1) ? 0 : 1) != 0)
        {
            paramintW[0] = -3;
        }

        if ((paramintW[0] == 0 ? 0 : 1) != 0)
        {
            //Xerbla.xerbla("DGETRI", -paramintW[0]);
            return;
        }
        if (i != 0) {
            return;
        }
        if ((paramInt1 != 0 ? 0 : 1) != 0) {
            return;
        }

        intW temp=new intW(paramintW[0]);
        dtrtri(KBlasOrientationType.UPPER, KBlasUnitType.NONUNIT, paramInt1, paramArrayOfDouble1, paramInt2, paramInt3, paramintW);
        //lapack.dtrtri("Upper", "Non-unit", paramInt1, paramArrayOfDouble1, paramInt2, paramInt3, temp);
        paramintW[0]=temp.val;

        if ((paramintW[0] <= 0 ? 0 : 1) != 0) {
            return;
        }
        i6 = 2;
        i3 = paramInt1;
        if (((i5 >= paramInt1 ? 0 : 1) != 0 ? 1 : 0) != 0)
        {
            k = Math.max(i3 * i5, 1);
            if ((paramInt6 >= k ? 0 : 1) != 0)
            {
                i5 = paramInt6 / i3;
                int f=lapack.ilaenv(2, "DGETRI", " ", paramInt1, -1, -1, -1);
                i6 = Math.max(2, f);
            }
        }
        else
        {
            k = paramInt1;
        }
        int i9;
        if (((i5 < paramInt1 ? 0 : 1) == 0 ? 0 : 1) != 0)
        {
            m = paramInt1;
            for (int i8 = (1 - paramInt1 + -1) / -1; i8 > 0; i8--)
            {
                j = m + 1;
                for (i9 = paramInt1 - (m + 1) + 1; i9 > 0; i9--)
                {
                    paramArrayOfDouble2[(j - 1 + paramInt5)] = paramArrayOfDouble1[(j - 1 + (m - 1) * paramInt3 + paramInt2)];
                    paramArrayOfDouble1[(j - 1 + (m - 1) * paramInt3 + paramInt2)] = 0.0D;
                    j += 1;
                }
                if ((m >= paramInt1 ? 0 : 1) != 0) {
                    //blas.dgemv("No transpose", paramInt1, paramInt1 - m, -1.0D, paramArrayOfDouble1, (m) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble2, m + paramInt5, 1, 1.0D, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, 1);
                    dgemv(KBlasTransposeType.NOTRANSPOSE, paramInt1, paramInt1 - m, -1.0D, paramArrayOfDouble1, (m) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble2, m + paramInt5, 1, 1.0D, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, 1);

                }
                m += -1;
            }
        }
        else
        {
            i7 = (paramInt1 - 1) / i5 * i5 + 1;
            m = i7;
            for (int i8 = (1 - i7 + -i5) / -i5; i8 > 0; i8--)
            {
                n = Math.min(i5, paramInt1 - m + 1);
                i1 = m;
                for (i9 = m + n - 1 - m + 1; i9 > 0; i9--)
                {
                    j = i1 + 1;
                    for (int i10 = paramInt1 - (i1 + 1) + 1; i10 > 0; i10--)
                    {
                        paramArrayOfDouble2[(j + (i1 - m) * i3 - 1 + paramInt5)] = paramArrayOfDouble1[(j - 1 + (i1 - 1) * paramInt3 + paramInt2)];
                        paramArrayOfDouble1[(j - 1 + (i1 - 1) * paramInt3 + paramInt2)] = 0.0D;
                        j += 1;
                    }
                    i1 += 1;
                }
                if ((m + n > paramInt1 ? 0 : 1) != 0) {
                    dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, paramInt1, n, paramInt1 - m - n + 1, -1.0D, paramArrayOfDouble1, (m + n - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble2, m + n - 1 + paramInt5, i3, 1.0D, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, paramInt3);
                }
                dtrsm(KBlasSideType.RIGHT, KBlasOrientationType.LOWER, KBlasTransposeType.NOTRANSPOSE, KBlasUnitType.UNIT, paramInt1, n, 1.0D, paramArrayOfDouble2, m - 1 + paramInt5, i3, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, paramInt3);
                //blas.dtrsm("Right", "Lower", "No transpose", "Unit", paramInt1, n, 1.0D, paramArrayOfDouble2, m - 1 + paramInt5, i3, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, paramInt3);
                m += -i5;
            }
        }
        m = paramInt1 - 1;
        for (int i8 = (1 - (paramInt1 - 1) + -1) / -1; i8 > 0; i8--)
        {
            i2 = paramArrayOfInt[(m - 1 + paramInt4)];
            if ((i2 == m ? 0 : 1) != 0) {
                dswap(paramInt1, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, 1, paramArrayOfDouble1, (i2 - 1) * paramInt3 + paramInt2, 1);
            }
            m += -1;
        }
        paramArrayOfDouble2[(paramInt5)] = k;
    }

    public static void dswap(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, double[] paramArrayOfDouble2, int paramInt4, int paramInt5)
    {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        if ((paramInt1 > 0 ? 0 : 1) != 0) {
            return;
        }
        int i1;
        if (((paramInt5 != 1 ? 0 : 1) != 0 ? 1 : 0) != 0)
        {
            m = paramInt1 % 3;
            if ((m == 0 ? 0 : 1) != 0)
            {
                i = 1;
                for (i1 = m ; i1 > 0; i1--)
                {
                    d = paramArrayOfDouble1[(i - 1 + paramInt2)];
                    paramArrayOfDouble1[(i - 1 + paramInt2)] = paramArrayOfDouble2[(i - 1 + paramInt4)];
                    paramArrayOfDouble2[(i - 1 + paramInt4)] = d;
                    i += 1;
                }
                if ((paramInt1 >= 3 ? 0 : 1) != 0) {
                    return;
                }
            }
            n = m + 1;
            i = n;
            for (i1 = (paramInt1 - n + 3) / 3; i1 > 0; i1--)
            {
                d = paramArrayOfDouble1[(i - 1 + paramInt2)];
                paramArrayOfDouble1[(i - 1 + paramInt2)] = paramArrayOfDouble2[(i - 1 + paramInt4)];
                paramArrayOfDouble2[(i - 1 + paramInt4)] = d;
                d = paramArrayOfDouble1[(i + paramInt2)];
                paramArrayOfDouble1[(i + paramInt2)] = paramArrayOfDouble2[(i + paramInt4)];
                paramArrayOfDouble2[(i + paramInt4)] = d;
                d = paramArrayOfDouble1[(i + 1 + paramInt2)];
                paramArrayOfDouble1[(i + 1 + paramInt2)] = paramArrayOfDouble2[(i + 1 + paramInt4)];
                paramArrayOfDouble2[(i + 1 + paramInt4)] = d;
                i += 3;
            }
        }
        else
        {
            j = 1;
            k = 1;
            if ((paramInt3 >= 0 ? 0 : 1) != 0) {
                j = (-paramInt1 + 1) * paramInt3 + 1;
            }
            if ((paramInt5 >= 0 ? 0 : 1) != 0) {
                k = (-paramInt1 + 1) * paramInt5 + 1;
            }
            i = 1;
            for (i1 = paramInt1 ; i1 > 0; i1--)
            {
                d = paramArrayOfDouble1[(j - 1 + paramInt2)];
                paramArrayOfDouble1[(j - 1 + paramInt2)] = paramArrayOfDouble2[(k - 1 + paramInt4)];
                paramArrayOfDouble2[(k - 1 + paramInt4)] = d;
                j += paramInt3;
                k += paramInt5;
                i += 1;
            }
        }
    }

    public void dtrtri(KBlasOrientationType paramString1, KBlasUnitType paramString2, int paramInt1, double[] paramArrayOfDouble, int paramInt2, int paramInt3, int[] paramintW)
    {
        String paramString1s="Upper";
        String paramString2s="Non-unit" ;
        intW paramintWw = new intW(paramintW[0]);

        boolean bool1 = false;
        boolean bool2 = false;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        paramintW[0] = 0;
        bool2 = paramString1.equals(KBlasOrientationType.UPPER);
        bool1 = paramString2.equals(KBlasUnitType.NONUNIT);
        if ((((!bool2)) && ((!paramString1.equals(KBlasOrientationType.LOWER))) ? 1 : 0) != 0) {
            paramintW[0] = -1;
        } else if ((((!bool1)) && ((!paramString2.equals(KBlasUnitType.UNIT))) ? 1 : 0) != 0) {
            paramintW[0] = -2;
        } else if ((paramInt1 >= 0 ? 0 : 1) != 0) {
            paramintW[0] = -3;
        } else if ((paramInt3 >= Math.max(1, paramInt1) ? 0 : 1) != 0) {
            paramintW[0] = -5;
        }
        if (( paramintW[0] == 0 ? 0 : 1) != 0)
        {
            //Xerbla.xerbla("DTRTRI", - paramintW[0]);
            return;
        }
        if ((paramInt1 != 0 ? 0 : 1) != 0) {
            return;
        }
        int n;
        if (bool1)
        {
            paramintW[0] = 1;
            for (n = paramInt1 - 1 + 1; n > 0; n--)
            {
                if ((paramArrayOfDouble[( paramintW[0] - 1 + ( paramintW[0] - 1) * paramInt3 + paramInt2)] != 0.0D ? 0 : 1) != 0) {
                    return;
                }
                paramintW[0] += 1;
            }
            paramintW[0] = 0;
        }
        k = lapack.ilaenv(1, "DTRTRI", paramString1s + paramString2s, paramInt1, -1, -1, -1);
        if (((k < paramInt1 ? 0 : 1) == 0 ? 0 : 1) != 0)
        {
            dtrti2(paramString1, paramString2, paramInt1, paramArrayOfDouble, paramInt2, paramInt3, paramintW);
         //   lapack.dtrti2(paramString1s, paramString2s, paramInt1, paramArrayOfDouble, paramInt2, paramInt3, paramintWw);
        }
        else if (bool2)
        {
            i = 1;
            for (n = (paramInt1 - 1 + k) / k; n > 0; n--)
            {
                j = Math.min(k, paramInt1 - i + 1);
               // blas.dtrmm("Left", "Upper", "No transpose", paramString2s, i - 1, j, 1.0D, paramArrayOfDouble, paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);
                dtrmm(KBlasSideType.LEFT, KBlasOrientationType.UPPER, KBlasTransposeType.NOTRANSPOSE, paramString2, i - 1, j, 1.0D, paramArrayOfDouble, paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);

                //blas.dtrsm("Right", "Upper", "No transpose", paramString2s, i - 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);
                dtrsm(KBlasSideType.RIGHT, KBlasOrientationType.UPPER, KBlasTransposeType.NOTRANSPOSE, paramString2, i - 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);

                dtrti2(KBlasOrientationType.UPPER, paramString2, j, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramintW);
                i += k;
            }
        }
        else
        {
            m = (paramInt1 - 1) / k * k + 1;
            i = m;
            for (n = (1 - m + -k) / -k; n > 0; n--)
            {
                j = Math.min(k, paramInt1 - i + 1);
                if ((i + j > paramInt1 ? 0 : 1) != 0)
                {
                 //   blas.dtrmm("Left", "Lower", "No transpose", paramString2s, paramInt1 - i - j + 1, j, 1.0D, paramArrayOfDouble, i + j - 1 + (i + j - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);
                    dtrmm(KBlasSideType.LEFT, KBlasOrientationType.LOWER, KBlasTransposeType.NOTRANSPOSE, paramString2, paramInt1 - i - j + 1, j, 1.0D, paramArrayOfDouble, i + j - 1 + (i + j - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);
                    dtrsm(KBlasSideType.RIGHT, KBlasOrientationType.LOWER, KBlasTransposeType.NOTRANSPOSE, paramString2, paramInt1 - i - j + 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);

                  //  blas.dtrsm("Right", "Lower", "No transpose", paramString2s, paramInt1 - i - j + 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);
                }
                dtrti2(KBlasOrientationType.LOWER, paramString2, j, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramintW);
                i += -k;
            }
        }
    }

    public static void dtrsm(KBlasSideType paramString1, KBlasOrientationType paramString2, KBlasTransposeType paramString3, KBlasUnitType paramString4, int paramInt1, int paramInt2, double paramDouble, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6)
    {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        boolean bool1 = false;
        boolean bool2 = false;
        boolean bool3 = false;
        bool1 =paramString1.equals(KBlasSideType.LEFT);
        if (bool1) {
            n = paramInt1;
        } else {
            n = paramInt2;
        }
        bool2 = paramString4.equals(KBlasUnitType.NONUNIT);
        bool3 = paramString2.equals(KBlasOrientationType.UPPER);
        /*j = 0;
        if ((((!bool1)) && ((!paramString1.equals(KBlasSideType.RIGHT))) ? 1 : 0) != 0)
        {
            j = 1;
        }
        else if ((((!bool3)) && ((!paramString2.equals(KBlasOrientationType.LOWER))) ? 1 : 0) != 0)
        {
            j = 2;
        }
        else
        {
            if (((!paramString3.equals(KBlasTransposeType.CONJUGATE)) ? 1 : 0) != 0) {
                j = 3;
            } else if ((((!paramString4.equals(KBlasUnitType.UNIT))) && ((!paramString4.equals(KBlasUnitType.NONUNIT))) ? 1 : 0) != 0) {
                j = 4;
            } else if ((paramInt1 >= 0 ? 0 : 1) != 0) {
                j = 5;
            } else if ((paramInt2 >= 0 ? 0 : 1) != 0) {
                j = 6;
            } else if ((paramInt4 >= Math.max(1, n) ? 0 : 1) != 0) {
                j = 9;
            } else if ((paramInt6 >= Math.max(1, paramInt1) ? 0 : 1) != 0) {
                j = 11;
            }
        }
        if ((j == 0 ? 0 : 1) != 0)
        {
            //Xerbla.xerbla("DTRSM ", j);
            return;
        }
        if (((paramInt2 != 0 ? 0 : 1) == 0 ? 0 : 1) != 0) {
            return;
        }*/
        int i1;
        int i2;
        if ((paramDouble != 0.0D ? 0 : 1) != 0)
        {
            k = 1;
            for (i1 = paramInt2 ; i1 > 0; i1--)
            {
                i = 1;
                for (i2 = paramInt1 ; i2 > 0; i2--)
                {
                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = 0.0D;
                    i += 1;
                }
                k += 1;
            }
            return;
        }
        int i3;
        if (bool1)
        {
            if (paramString3.equals(KBlasTransposeType.NOTRANSPOSE))
            {
                if (bool3)
                {
                    k = 1;
                    for (i1 = paramInt2 ; i1 > 0; i1--)
                    {
                        if ((paramDouble == 1.0D ? 0 : 1) != 0)
                        {
                            i = 1;
                            for (i2 = paramInt1 ; i2 > 0; i2--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                                i += 1;
                            }
                        }
                        m = paramInt1;
                        for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                        {
                            if ((paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                            {
                                if (bool2) {
                                    paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] /= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                                }
                                i = 1;
                                for (i3 = m - 1 ; i3 > 0; i3--)
                                {
                                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt4 + paramInt3)];
                                    i += 1;
                                }
                            }
                            m += -1;
                        }
                        k += 1;
                    }
                }
                else
                {
                    k = 1;
                    for (i1 = paramInt2 ; i1 > 0; i1--)
                    {
                        if ((paramDouble == 1.0D ? 0 : 1) != 0)
                        {
                            i = 1;
                            for (i2 = paramInt1 ; i2 > 0; i2--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                                i += 1;
                            }
                        }
                        m = 1;
                        for (i2 = paramInt1 ; i2 > 0; i2--)
                        {
                            if ((paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] == 0.0D ? 0 : 1) != 0)
                            {
                                if (bool2) {
                                    paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] /= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                                }
                                i = m + 1;
                                for (i3 = paramInt1 - (m + 1) + 1; i3 > 0; i3--)
                                {
                                    paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)] * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt4 + paramInt3)];
                                    i += 1;
                                }
                            }
                            m += 1;
                        }
                        k += 1;
                    }
                }
            }
            else if (bool3)
            {
                k = 1;
                for (i1 = paramInt2 ; i1 > 0; i1--)
                {
                    i = 1;
                    for (i2 = paramInt1 ; i2 > 0; i2--)
                    {
                        d = paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)];
                        m = 1;
                        for (i3 = i - 1 ; i3 > 0; i3--)
                        {
                            d -= paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                            m += 1;
                        }
                        if (bool2) {
                            d /= paramArrayOfDouble1[(i - 1 + (i - 1) * paramInt4 + paramInt3)];
                        }
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = d;
                        i += 1;
                    }
                    k += 1;
                }
            }
            else
            {
                k = 1;
                for (i1 = paramInt2 ; i1 > 0; i1--)
                {
                    i = paramInt1;
                    for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                    {
                        d = paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)];
                        m = i + 1;
                        for (i3 = paramInt1 - (i + 1) + 1; i3 > 0; i3--)
                        {
                            d -= paramArrayOfDouble1[(m - 1 + (i - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(m - 1 + (k - 1) * paramInt6 + paramInt5)];
                            m += 1;
                        }
                        if (bool2) {
                            d /= paramArrayOfDouble1[(i - 1 + (i - 1) * paramInt4 + paramInt3)];
                        }
                        paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = d;
                        i += -1;
                    }
                    k += 1;
                }
            }
        }
        else if (paramString3.equals(KBlasTransposeType.NOTRANSPOSE))
        {
            if (bool3)
            {
                k = 1;
                for (i1 = paramInt2 ; i1 > 0; i1--)
                {
                    if ((paramDouble == 1.0D ? 0 : 1) != 0)
                    {
                        i = 1;
                        for (i2 = paramInt1 ; i2 > 0; i2--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                            i += 1;
                        }
                    }
                    m = 1;
                    for (i2 = k - 1 ; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                        {
                            i = 1;
                            for (i3 = paramInt1 ; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    if (bool2)
                    {
                        d = 1.0D / paramArrayOfDouble1[(k - 1 + (k - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i2 = paramInt1 ; i2 > 0; i2--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                            i += 1;
                        }
                    }
                    k += 1;
                }
            }
            else
            {
                k = paramInt2;
                for (i1 = (1 - paramInt2 + -1) / -1; i1 > 0; i1--)
                {
                    if ((paramDouble == 1.0D ? 0 : 1) != 0)
                    {
                        i = 1;
                        for (i2 = paramInt1 ; i2 > 0; i2--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                            i += 1;
                        }
                    }
                    m = k + 1;
                    for (i2 = paramInt2 - (k + 1) + 1; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                        {
                            i = 1;
                            for (i3 = paramInt1 ; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= paramArrayOfDouble1[(m - 1 + (k - 1) * paramInt4 + paramInt3)] * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                                i += 1;
                            }
                        }
                        m += 1;
                    }
                    if (bool2)
                    {
                        d = 1.0D / paramArrayOfDouble1[(k - 1 + (k - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i2 = paramInt1 ; i2 > 0; i2--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)]);
                            i += 1;
                        }
                    }
                    k += -1;
                }
            }
        }
        else if (bool3)
        {
            m = paramInt2;
            for (i1 = (1 - paramInt2 + -1) / -1; i1 > 0; i1--)
            {
                if (bool2)
                {
                    d = 1.0D / paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                    i = 1;
                    for (i2 = paramInt1 ; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                k = 1;
                for (i2 = m - 1 ; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i3 = paramInt1 ; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                            i += 1;
                        }
                    }
                    k += 1;
                }
                if ((paramDouble == 1.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i2 = paramInt1 ; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                m += -1;
            }
        }
        else
        {
            m = 1;
            for (i1 = paramInt2 ; i1 > 0; i1--)
            {
                if (bool2)
                {
                    d = 1.0D / paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt4 + paramInt3)];
                    i = 1;
                    for (i2 = paramInt1 ; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                k = m + 1;
                for (i2 = paramInt2 - (m + 1) + 1; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramArrayOfDouble1[(k - 1 + (m - 1) * paramInt4 + paramInt3)];
                        i = 1;
                        for (i3 = paramInt1 ; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(i - 1 + (k - 1) * paramInt6 + paramInt5)] -= d * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)];
                            i += 1;
                        }
                    }
                    k += 1;
                }
                if ((paramDouble == 1.0D ? 0 : 1) != 0)
                {
                    i = 1;
                    for (i2 = paramInt1 ; i2 > 0; i2--)
                    {
                        paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)] = (paramDouble * paramArrayOfDouble2[(i - 1 + (m - 1) * paramInt6 + paramInt5)]);
                        i += 1;
                    }
                }
                m += 1;
            }
        }
    }

    public void dtrti2(KBlasOrientationType paramString1, KBlasUnitType paramString2, int paramInt1, double[] paramArrayOfDouble, int paramInt2, int paramInt3, int[] paramintW)
    {
        String paramString1s="Upper";
        String paramString2s="Non-unit" ;

        boolean bool1 = false;
        boolean bool2 = false;
        int i = 0;
        double d = 0.0D;
        paramintW[0] = 0;
        bool2 = paramString1.equals(KBlasOrientationType.UPPER);
        bool1 = paramString2.equals(KBlasUnitType.NONUNIT);
        if ((((!bool2)) && ((!paramString1.equals(KBlasOrientationType.LOWER))) ? 1 : 0) != 0) {
            paramintW[0] = -1;
        } else if ((((!bool1)) && ((!paramString2.equals(KBlasUnitType.UNIT))) ? 1 : 0) != 0) {
            paramintW[0] = -2;
        } else if ((paramInt1 >= 0 ? 0 : 1) != 0) {
            paramintW[0] = -3;
        } else if ((paramInt3 >= Math.max(1, paramInt1) ? 0 : 1) != 0) {
            paramintW[0] = -5;
        }
        if ((paramintW[0] == 0 ? 0 : 1) != 0)
        {
            //Xerbla.xerbla("DTRTI2", -paramintW[0]);
            return;
        }
        int j;
        if (bool2)
        {
            i = 1;
            for (j = paramInt1 - 1 + 1; j > 0; j--)
            {
                if (bool1)
                {
                    paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)] = (1.0D / paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)]);
                    d = -paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)];
                }
                else
                {
                    d = -1.0D;
                }
              // blas.dtrmv("Upper", "No transpose", paramString2s, i - 1, paramArrayOfDouble, paramInt2, paramInt3, paramArrayOfDouble, 1 - 1 + (i - 1) * paramInt3 + paramInt2, 1);

                dtrmv("Upper", "No transpose", paramString2s, i - 1, paramArrayOfDouble, paramInt2, paramInt3, paramArrayOfDouble, 1 - 1 + (i - 1) * paramInt3 + paramInt2, 1);
                dscal(i - 1, d, paramArrayOfDouble, 1 - 1 + (i - 1) * paramInt3 + paramInt2, 1);

                i += 1;
            }
        }
        else
        {
            i = paramInt1;
            for (j = (1 - paramInt1 + -1) / -1; j > 0; j--)
            {
                if (bool1) {
                    paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)] = (1.0D / paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)]);
                    d = -paramArrayOfDouble[(i - 1 + (i - 1) * paramInt3 + paramInt2)];
                }
                else
                {
                    d = -1.0D;
                }
                if ((i >= paramInt1 ? 0 : 1) != 0)
                {
                    dtrmv("Lower", "No transpose", paramString2s, paramInt1 - i, paramArrayOfDouble, i + (i + 1 - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + (i - 1) * paramInt3 + paramInt2, 1);
                    dscal(paramInt1 - i, d, paramArrayOfDouble, i + (i - 1) * paramInt3 + paramInt2, 1);
                }
                i += -1;
            }
        }
    }





    public void dtrmv(String paramString1, String paramString2, String paramString3, int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, double[] paramArrayOfDouble2, int paramInt4, int paramInt5)
    {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        int n = 0;
        int i1 = 0;
        boolean bool = false;
        j = 0;

        bool = Lsame.lsame(paramString3, "N");
        if ((paramInt5 > 0 ? 0 : 1) != 0) {
            i1 = 1 - (paramInt1 - 1) * paramInt5;
        } else if ((paramInt5 == 1 ? 0 : 1) != 0) {
            i1 = 1;
        }
        int i2;
        int i3;
        if (Lsame.lsame(paramString2, "N"))
        {
            if (Lsame.lsame(paramString1, "U"))
            {
                if ((paramInt5 != 1 ? 0 : 1) != 0)
                {
                    m = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble2[(m - 1 + paramInt4)] == 0.0D ? 0 : 1) != 0)
                        {
                            d = paramArrayOfDouble2[(m - 1 + paramInt4)];
                            i = 1;
                            for (i3 = m - 1 - 1 + 1; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(i - 1 + paramInt4)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)];
                                i += 1;
                            }
                            if (bool) {
                                paramArrayOfDouble2[(m - 1 + paramInt4)] *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                            }
                        }
                        m += 1;
                    }
                }
                else
                {
                    n = i1;
                    m = 1;
                    for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
                    {
                        if ((paramArrayOfDouble2[(n - 1 + paramInt4)] == 0.0D ? 0 : 1) != 0)
                        {
                            d = paramArrayOfDouble2[(n - 1 + paramInt4)];
                            k = i1;
                            i = 1;
                            for (i3 = m - 1 - 1 + 1; i3 > 0; i3--)
                            {
                                paramArrayOfDouble2[(k - 1 + paramInt4)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)];
                                k += paramInt5;
                                i += 1;
                            }
                            if (bool) {
                                paramArrayOfDouble2[(n - 1 + paramInt4)] *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                            }
                        }
                        n += paramInt5;
                        m += 1;
                    }
                }
            }
            else if ((paramInt5 != 1 ? 0 : 1) != 0)
            {
                m = paramInt1;
                for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble2[(m - 1 + paramInt4)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramArrayOfDouble2[(m - 1 + paramInt4)];
                        i = paramInt1;
                        for (i3 = (m + 1 - paramInt1 + -1) / -1; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(i - 1 + paramInt4)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)];
                            i += -1;
                        }
                        if (bool) {
                            paramArrayOfDouble2[(m - 1 + paramInt4)] *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                        }
                    }
                    m += -1;
                }
            }
            else
            {
                i1 += (paramInt1 - 1) * paramInt5;
                n = i1;
                m = paramInt1;
                for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                {
                    if ((paramArrayOfDouble2[(n - 1 + paramInt4)] == 0.0D ? 0 : 1) != 0)
                    {
                        d = paramArrayOfDouble2[(n - 1 + paramInt4)];
                        k = i1;
                        i = paramInt1;
                        for (i3 = (m + 1 - paramInt1 + -1) / -1; i3 > 0; i3--)
                        {
                            paramArrayOfDouble2[(k - 1 + paramInt4)] += d * paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)];
                            k -= paramInt5;
                            i += -1;
                        }
                        if (bool) {
                            paramArrayOfDouble2[(n - 1 + paramInt4)] *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                        }
                    }
                    n -= paramInt5;
                    m += -1;
                }
            }
        }
        else if (Lsame.lsame(paramString1, "U"))
        {
            if ((paramInt5 != 1 ? 0 : 1) != 0)
            {
                m = paramInt1;
                for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                {
                    d = paramArrayOfDouble2[(m - 1 + paramInt4)];
                    if (bool) {
                        d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                    }
                    i = m - 1;
                    for (i3 = (1 - (m - 1) + -1) / -1; i3 > 0; i3--)
                    {
                        d += paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)] * paramArrayOfDouble2[(i - 1 + paramInt4)];
                        i += -1;
                    }
                    paramArrayOfDouble2[(m - 1 + paramInt4)] = d;
                    m += -1;
                }
            }
            else
            {
                n = i1 + (paramInt1 - 1) * paramInt5;
                m = paramInt1;
                for (i2 = (1 - paramInt1 + -1) / -1; i2 > 0; i2--)
                {
                    d = paramArrayOfDouble2[(n - 1 + paramInt4)];
                    k = n;
                    if (bool) {
                        d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                    }
                    i = m - 1;
                    for (i3 = (1 - (m - 1) + -1) / -1; i3 > 0; i3--)
                    {
                        k -= paramInt5;
                        d += paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)] * paramArrayOfDouble2[(k - 1 + paramInt4)];
                        i += -1;
                    }
                    paramArrayOfDouble2[(n - 1 + paramInt4)] = d;
                    n -= paramInt5;
                    m += -1;
                }
            }
        }
        else if ((paramInt5 != 1 ? 0 : 1) != 0)
        {
            m = 1;
            for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
            {
                d = paramArrayOfDouble2[(m - 1 + paramInt4)];
                if (bool) {
                    d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                }
                i = m + 1;
                for (i3 = paramInt1 - (m + 1) + 1; i3 > 0; i3--)
                {
                    d += paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)] * paramArrayOfDouble2[(i - 1 + paramInt4)];
                    i += 1;
                }
                paramArrayOfDouble2[(m - 1 + paramInt4)] = d;
                m += 1;
            }
        }
        else
        {
            n = i1;
            m = 1;
            for (i2 = paramInt1 - 1 + 1; i2 > 0; i2--)
            {
                d = paramArrayOfDouble2[(n - 1 + paramInt4)];
                k = n;
                if (bool) {
                    d *= paramArrayOfDouble1[(m - 1 + (m - 1) * paramInt3 + paramInt2)];
                }
                i = m + 1;
                for (i3 = paramInt1 - (m + 1) + 1; i3 > 0; i3--)
                {
                    k += paramInt5;
                    d += paramArrayOfDouble1[(i - 1 + (m - 1) * paramInt3 + paramInt2)] * paramArrayOfDouble2[(k - 1 + paramInt4)];
                    i += 1;
                }
                paramArrayOfDouble2[(n - 1 + paramInt4)] = d;
                n += paramInt5;
                m += 1;
            }
        }
    }

    @Override
    public void dgetrf(int paramInt1, int paramInt2, double[] paramArrayOfDouble, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetrf(paramInt1, paramInt2, paramArrayOfDouble, paramInt3, paramInt4, paramArrayOfInt, paramInt5, newint);
        paramintW[0] = newint.val;
    }

    @Override
    public void trans(KArray2D matA, KArray2D result) {
        new JavaBlas().trans(matA,result);
    }


    /*public void dscal(double alpha, KArray2D matA) {
        blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
    }*/

    public static void dscal(int paramInt1, double paramDouble, double[] paramArrayOfDouble, int paramInt2, int paramInt3)
    {
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        if ((paramInt1 > 0 ? 0 : 1) == 0) {}
        if (((paramInt3 > 0 ? 0 : 1) == 0 ? 0 : 1) != 0) {
            return;
        }
        int n;
        if ((paramInt3 != 1 ? 0 : 1) != 0)
        {
            j = paramInt1 % 5;
            if ((j == 0 ? 0 : 1) != 0)
            {
                i = 1;
                for (n = j ; n > 0; n--)
                {
                    paramArrayOfDouble[(i - 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i - 1 + paramInt2)]);
                    i += 1;
                }
                if ((paramInt1 >= 5 ? 0 : 1) != 0) {
                    return;
                }
            }
            k = j + 1;
            i = k;
            for (n = (paramInt1 - k + 5) / 5; n > 0; n--)
            {
                paramArrayOfDouble[(i - 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i - 1 + paramInt2)]);
                paramArrayOfDouble[(i + paramInt2)] = (paramDouble * paramArrayOfDouble[(i + paramInt2)]);
                paramArrayOfDouble[(i + 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i + 1 + paramInt2)]);
                paramArrayOfDouble[(i + 3 - 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i + 3 - 1 + paramInt2)]);
                paramArrayOfDouble[(i + 4 - 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i + 4 - 1 + paramInt2)]);
                i += 5;
            }
        } else {
            m = paramInt1 * paramInt3;
            i = 1;
            for (n = (m - 1 + paramInt3) / paramInt3; n > 0; n--)
            {
                paramArrayOfDouble[(i - 1 + paramInt2)] = (paramDouble * paramArrayOfDouble[(i - 1 + paramInt2)]);
                i += paramInt3;
            }
        }
    }


    public void dgemm2(KBlasTransposeType transa, KBlasTransposeType transb, double alpha, KArray2D matA, KArray2D matB, double beta, KArray2D matC) {
        blas.dgemm(transTypeToChar(transa), transTypeToChar(transb), matA.rows(), matB.columns(), matA.columns(), alpha, matA.data(), matA.rows(), matB.data(), matB.rows(), beta, matC.data(), matC.rows());
    }

    @Override
    public void shutdown() {
        blas = null;
    }

    @Override
    public void dscale(double alpha, KArray2D matA) {
       // blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
    }

    private static final String TRANSPOSE_TYPE_CONJUCATE = "c";

    private static final String TRANSPOSE_TYPE_NOTRANSPOSE = "n";

    private static final String TRANSPOSE_TYPE_TRANSPOSE = "t";

    private static String transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return null;
    }

}
