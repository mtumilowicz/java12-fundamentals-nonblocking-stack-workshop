package workshop;

import cas.SimulatedCAS;

class CasCounterWorkshop {
    private final SimulatedCAS value = new SimulatedCAS();

    public int getValue() {
        return value.get();
    }

    public int increment() {
        // compareAndSet(old, old + 1)
        return 0;
    }
}