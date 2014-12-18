package p;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

interface StateActor {

    void takeAction(StateContext context, char state);

}

class EnterHexValue implements StateActor {

    public static final EnterHexValue ENTER_HEX = new EnterHexValue();

    @Override
    public void takeAction(StateContext context, char c) {
        context.specialState = context.currentState;
    }
}

class TakePairActor implements StateActor {

    public static final TakePairActor TAKE_PAIR = new TakePairActor();

    @Override
    public void takeAction(StateContext context, char c) {
        context.takePair();
    }
}

class TakeKey implements StateActor {
    public static final TakeKey TAKE_KEY = new TakeKey();
    
    @Override
    public void takeAction(StateContext context, char c) {
        context.takeKey();
    }
}

class AddToken implements StateActor {

    public static final AddToken ADD_CHAR = new AddToken();

    @Override
    public void takeAction(StateContext context, char c) {
        context.addToken(c);
    }
}

class AddSpace implements StateActor {

    public static final AddSpace ADD_SPACE = new AddSpace();

    @Override
    public void takeAction(StateContext context, char state) {
        context.addToken(' ');
    }
}

class AddHexValueActor implements StateActor {
    public static final AddHexValueActor ADD_HEX_DIGIT = new AddHexValueActor();
    
    @Override
    public void takeAction(StateContext context, char c) {
        if (context.special == null) {
            checkOutOfBounds(c, context);
            context.special = c;
        } else {
            checkOutOfBounds(c, context);
            char token = (char) ((toValue(context.special) << 4) + toValue(c));
            context.addToken(token);
            context.nextState = context.specialState;
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

class HexValueOutOfRange extends StreamInvalidException {

    public HexValueOutOfRange(Integer index, Character c) {
        super(index, c);
    }
}
