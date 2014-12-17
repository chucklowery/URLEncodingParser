package p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class StateContext {

    public StateContext() {
        currentState = State.KEY;
    }

    State currentState;
    HashMap<String, List<String>> pairs = new HashMap<>();
    CharBuffer keyBuilder = new CharBuffer();
    CharBuffer valueBuilder = new CharBuffer();
    Character special = null;
    int position = 0;

    void addToKeyToken(char value) {
        keyBuilder.append(value);
    }

    void addToValueToken(char value) {
        valueBuilder.append(value);
    }

    void takePair() {
        if (keyBuilder.length() == 0 && valueBuilder.length() == 0) {
            return;
        }
        getValues(toValue(keyBuilder)).add(toValue(valueBuilder));
        valueBuilder.reset();
        keyBuilder.reset();
    }

    private String toValue(CharBuffer builder) {
        if (builder.length() == 0) {
            return null;
        } else {
            return builder.toString();
        }
    }

    List<String> getValues(String key) {
        List<String> values = pairs.get(key);
        if (values == null) {
            values = new ArrayList<>();
            pairs.put(key, values);
        }
        return values;
    }
}
