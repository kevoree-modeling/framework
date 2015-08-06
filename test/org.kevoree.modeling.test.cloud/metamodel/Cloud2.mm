with kmfVersion "4.19.1-SNAPSHOT"
with version "1.0.0-SNAPSHOT"
class cloud.Cloud {
    ref* nodes : cloud.Node
}
class cloud.Node {
    att name : String
    ref* softwares : cloud.Software
}
class cloud.Software {
    att name : String
    att size : Int
}
