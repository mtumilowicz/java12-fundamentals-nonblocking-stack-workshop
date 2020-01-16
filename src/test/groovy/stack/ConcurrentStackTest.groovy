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
        and:
        CountDownLatch latch = new CountDownLatch(5)
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> cas.push('a') }
            latch.countDown()
        }
        ExecutorService es = Executors.newCachedThreadPool()

        when: 'run concurrently'
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        es.submit(task)
        and:
        latch.await()
        es.shutdownNow()
        and: 'drain the stack'
        int i = 0
        while (cas.pop() == 'a') {
            i++
        }
        then: 'exactly 5000 elements'
        i == 5000
        and: 'stack is empty again'
        !cas.pop()
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
