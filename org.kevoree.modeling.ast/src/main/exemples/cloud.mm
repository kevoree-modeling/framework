with version "3.0", kmfVersion "titi"

enum mycloud.NodeState {
    OK
    NOK
}

class mycloud.Cloud {
    att hello : String
att hello2 : String with precision 0.4, index, oclConstraints "titi < 35"
ref myRef : mycloud.Cloud
ref* myRef2 : mycloud2.Cloud with oppositeOf hello


dependency MyDep : mycloud.Cloud
input input0 "myExtractorQuery"
output out0 : String
}