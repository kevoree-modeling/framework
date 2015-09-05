package org.kevoree.modeling.blas;

import static jcuda.jcublas.JCublas2.*;
import static jcuda.runtime.JCuda.*;
import jcuda.driver.JCudaDriver;
import jcuda.jcublas.*;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasHandle;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.KBlasTransposeType;
import org.nd4j.linalg.jcublas.blas.JcublasLevel1;
import org.nd4j.linalg.jcublas.blas.JcublasLevel3;

import static jcuda.jcublas.JCublas2.cublasCreate;
import static jcuda.jcublas.JCublas2.cublasDestroy;


public class JCudaBlas implements KBlas {
    private static final Log LOG = LogFactory.getLog(JCudaBlas.class);
    public static boolean EXCEPTIONS_ENABLED = false;
    public static boolean CUBLAS2_AVAILABLE = false;

    private static cublasHandle handle;

    public JCudaBlas() {
        //Initialize the kernel
        JCublas.cublasInit();
        JCublas2.initialize();
        handle = new cublasHandle();
        cublasCreate(handle);



        try {
            JCuda.setExceptionsEnabled(EXCEPTIONS_ENABLED);
            cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
            JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
            // verify that compute capability of 1.3 is available, because only
            // here is the double precision operation allowed.
            if (cudaDeviceProp.major <= 1 && cudaDeviceProp.minor < 3) {
                throw new IllegalArgumentException(
                        "WARN Double precision computing only allowed since capability 1.3! You have "
                                + cudaDeviceProp.major
                                + "."
                                + cudaDeviceProp.minor
                                + "! If you have exceptions turned off, then this may result in strange behaviour.");
            }
            // actually here is only cublas2 available.
            if (Integer.parseInt(cudaDeviceProp.getName().replaceAll("[^\\d]", "")) > 400) {
                JCublas2.setExceptionsEnabled(EXCEPTIONS_ENABLED);
                JCublas2.initialize();
                CUBLAS2_AVAILABLE = true;
                //System.out.println("cublas enabled");
                handle = new cublasHandle();
                JCublas2.cublasCreate(handle);
                JCublas2.cublasSetPointerMode(handle,
                        cublasPointerMode.CUBLAS_POINTER_MODE_HOST);
            } else {
                JCublas.setExceptionsEnabled(EXCEPTIONS_ENABLED);
                JCublas.cublasInit();
            }

            // cleanup that handle at the end of this process
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    JCudaBlas.cublasDestroy(handle);
                }
            });

            LOG.info("Using device " + cudaDeviceProp.getName()
                    + " with total RAM of "
                    + cudaDeviceProp.totalGlobalMem
                    + ". Compute capability: " + cudaDeviceProp.major + "."
                    + cudaDeviceProp.minor);

        } catch (Throwable e) {
            // e.printStackTrace();
            LOG.error(e.getLocalizedMessage());
        }

    }

    private static void cublasDestroy(cublasHandle handle) {
        cublasDestroy(handle);
    }


    public Pointer allocAndCopy(double[] matA){
        Pointer d_A = new Pointer();
        JCuda.cudaMalloc(d_A,matA.length*Sizeof.DOUBLE);
        cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        //old
        // JCublas.cublasAlloc(matA.length, Sizeof.DOUBLE, d_A);
        // JCublas.cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        return d_A;
    }




    @Override
    public void dgemm(KBlasTransposeType transA, KBlasTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC) {
        Pointer d_A = allocAndCopy(matA);
        Pointer d_B = allocAndCopy(matB);
        Pointer d_C = allocAndCopy(matC);
        Pointer d_alpha = allocAndCopy(new double[]{alpha});
        Pointer d_beta = allocAndCopy(new double[]{beta});

        // Execute dgemm


            JCuda.cudaDeviceSynchronize();
            int x=  JCublas2.cublasDgemm(handle,transTypeToInt(transA), transTypeToInt(transB), m, n, k, d_alpha, d_A, ldA, d_B, ldB, d_beta, d_C, ldC);
            JCuda.cudaDeviceSynchronize();

          //  handle.wait();

        //JCuda.cudaDeviceSynchronize();
        //ContextHolder.syncStream();

      //  System.out.println(x);

         //JCublas.cublasDgemm(transTypeToChar(transA), transTypeToChar(transB), m, n, k, alpha, d_A, ldA, d_B, ldB, beta, d_C, ldC);

                 // Copy the result from the device to the host
        JCublas.cublasGetVector(matC.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matC), 1);

        // Clean up
        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);
        cudaFree(d_alpha);
        cudaFree(d_beta);
      /*  JCublas.cublasFree(d_A);
        JCublas.cublasFree(d_B);
        JCublas.cublasFree(d_C);
        JCublas.cublasFree(d_alpha);
        JCublas.cublasFree(d_beta);*/
    }

    @Override
    public void dgetrs(KBlasTransposeType transA, int dim, int nrhs, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] matB, int offsetB, int ldB, int[] info) {


      //  JCublas2.

    }

    @Override
    public void dgetri(int dim, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] work, int offsetWork, int ldWork, int[] info) {

    }

    @Override
    public void dgetrf(int rows, int columns, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, int[] info) {

    }

    @Override
    public void dorgqr(int m, int n, int k, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetWork, int lWork, int[] info) {

    }

    @Override
    public void dgeqrf(int m, int n, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetwork, int lWork, int[] info) {

    }

    @Override
      public void shutdown() {
        cublasDestroy(handle);
        JCublas.cublasShutdown();
    }

    private static final char TRANSPOSE_TYPE_CONJUCATE = 'c';

    private static final char TRANSPOSE_TYPE_NOTRANSPOSE = 'n';

    private static final char TRANSPOSE_TYPE_TRANSPOSE = 't';

    private static char transTypeToChar(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return TRANSPOSE_TYPE_CONJUCATE;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return '0';
    }


    private static int transTypeToInt(KBlasTransposeType type) {
        if (type.equals(KBlasTransposeType.CONJUGATE)) {
            return cublasOperation.CUBLAS_OP_C;
        } else if (type.equals(KBlasTransposeType.NOTRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_N;
        } else if (type.equals(KBlasTransposeType.TRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_T;
        }
        return '0';
    }

}
