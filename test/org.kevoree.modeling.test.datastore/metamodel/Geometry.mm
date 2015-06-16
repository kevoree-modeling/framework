class geometry.Library {
    @contained
    shapes : geometry.Shape[0,*]

    func addShape(shapeName : String)
}

class geometry.Shape {
    @id name : String
    color : String
}
