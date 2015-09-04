package test;

import org.junit.Test;
import org.kevoree.modeling.blas.NetlibBlas;
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
        KArray2D matA= MatrixOperations.random(5,3);
        KBlas java = new JavaBlas();
        KBlas netLib = new NetlibBlas();
        double eps=1e-7;

        QR qrjava = new QR(matA.rows(),matA.columns());
        QR qrNetlib = new QR(matA.rows(),matA.columns());

        qrjava.factor(matA,false,java);
        qrNetlib.factor(matA,false,netLib);

        KArray2D Qjava= qrjava.getQ();
        KArray2D Rjava= qrjava.getR();
        KArray2D Qnetlib = qrNetlib.getQ();
        KArray2D Rnetlib = qrNetlib.getR();

        System.out.println(MatrixOperations.compareMatrix(Qjava,Qnetlib,eps));
        System.out.println(MatrixOperations.compareMatrix(Rjava,Rnetlib,eps));
    }
}
