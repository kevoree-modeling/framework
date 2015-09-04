package test;

import org.junit.Test;
import org.kevoree.modeling.blas.F2JBlas;
import org.kevoree.modeling.blas.NetlibBlas;
import org.kevoree.modeling.blas.NetlibBlasDebug;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;
import org.kevoree.modeling.util.maths.structure.blas.impl.JavaBlas;
import org.kevoree.modeling.util.maths.structure.matrix.MatrixOperations;
import org.kevoree.modeling.util.maths.structure.matrix.solver.QR;

/**
 * Created by assaad on 04/09/15.
 */
public class QRTest {
    @Test
    public void qrTest(){
        KArray2D matA= MatrixOperations.random(2000,1000);
        KBlas java = new JavaBlas();
        KBlas netLib = new NetlibBlas();
        double eps=1e-7;

        long timestart,timeend;


        QR qrjava = new QR(matA.rows(),matA.columns());

        QR qrNetlib = new QR(matA.rows(),matA.columns());


        timestart = System.currentTimeMillis();
        qrjava.factor(matA,false,java);
        timeend=System.currentTimeMillis();
        System.out.println("java factorisation " + ((double) (timeend - timestart)) / 1000 + " s");

        timestart = System.currentTimeMillis();
        qrNetlib.factor(matA, false, netLib);
        timeend=System.currentTimeMillis();
        System.out.println("Netlib factorisation " + ((double) (timeend - timestart)) / 1000 + " s");

        KArray2D Qjava= qrjava.getQ();
        KArray2D Rjava= qrjava.getR();
        KArray2D Qnetlib = qrNetlib.getQ();
        KArray2D Rnetlib = qrNetlib.getR();

        double err1=MatrixOperations.compareMatrix(Qjava, Qnetlib);
        double err2=MatrixOperations.compareMatrix(Rjava, Rnetlib);
                System.out.println(err1);
        System.out.println(err2);

        int x=5;
    }
}
