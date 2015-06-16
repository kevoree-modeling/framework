
class kmf.opposite.test.Container  {
    @contained
    bees : kmf.opposite.test.B[0,*]
    @contained
    aees : kmf.opposite.test.A[0,*]
    @contained
    cees : kmf.opposite.test.C[0,*]
}

class kmf.opposite.test.C  {
    @contained
    bees : kmf.opposite.test.B[0,*]
}

class kmf.opposite.test.A  {
    singleRef : kmf.opposite.test.B
    multiRef : kmf.opposite.test.B[0,*]
    singleA_singleB : kmf.opposite.test.B oppositeOf singleA_singleB
    singleA_multiB : kmf.opposite.test.B oppositeOf singleA_multiB
    multiA_singleB : kmf.opposite.test.B[0,*] oppositeOf multiA_singleB
    multiA_multiB : kmf.opposite.test.B[0,*] oppositeOf multiA_multiB
    oppositeSimpleA_oppositeSimpleB : kmf.opposite.test.B[0,*] oppositeOf oppositeSimpleA_oppositeSimpleB
}

class kmf.opposite.test.B  {
    @contained
    singleRef : kmf.opposite.test.A
    @contained
    multiRef : kmf.opposite.test.A[0,*]
    @contained
    singleA_singleB : kmf.opposite.test.A oppositeOf singleA_singleB
    @contained
    singleA_multiB : kmf.opposite.test.A[0,*] oppositeOf singleA_multiB
    @contained
    multiA_singleB : kmf.opposite.test.A oppositeOf multiA_singleB
    @contained
    multiA_multiB : kmf.opposite.test.A[0,*] oppositeOf multiA_multiB

    oppositeSimpleA_oppositeSimpleB : kmf.opposite.test.A[0,*]
}
