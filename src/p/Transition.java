package p;

class Transition {

    State nextState;
    StateActor actor;

    Transition(State nextState, StateActor action) {
        this.nextState = nextState;
        this.actor = action;
    }

    void transition(StateContext context) {
        actor.takeAction(context, context.token);
        context.currentState = nextState;
    }
}
