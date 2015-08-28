package org.kevoree.modeling.util.maths.matrix;

public class MatrixMatrixMult {

    //public static int MULT_COLUMN_SWITCH = 15;

    public static void multTransA_smallMV(DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C) {
        int cIndex = 0;
        for (int i = 0; i < A.numCols; i++) {
            double total = 0.0;
            int indexA = i;
            for (int j = 0; j < A.numRows; j++) {
                total += A.getValueAtIndex(indexA) * B.getValueAtIndex(j);
                indexA += A.numCols;
            }
            C.setValueAtIndex(cIndex++, total);
        }
    }

    public static void multTransA_reorderMV(DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C) {
        if (A.numRows == 0) {
            DenseMatrix64F.fill(C, 0);
            return;
        }
        double B_val = B.getValueAtIndex(0);
        for (int i = 0; i < A.numCols; i++) {
            C.setValueAtIndex(i, A.getValueAtIndex(i) * B_val);
        }
        int indexA = A.numCols;
        for (int i = 1; i < A.numRows; i++) {
            B_val = B.getValueAtIndex(i);
            for (int j = 0; j < A.numCols; j++) {
                C.plus(j, A.getValueAtIndex(indexA++) * B_val);
            }
        }
    }

    public static void multTransA_reorderMM(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        if (a.numCols == 0 || a.numRows == 0) {
            DenseMatrix64F.fill(c, 0);
            return;
        }
        double valA;
        for (int i = 0; i < a.numCols; i++) {
            int indexC_start = i * c.numCols;
            // first assign R
            valA = a.getValueAtIndex(i);
            int indexB = 0;
            int end = indexB + b.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                c.setValueAtIndex(indexC++, valA * b.getValueAtIndex(indexB++));
            }
            // now increment it
            for (int k = 1; k < a.numRows; k++) {
                valA = a.get(k, i);
                end = indexB + b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    c.plus(indexC++, valA * b.getValueAtIndex(indexB++));
                }
            }
        }
    }

    public static void multTransA_smallMM(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        int cIndex = 0;
        for (int i = 0; i < a.numCols; i++) {
            for (int j = 0; j < b.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows * b.numCols;
                double total = 0;
                // loop for k
                for (; indexB < end; indexB += b.numCols) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB);
                    indexA += a.numCols;
                }
                c.setValueAtIndex(cIndex++, total);
            }
        }
    }

    public static void multTransA(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        if (b.numCols == 1) {
            if (a.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH) {
                multTransA_reorderMV(a, b, c);
            } else {
                multTransA_smallMV(a, b, c);
            }
        } else if (a.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH || b.numCols >= DenseMatrix64F.MULT_COLUMN_SWITCH) {
            multTransA_reorderMM(a, b, c);
        } else {
            multTransA_smallMM(a, b, c);
        }
    }

    public static void mult_reorder(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {

        if (a.numCols == 0 || a.numRows == 0) {
            CommonOps.fill(c, 0);
            return;
        }
        double valA;
        int indexCbase = 0;
        int endOfKLoop = b.numRows * b.numCols;

        for (int i = 0; i < a.numRows; i++) {
            int indexA = i * a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.getValueAtIndex(indexA++);

            while (indexB < end) {
                c.setValueAtIndex(indexC++, valA * b.getValueAtIndex(indexB++));
            }

            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.getValueAtIndex(indexA++);

                while (indexB < end) { // j loop
                    c.plus(indexC++, valA * b.getValueAtIndex(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void mult_small(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        int aIndexStart = 0;
        int cIndex = 0;

        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < b.numCols; j++) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while (indexA < end) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB);
                    indexB += b.numCols;
                }

                c.setValueAtIndex(cIndex++, total);
            }
            aIndexStart += a.numCols;
        }
    }
    /*
    public static void mult_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.get(k, j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.getValueAtIndex(indexA++)*aux[k++];
                }
                c.setValueAtIndex(i * c.numCols+j , total );
            }
        }
    }*/

    public static void multTransA_reorder(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        if (a.numCols == 0 || a.numRows == 0) {
            CommonOps.fill(c, 0);
            return;
        }
        double valA;

        for (int i = 0; i < a.numCols; i++) {
            int indexC_start = i * c.numCols;

            // first assign R
            valA = a.getValueAtIndex(i);
            int indexB = 0;
            int end = indexB + b.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                c.setValueAtIndex(indexC++, valA * b.getValueAtIndex(indexB++));
            }
            // now increment it
            for (int k = 1; k < a.numRows; k++) {
                valA = a.get(k, i);
                end = indexB + b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    c.plus(indexC++, valA * b.getValueAtIndex(indexB++));
                }
            }
        }
    }

    public static void multTransA_small(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        int cIndex = 0;

        for (int i = 0; i < a.numCols; i++) {
            for (int j = 0; j < b.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows * b.numCols;

                double total = 0.0d;

                // loop for k
                for (; indexB < end; indexB += b.numCols) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB);
                    indexA += a.numCols;
                }

                c.setValueAtIndex(cIndex++, total);
            }
        }
    }

    /*
    public static void multTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0.0d;

                for( ;indexB<end; ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB++);
                    indexA += a.numCols;
                }

                c.setValueAtIndex(cIndex++, total);
            }
        }
    }
    public static void multTransAB_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ a.numRows ];

        if( a.numCols == 0 || a.numRows == 0 ) {
            CommonOps.fill(c,0);
            return;
        }
        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.get(k, i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.get(j, k);
                }
                c.setValueAtIndex(indexC++, total);
            }
        }
    }*/


    public static void multTransB(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
        int cIndex = 0;
        int aIndexStart = 0;

        for (int xA = 0; xA < a.numRows; xA++) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for (int xB = 0; xB < b.numRows; xB++) {
                int indexA = aIndexStart;

                double total = 0;

                while (indexA < end) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB++);
                }

                c.setValueAtIndex(cIndex++, total);
            }
            aIndexStart += a.numCols;
        }
    }

    /*
    public static void multAdd_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.getValueAtIndex(indexA++);

            while( indexB < end ) {
                c.plus(indexC++ , valA*b.getValueAtIndex(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.getValueAtIndex(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void multAdd_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB);
                    indexB += b.numCols;
                }

                c.plus( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }


    public static void multAdd_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
            if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.get(k, j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.getValueAtIndex(indexA++)*aux[k++];
                }
                c.plus( i*c.numCols+j , total );
            }
        }
    }

    public static void multAddTransA_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = a.getValueAtIndex(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.plus( indexC++ , valA*b.getValueAtIndex(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = a.get(k, i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
        }
    }


    public static void multAddTransA_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , total );
            }
        }
    }


    public static void multAddTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB++);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , total );
            }
        }
    }


    public static void multAddTransAB_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {

        if( aux == null ) aux = new double[ a.numRows ];

        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.get(k, i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.get(j, k);
                }
                c.plus( indexC++ , total );
            }
        }
    }


    public static void multAddTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB++);
                }

                c.plus( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }


    public static void mult_reorderalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols == 0 || a.numRows == 0 ) {
            CommonOps.fill(c,0);
            return;
        }
        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*a.getValueAtIndex(indexA++);

            while( indexB < end ) {
                c.setValueAtIndex(indexC++, valA * b.getValueAtIndex(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*a.getValueAtIndex(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }


    public static void mult_smallalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB);
                    indexB += b.numCols;
                }

                c.setValueAtIndex(cIndex++, alpha * total);
            }
            aIndexStart += a.numCols;
        }
    }


    public static void mult_auxalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.get(k, j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.getValueAtIndex(indexA++)*aux[k++];
                }
                c.setValueAtIndex(i * c.numCols + j, alpha * total);
            }
        }
    }
    public static void multTransA_reorderalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols == 0 || a.numRows == 0 ) {
            CommonOps.fill(c,0);
            return;
        }
        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*a.getValueAtIndex(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.setValueAtIndex(indexC++, valA * b.getValueAtIndex(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*a.get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
        }
    }
    public static void multTransA_smallalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB);
                    indexA += a.numCols;
                }

                c.setValueAtIndex(cIndex++, alpha * total);
            }
        }
    }
    public static void multTransABalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB++);
                    indexA += a.numCols;
                }

                c.setValueAtIndex(cIndex++, alpha * total);
            }
        }
    }
    public static void multTransAB_auxalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ a.numRows ];

        if( a.numCols == 0 || a.numRows == 0 ) {
            CommonOps.fill(c,0);
            return;
        }
        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.get(k, i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.get(j, k);
                }
                c.setValueAtIndex(indexC++, alpha * total);
            }
        }
    }
    public static void multTransBalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB++);
                }

                c.setValueAtIndex(cIndex++, alpha * total);
            }
            aIndexStart += a.numCols;
        }
    }
    public static void multAdd_reorderalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*a.getValueAtIndex(indexA++);

            while( indexB < end ) {
                c.plus(indexC++ , valA*b.getValueAtIndex(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*a.getValueAtIndex(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void multAdd_smallalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB);
                    indexB += b.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }


    public static void multAdd_auxalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.get(k, j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.getValueAtIndex(indexA++)*aux[k++];
                }
                c.plus( i*c.numCols+j , alpha*total );
            }
        }
    }


    public static void multAddTransA_reorderalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*a.getValueAtIndex(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.plus( indexC++ , valA*b.getValueAtIndex(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*a.get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.getValueAtIndex(indexB++));
                }
            }
        }
    }


    public static void multAddTransA_smallalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
        }
    }


    public static void multAddTransABalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.getValueAtIndex(indexA) * b.getValueAtIndex(indexB++);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
        }
    }


    public static void multAddTransAB_auxalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( aux == null ) aux = new double[ a.numRows ];

        if( a.numCols == 0 || a.numRows == 0 ) {
            return;
        }
        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.get(k, i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.get(j, k);
                }
                c.plus( indexC++ , alpha*total );
            }
        }
    }


    public static void multAddTransBalpha( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.getValueAtIndex(indexA++) * b.getValueAtIndex(indexB++);
                }

                c.plus( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }
*/
}
