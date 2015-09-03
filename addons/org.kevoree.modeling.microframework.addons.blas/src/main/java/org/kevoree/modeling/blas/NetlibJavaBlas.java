package org.kevoree.modeling.blas;

import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.netlib.util.intW;

import java.util.Objects;

public class NetlibJavaBlas implements KBlas {
    private BLAS blas;
    private LAPACK lapack;


    public NetlibJavaBlas(){
        try{
            blas=(BLAS) load("com.github.fommil.netlib.F2jBLAS");
            lapack=(LAPACK) load("com.github.fommil.netlib.F2jLAPACK");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static Object load(String className) throws Exception {
        Class klass = Class.forName(className);
        return klass.newInstance();
    }

    @Override
    public void dgemm(KBlasTransposeType paramString1, KBlasTransposeType paramString2, int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double[] paramArrayOfDouble1, int paramInt4, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, double paramDouble2, double[] paramArrayOfDouble3, int paramInt8, int paramInt9) {
        blas.dgemm(transTypeToChar(paramString1),transTypeToChar(paramString2), paramInt1,  paramInt2,  paramInt3,  paramDouble1, paramArrayOfDouble1, paramInt4, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, paramDouble2, paramArrayOfDouble3, paramInt8, paramInt9);
        }

    @Override
    public void dgetrs(KBlasTransposeType paramString, int paramInt1, int paramInt2, double[] paramArrayOfDouble1, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, double[] paramArrayOfDouble2, int paramInt6, int paramInt7, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetrs(transTypeToChar(paramString), paramInt1, paramInt2, paramArrayOfDouble1, paramInt3, paramInt4, paramArrayOfInt, paramInt5, paramArrayOfDouble2, paramInt6, paramInt7, newint);
        paramintW[0] = newint.val;
    }

    @Override
    public void dgetri(int paramInt1, double[] paramArrayOfDouble1, int paramInt2, int paramInt3, int[] paramArrayOfInt, int paramInt4, double[] paramArrayOfDouble2, int paramInt5, int paramInt6, int[] paramintW) {
        intW newint = new intW(paramintW[0]);
        lapack.dgetri(paramInt1, paramArrayOfDouble1,paramInt2, paramInt3, paramArrayOfInt, paramInt4, paramArrayOfDouble2, paramInt5, paramInt6, newint);
        paramintW[0] = newint.val;

    }

  /*
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
                    blas.dgemv("No transpose", paramInt1, paramInt1 - m, -1.0D, paramArrayOfDouble1, (m) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble2, m + paramInt5, 1, 1.0D, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, 1);
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
                blas.dtrsm("Right", "Lower", "No transpose", "Unit", paramInt1, n, 1.0D, paramArrayOfDouble2, m - 1 + paramInt5, i3, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, paramInt3);
                m += -i5;
            }
        }
        m = paramInt1 - 1;
        for (int i8 = (1 - (paramInt1 - 1) + -1) / -1; i8 > 0; i8--)
        {
            i2 = paramArrayOfInt[(m - 1 + paramInt4)];
            if ((i2 == m ? 0 : 1) != 0) {
                blas.dswap(paramInt1, paramArrayOfDouble1, (m - 1) * paramInt3 + paramInt2, 1, paramArrayOfDouble1, (i2 - 1) * paramInt3 + paramInt2, 1);
            }
            m += -1;
        }
        paramArrayOfDouble2[(paramInt5)] = k;
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
                blas.dtrmm("Left", "Upper", "No transpose", paramString2s, i - 1, j, 1.0D, paramArrayOfDouble, paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);
                blas.dtrsm("Right", "Upper", "No transpose", paramString2s, i - 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, (i - 1) * paramInt3 + paramInt2, paramInt3);
                lapack.dtrti2("Upper", paramString2s, j, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramintWw);
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
                    blas.dtrmm("Left", "Lower", "No transpose", paramString2s, paramInt1 - i - j + 1, j, 1.0D, paramArrayOfDouble, i + j - 1 + (i + j - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);
                    blas.dtrsm("Right", "Lower", "No transpose", paramString2s, paramInt1 - i - j + 1, j, -1.0D, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + j - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3);
                }
                lapack.dtrti2("Lower", paramString2s, j, paramArrayOfDouble, i - 1 + (i - 1) * paramInt3 + paramInt2, paramInt3, paramintWw);
                i += -k;
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

                blas.dscal(i - 1, d, paramArrayOfDouble, 1 - 1 + (i - 1) * paramInt3 + paramInt2, 1);

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
                    blas.dtrmv("Lower", "No transpose", paramString2s, paramInt1 - i, paramArrayOfDouble, i + (i + 1 - 1) * paramInt3 + paramInt2, paramInt3, paramArrayOfDouble, i + (i - 1) * paramInt3 + paramInt2, 1);
                    blas.dscal(paramInt1 - i, d, paramArrayOfDouble, i + (i - 1) * paramInt3 + paramInt2, 1);
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
*/
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


    public void dscal(double alpha, KArray2D matA) {
        blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
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
        blas.dscal(matA.rows() * matA.columns(), alpha, matA.data(), 1);
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
