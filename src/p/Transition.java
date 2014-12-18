package p;

class Transition {

    State nextState;
    StateActor actor;

    Transition(State nextState, StateActor action) {
        this.nextState = nextState;
        this.actor = action;
    }

    void transition(StateContext context, char value) {
        actor.takeAction(context, value);
        context.currentState = nextState;
    }
}
