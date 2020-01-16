package stack


import spock.lang.Specification

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class ConcurrentStackTest extends Specification {
    def 'push is thread safe'() {
        given:
        ConcurrentStack<String> cas = new ConcurrentStack<>()

        and: 'stack is empty'
        !cas.pop()

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'task that increments counter 1000 times'
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> cas.push('a') }
            latch.countDown()
        }

        and: 'executor service to run tasks'
        ExecutorService es = Executors.newCachedThreadPool()

        when: 'run concurrently'
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)

        and: 'wait for all tasks to end'
        latch.await()

        and: 'shutdown executor service'
        es.shutdownNow()

        and: 'count the elements by draining the stack'
        int i = 0
        while (cas.pop() != null) {
            i++
        }

        then: 'stack is empty again'
        !cas.pop()

        and: 'exactly 5000 elements'
        i == 5000
    }

    def "pop is thread safe"() {
        given:
        ConcurrentStack<Integer> cas = new ConcurrentStack<>()
        and: 'stack is empty'
        !cas.pop()
        and:
        CountDownLatch latch = new CountDownLatch(5)
        ConcurrentSkipListSet<Integer> concurrentSet = new ConcurrentSkipListSet<>()
        Runnable task2 = {
            IntStream.range(0, 1000).forEach { ignore -> concurrentSet.add(cas.pop()) }
            latch.countDown()
        }
        ExecutorService es = Executors.newCachedThreadPool()

        IntStream.range(0, 5000).forEach { cas.push(it) }

        when:
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        es.submit(task2)
        and:
        latch.await()
        es.shutdownNow()

        then:
        cas.pop() == null
        and:
        def sorted = concurrentSet.sort()
        sorted.min() == 0
        sorted.max() == 4999
    }

}
