package p;

enum Event {

    CHAR,
    EQUAL,
    AMPERSAND,
    PERCENT,
    PLUS;

    public static Event toEvent(char c) {
        switch (c) {
            case '%':
                return PERCENT;
            case '=':
                return EQUAL;
            case '&':
                return AMPERSAND;
            case '+':
                return PLUS;
            default:
                return CHAR;
        }
    }
}
