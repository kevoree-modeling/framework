
class fsmsample.FSM  {
    @id
    name : String
    @contained
    ownedState : fsmsample.State[0,*]
    initialState : fsmsample.State
    finalState : fsmsample.State
    currentState : fsmsample.State
}

class fsmsample.State  {
    @id
    name : String
    owningFSM : fsmsample.FSM
    @contained
    outgoingTransition : fsmsample.Transition[0,*]
    incomingTransition : fsmsample.Transition[0,*]
}

class fsmsample.Transition  {
    @id
    name : String
    input : String
    output : String
    source : fsmsample.State
    target : fsmsample.State
    @contained
    action : fsmsample.Action
}

class fsmsample.Action  {
    @id
    name : String

}
