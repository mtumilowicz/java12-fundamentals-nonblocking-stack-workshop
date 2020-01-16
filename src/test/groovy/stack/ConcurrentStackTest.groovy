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
        ConcurrentStack<String> stack = new ConcurrentStack<>()

        and: 'stack is empty'
        !stack.pop()

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'task that increments counter 1000 times'
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> stack.push('a') }
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
        while (stack.pop() != null) {
            i++
        }

        then: 'stack is empty again'
        !stack.pop()

        and: 'exactly 5000 elements'
        i == 5000
    }

    def 'pop is thread safe'() {
        given:
        ConcurrentStack<Integer> cas = new ConcurrentStack<>()

        and: 'stack is empty'
        !cas.pop()

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'concurrentSet for persisting stack elements'
        ConcurrentSkipListSet<Integer> concurrentSet = new ConcurrentSkipListSet<>()

        and: 'task that pops 1000 elements from the stack and push to the concurrentSet'
        Runnable task = {
            IntStream.range(0, 1000).forEach { ignore -> concurrentSet.add(cas.pop()) }
            latch.countDown()
        }

        and: 'executor service to run tasks'
        ExecutorService es = Executors.newCachedThreadPool()

        and: 'prepare stack for draining by adding 5000 numbers to it'
        IntStream.range(0, 5000).forEach { cas.push(it) }

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

        then: 'stack is empty again'
        cas.pop() == null

        and: 'concurrentSet has all the elements from the stack'
        def sorted = concurrentSet.sort()
        sorted.removeAll(0..4999)
        sorted.size() == 0
    }

}
