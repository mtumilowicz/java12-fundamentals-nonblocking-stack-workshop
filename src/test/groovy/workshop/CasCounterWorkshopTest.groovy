package workshop

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class CasCounterWorkshopTest extends Specification {

    def 'increment is thread safe'() {
        given:
        CasCounterWorkshop counter = new CasCounterWorkshop()

        and: 'counter is 0'
        counter.getValue() == 0

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'task that increments counter 1000 times'
        Runnable increment1000x = {
            IntStream.range(0, 1000).forEach { ignore -> counter.increment() }
            latch.countDown()
        }

        and: 'executor service to run tasks'
        ExecutorService es = Executors.newCachedThreadPool()

        when: 'run concurrently'
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)
        es.submit(increment1000x)

        and: 'wait for all tasks to end'
        latch.await()

        and: 'shutdown executor service'
        es.shutdownNow()

        then: '5 x 1000 = 5000'
        counter.getValue() == 5000
    }
}
