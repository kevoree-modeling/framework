class cloud.Node {
    att name : String
    value : String
    children : cloud.Node[0,*]
    element : cloud.Element

    state : cloud.State
}

class cloud.Element {
    @id
    name : String

    value : String

    @precision(2.2)
    load : Continuous

    func trigger(param : String, loop : Int) : String
}

enum cloud.State {
OK
NOK
}

