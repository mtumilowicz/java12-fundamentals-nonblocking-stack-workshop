package workshop;

class NotThreadSafeCounterWorkshop {
    private int value = 0;

    int getValue() {
        return value;
    }

    int increment() {
        return ++value;
    }
}
