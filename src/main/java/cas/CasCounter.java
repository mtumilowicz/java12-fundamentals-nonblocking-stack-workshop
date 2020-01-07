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

    public static void main(String[] args) throws InterruptedException {
        CasCounter cas = new CasCounter();
        CountDownLatch latch = new CountDownLatch(5);
        Runnable task = () -> {
            IntStream.range(0, 1000).forEach(ignore -> cas.increment());
            latch.countDown();
        };
        ExecutorService es = Executors.newFixedThreadPool(5);
        es.submit(task);
        es.submit(task);
        es.submit(task);
        es.submit(task);
        es.submit(task);
        es.shutdownNow();

        latch.await();

        System.out.println(cas.getValue());
    }
}