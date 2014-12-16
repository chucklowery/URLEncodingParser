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

    StringBuilder keyBuilder = new StringBuilder();
    StringBuilder valueBuilder = new StringBuilder();
    Character special = null;
    int position = 0;

    void addToKeyToken(char value) {
        keyBuilder.append(value);
    }

    void addToValueToken(char value) {
        valueBuilder.append(value);
    }

    void takePair() {
        if (keyBuilder.length() == 0) {
            return;
        }
        String value;
        if (valueBuilder.length() > 0) {
            value = valueBuilder.toString();
            valueBuilder = new StringBuilder();
        } else {
            value = null;
        }
        getValues(keyBuilder.toString()).add(value);

        keyBuilder = new StringBuilder();

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
