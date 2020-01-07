package cas

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class CasCounterTest extends Specification {

    def "Increment v2"() {
        given:
        X cas = new X()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> cas.increment() }
            latch.countDown()
        }
        ExecutorService es = Executors.newFixedThreadPool(5)

        when:
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.shutdownNow()
        and:
        latch.await()

        then:
        cas.getValue() != 5000
    }

    def "Increment"() {
        given:
        CasCounter cas = new CasCounter()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> cas.increment() }
            latch.countDown()
        }
        ExecutorService es = Executors.newFixedThreadPool(5)

        when:
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.shutdownNow()
        and:
        latch.await()

        then:
        cas.getValue() == 5000
    }
}
