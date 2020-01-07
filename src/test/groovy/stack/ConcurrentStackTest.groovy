package stack


import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class ConcurrentStackTest extends Specification {
    def "Increment"() {
        given:
        ConcurrentStack<String> cas = new ConcurrentStack<>()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> cas.push("a") }
            latch.countDown()
        }

        ExecutorService es = Executors.newFixedThreadPool(5)

        when:
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        latch.await()
        es.shutdownNow()

        int i = 0
        while (cas.pop() != null) {
            i++
        }
        then:
        i == 5000
    }

    def "Increment v2"() {
        given:
        ConcurrentStack<Integer> cas = new ConcurrentStack<>()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable task2 = {
            IntStream.range(0, 1000).forEach { ignore -> cas.pop() }
            latch.countDown()
        }
        ExecutorService es = Executors.newFixedThreadPool(5)

        IntStream.range(0, 5001).forEach { cas.push(it) }

        when:
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        latch.await()
        es.shutdownNow()

        then:
        cas.pop() == 0
    }

}
