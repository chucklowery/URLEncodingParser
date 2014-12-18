package p;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static p.AddHexValueActor.ADD_HEX_DIGIT;
import static p.AddToken.ADD_CHAR;
import static p.BadStreamActor.BAD_STREAM;
import static p.Event.AMPERSAND;
import static p.Event.CHAR;
import static p.Event.EQUAL;
import static p.Event.PERCENT;
import static p.Event.PLUS;
import static p.Event.toEvent;
import static p.AddSpace.ADD_SPACE;
import static p.EnterHexValue.ENTER_HEX;
import static p.State.KEY;
import static p.State.HEX_VALUE;
import static p.State.VALUE;
import static p.TakeKey.TAKE_KEY;
import static p.TakePairActor.TAKE_PAIR;

/**
 * Thread safe URL encoded key=value parser.
 *
 * @author Charles H. Lowery <chuck.lowery @ gmail.com>
 */
public class URLEncodedParser {

    private static final Transition[][] transitions;

    static {
        transitions = new Transition[State.values().length][Event.values().length];

        transition(KEY, CHAR, KEY, ADD_CHAR);
        transition(KEY, AMPERSAND, KEY, TAKE_PAIR);
        transition(KEY, EQUAL, VALUE, TAKE_KEY);
        transition(KEY, PERCENT, HEX_VALUE, ENTER_HEX);
        transition(KEY, PLUS, KEY, ADD_SPACE);

        transition(VALUE, CHAR, VALUE, ADD_CHAR);
        transition(VALUE, EQUAL, VALUE, ADD_CHAR);
        transition(VALUE, AMPERSAND, KEY, TAKE_PAIR);
        transition(VALUE, PERCENT, HEX_VALUE, ENTER_HEX);
        transition(VALUE, PLUS, VALUE, ADD_SPACE);

        transition(HEX_VALUE, CHAR, HEX_VALUE, ADD_HEX_DIGIT);
        transition(HEX_VALUE, PERCENT, HEX_VALUE, BAD_STREAM);
        transition(HEX_VALUE, AMPERSAND, HEX_VALUE, BAD_STREAM);
        transition(HEX_VALUE, EQUAL, HEX_VALUE, BAD_STREAM);
        transition(HEX_VALUE, PLUS, HEX_VALUE, BAD_STREAM);
    }

    public Map<String, List<String>> parse(InputStream rawStream, int length) {
        final InputStreamReader stream = new InputStreamReader(rawStream, StandardCharsets.UTF_8);
        StateContext context = new StateContext();
        int rawTokem;
        for (context.position = 0; context.position < length && (rawTokem = read(stream)) > -1; context.position++) {
            char token = (char) rawTokem;
            
            transitions[context.currentState.ordinal()][toEvent(token).ordinal()].transition(context, token);
            context.currentState = context.nextState;
        }
        context.takePair();
        return context.pairs;
    }

    private int read(InputStreamReader stream) {
        try {
            return stream.read();
        } catch (IOException ex) {
            throw new UnexpectEndOfStream();
        }
    }

    private static void transition(State given, Event when, State then, StateActor action) {
        transitions[given.ordinal()][when.ordinal()] = new Transition(then, action);
    }
}

class UnexpectEndOfStream extends RuntimeException {

    public UnexpectEndOfStream() {
        super();
    }
}
