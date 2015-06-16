class cloud.Node {
    @id
    name : String
    value : String
    children : cloud.Node[0,*]
    element : cloud.Element
}

class cloud.Element {
    @id
    name : String

    value : String

    @precision(2.2)
    load : Continuous

    func trigger(param : String, loop : Int) : String
}

