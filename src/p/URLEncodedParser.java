package p;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static p.BadStreamActor.BAD_STREAM;
import static p.Event.AMPERSAND;
import static p.Event.CHAR;
import static p.Event.EQUAL;
import static p.Event.PERCENT;
import static p.Event.PLUS;
import static p.Event.toEvent;
import static p.NoOp.NO_OP;
import static p.ReplacePlusWithSpace.ADD_SPACE_TO_KEY;
import static p.ReplacePlusWithSpace.ADD_SPACE_TO_VALUE;
import static p.SetKey.ADD_TO_KEY;
import static p.SetValue.ADD_TO_VALUE;
import static p.SpecialDigitActor.USE_SPECIAL_KEY;
import static p.SpecialDigitActor.USE_SPECIAL_VALUE;
import static p.State.KEY;
import static p.State.SPECIAL_KEY;
import static p.State.SPECIAL_VALUE;
import static p.State.VALUE;
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

        transition(KEY, CHAR, KEY, ADD_TO_KEY);
        transition(KEY, AMPERSAND, KEY, TAKE_PAIR);
        transition(KEY, EQUAL, VALUE, NO_OP);
        transition(KEY, PERCENT, SPECIAL_KEY, NO_OP);
        transition(KEY, PLUS, KEY, ADD_SPACE_TO_KEY);

        transition(VALUE, CHAR, VALUE, ADD_TO_VALUE);
        transition(VALUE, EQUAL, VALUE, ADD_TO_VALUE);
        transition(VALUE, AMPERSAND, KEY, TAKE_PAIR);
        transition(VALUE, PERCENT, SPECIAL_VALUE, NO_OP);
        transition(VALUE, PLUS, VALUE, ADD_SPACE_TO_VALUE);

        transition(SPECIAL_KEY, CHAR, SPECIAL_KEY, USE_SPECIAL_KEY);
        transition(SPECIAL_KEY, PERCENT, SPECIAL_KEY, BAD_STREAM);
        transition(SPECIAL_KEY, AMPERSAND, SPECIAL_KEY, BAD_STREAM);
        transition(SPECIAL_KEY, EQUAL, SPECIAL_KEY, BAD_STREAM);
        transition(SPECIAL_KEY, PLUS, SPECIAL_KEY, BAD_STREAM);

        transition(SPECIAL_VALUE, CHAR, SPECIAL_VALUE, USE_SPECIAL_VALUE);
        transition(SPECIAL_VALUE, PERCENT, SPECIAL_VALUE, BAD_STREAM);
        transition(SPECIAL_VALUE, AMPERSAND, SPECIAL_VALUE, BAD_STREAM);
        transition(SPECIAL_VALUE, EQUAL, SPECIAL_VALUE, BAD_STREAM);
        transition(SPECIAL_VALUE, PLUS, SPECIAL_VALUE, BAD_STREAM);
    }

    public Map<String, List<String>> parse(InputStream rawStream, int length) {
        final InputStreamReader stream = new InputStreamReader(rawStream, StandardCharsets.UTF_8);
        StateContext context = new StateContext();
        int rawTokem;
        for (context.position = 0; context.position < length && (rawTokem = read(stream)) > -1; context.position++) {
            char token = (char) rawTokem;
            transitions[context.currentState.ordinal()][toEvent(token).ordinal()].transition(context, token);
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
