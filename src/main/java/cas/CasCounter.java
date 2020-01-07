package cas;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public class CasCounter {
    private final SimulatedCAS value = new SimulatedCAS();

    public int getValue() {
        return value.get();
    }

    public int increment() {
        int v;
        do {
            v = value.get();
        }
        while (!value.compareAndSet(v, v + 1));
        return v + 1;
    }
}