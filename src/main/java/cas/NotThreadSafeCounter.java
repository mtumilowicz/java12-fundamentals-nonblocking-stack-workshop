package cas;

public class NotThreadSafeCounter {
    private int value = 0;

    int getValue() {
        return value;
    }

    int increment() {
        value = value + 1;
        return value;
    }
}
