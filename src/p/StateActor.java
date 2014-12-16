package p;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static p.SetKey.ADD_TO_KEY;
import static p.SetValue.ADD_TO_VALUE;
import static p.State.KEY;
import static p.State.VALUE;

interface StateActor {

    void takeAction(StateContext context, char state);

}

class TakePairActor implements StateActor {

    public static final TakePairActor TAKE_PAIR = new TakePairActor();

    @Override
    public void takeAction(StateContext context, char c) {
        context.takePair();
    }
}

class NoOp implements StateActor {

    public static final NoOp NO_OP = new NoOp();

    @Override
    public void takeAction(StateContext context, char state) {
    }

}

class SetKey implements StateActor {

    public static final SetKey ADD_TO_KEY = new SetKey();

    @Override
    public void takeAction(StateContext context, char c) {
        context.addToKeyToken(c);
    }
}

class SetValue implements StateActor {

    public static final SetValue ADD_TO_VALUE = new SetValue();

    @Override
    public void takeAction(StateContext context, char c) {
        context.addToValueToken(c);
    }
}

class ReplacePlusWithSpace implements StateActor {

    public static final ReplacePlusWithSpace ADD_SPACE_TO_KEY = new ReplacePlusWithSpace(ADD_TO_KEY);
    public static final ReplacePlusWithSpace ADD_SPACE_TO_VALUE = new ReplacePlusWithSpace(ADD_TO_VALUE);
    StateActor actor;

    public ReplacePlusWithSpace(StateActor actor) {
        this.actor = actor;

    }

    @Override
    public void takeAction(StateContext context, char state) {
        actor.takeAction(context, ' ');
    }
}

class SpecialDigitActor implements StateActor {

    StateActor onValue;
    State state;

    public SpecialDigitActor(StateActor onValue, State state) {
        this.onValue = onValue;
        this.state = state;
    }

    public static final SpecialDigitActor USE_SPECIAL_KEY = new SpecialDigitActor(ADD_TO_KEY, KEY);
    public static final SpecialDigitActor USE_SPECIAL_VALUE = new SpecialDigitActor(ADD_TO_VALUE, VALUE);

    @Override
    public void takeAction(StateContext context, char c) {
        if (context.special == null) {
            checkOutOfBounds(c, context);
            context.special = c;
        } else {
            checkOutOfBounds(c, context);
            char token = (char) ((toValue(context.special) << 4) + toValue(c));
            onValue.takeAction(context, token);
            context.currentState = state;
            context.special = null;
        }
    }

    private void checkOutOfBounds(char c, StateContext context) throws HexValueOutOfRange {
        int value = toValue(c);
        if (value > 15 || value < 0) {
            throw new HexValueOutOfRange(context.position, c);
        }
    }

    static char hexToChar(char b1, char b2) {
        return (char) ((toValue(b1) << 4) + toValue(b2));
    }

    static int toValue(char c) {
        if (c >= 'a') {
            return 10 + c - 'a';
        } else if (c >= 'A') {
            return 10 + c - 'A';
        } else {
            return c - '0';
        }
    }
}

class ClearSpecialActor implements StateActor {

    public static final ClearSpecialActor CLEAR_SPECIAL = new ClearSpecialActor();

    @Override
    public void takeAction(StateContext context, char state) {
        context.special = null;
    }

}

class BadStreamActor implements StateActor {

    public static final BadStreamActor BAD_STREAM = new BadStreamActor(StreamInvalidException.class);
    Class<StreamInvalidException> message;

    public BadStreamActor(Class<StreamInvalidException> message) {
        this.message = message;
    }

    @Override
    public void takeAction(StateContext context, char state) {
        try {
            Constructor<StreamInvalidException> c = message.getConstructor(Integer.class, Character.class);
            throw c.newInstance(context.position, state);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
        }
    }
}

class StreamInvalidException extends RuntimeException {

    public StreamInvalidException(Integer index, Character c) {
        super("Unexpected token found in stream @" + index + " found:" + c);
    }
}

class UnexpectedTokenFoundInStream extends RuntimeException {

}

class HexValueOutOfRange extends StreamInvalidException {

    public HexValueOutOfRange(Integer index, Character c) {
        super(index, c);
    }
}
