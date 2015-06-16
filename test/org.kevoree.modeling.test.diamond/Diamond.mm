class org.kevoree.diamond.A {
    att_a1 : String
    att_a2 : String
    ref_a1 : org.kevoree.diamond.A
    ref_a2 : org.kevoree.diamond.A
}

class org.kevoree.diamond.B : org.kevoree.diamond.A {
    att_b1 : String
    att_b2 : String
    ref_b1 : org.kevoree.diamond.A
    ref_b2 : org.kevoree.diamond.A
}

class org.kevoree.diamond.CL : org.kevoree.diamond.B{
    ref_cl1 : org.kevoree.diamond.A
}

class org.kevoree.diamond.CR : org.kevoree.diamond.B{
    att_cr1 : String
    att_cr2 : String
    att_cr3 : String
}

class org.kevoree.diamond.CRA : org.kevoree.diamond.CR {
    ref_cra1 : org.kevoree.diamond.A
}

class org.kevoree.diamond.D : org.kevoree.diamond.CL, org.kevoree.diamond.CRA {
    att_d1 : String
    att_d2 : String
    ref_d1 : org.kevoree.diamond.A
}

class org.kevoree.diamond.E : org.kevoree.diamond.D {
    att_e1 : String
    att_e2 : String
    ref_e1 : org.kevoree.diamond.A
}