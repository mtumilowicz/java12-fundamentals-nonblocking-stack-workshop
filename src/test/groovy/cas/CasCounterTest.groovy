package cas

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class CasCounterTest extends Specification {

    def "increment"() {
        given:
        CasCounter cas = new CasCounter()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable increment1000x = {
            IntStream.range(0, 1000).forEach { ignore -> cas.increment() }
            latch.countDown()
        }
        ExecutorService es = Executors.newFixedThreadPool(5)

        when:
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        and:
        latch.await()
        es.shutdownNow()

        then:
        cas.getValue() == 5000
    }
}
