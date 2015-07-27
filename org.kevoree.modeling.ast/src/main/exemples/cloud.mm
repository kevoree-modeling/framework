version "3.0"
kmfVersion "titi"

enum mycloud.NodeState {
    OK
    NOK
}

class mycloud.Cloud {
    att hello : String
att hello2 : String precision 0.4
ref myRef : mycloud.Cloud
ref* myRef2 : mycloud2.Cloud oppositeOf hello


dependency MyDep : mycloud.Cloud
input input0 "myExtractorQuery"
output out0 : String
}