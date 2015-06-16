//KMF_VERSION=4.3.1
//VERSION=1.0.0-SNAPSHOT
class org.kevoree.modeling.test.Collection {
    @contained
    content : org.kevoree.modeling.test.Element[0,*]
}


class org.kevoree.modeling.test.Element {
    @id
    id : String
    value : String
    @contained
    children : org.kevoree.modeling.test.Element[0,*]
}
