class org.kevoree.cloud.Cloud {
    @contained
    nodes : org.kevoree.cloud.Node[0,*]
    @contained
    elements : org.kevoree.cloud.Element[0,*]
}

class org.kevoree.cloud.Node {
    @id
    name : String
    value : String
}

class org.kevoree.cloud.Element {
    @id
    name : String
    value : String
}


enum org.kevoree.cloud.CloudEnumTester {
    VAL1 VAL2 VAL3
}

