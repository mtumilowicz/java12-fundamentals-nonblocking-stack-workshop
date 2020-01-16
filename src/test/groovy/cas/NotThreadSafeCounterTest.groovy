package cas

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class NotThreadSafeCounterTest extends Specification {

    def 'increment is not thread safe'() {
        given:
        NotThreadSafeCounter cas = new NotThreadSafeCounter()
        CountDownLatch latch = new CountDownLatch(5)
        Runnable increment1000x = {
            IntStream.range(0, 1000).forEach { ignore -> cas.increment() }
            latch.countDown()
        }
        ExecutorService es = Executors.newCachedThreadPool()

        when: 'run concurrently'
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        and:
        latch.await()
        es.shutdownNow()

        then:
        cas.getValue() != 5000
    }
}
