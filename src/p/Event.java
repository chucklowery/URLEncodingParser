package p;

enum Event {

    CHAR,
    EQUAL,
    AMPERSAND,
    PERCENT;

    public static Event toEvent(char c) {
        switch (c) {
            case '%':
                return PERCENT;
            case '=':
                return EQUAL;
            case '&':
                return AMPERSAND;
            default:
                return CHAR;
        }
    }
}
