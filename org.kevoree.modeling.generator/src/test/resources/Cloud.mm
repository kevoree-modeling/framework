class org.kevoree.cloud.AbsCloud  {
    with instantiation "false"

}

class org.kevoree.cloud.Cloud {
    with instantiation "true"
    rel nodes: org.kevoree.cloud.Node
    rel elements: org.kevoree.cloud.Element
}

class org.kevoree.cloud.Node {
    att name: String
    att value: String
}

class org.kevoree.cloud.Element {
    att name: String
    att value: String
}

enum org.kevoree.cloud.CloudEnumTester {
    VAL1, VAL2, VAL3
}

class org.kevoree.cloud.SubElem extends org.kevoree.cloud.Element {
    with inference "GaussianCluster"
    dependency myCloud: org.kevoree.cloud.Cloud
    dependency myCloud2: org.kevoree.cloud.Node
    output outputName: org.kevoree.cloud.CloudEnumTester
}