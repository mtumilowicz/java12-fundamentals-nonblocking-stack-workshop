package stack

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.IntStream

class NotThreadSafeStackTest extends Specification {

    ArrayDeque<Integer> stack = new ArrayDeque<>()

    def 'push is NOT thread safe'() {
        given: 'stack is empty'
        stack.size() == 0

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'task that increments counter 1000 times'
        Runnable task = {
            try {
                IntStream.range(0, 1000).forEach { ignore -> stack.push(1) }
            } finally {
                latch.countDown()
            }
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

        then: 'unfortunately implementation was not thread safe'
        stack.size() != 5000
    }

    def 'poll is NOT thread safe'() {
        given: 'stack is empty'
        stack.size() == 0

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(5)

        and: 'task that polls 1000 elements from the stack'
        Runnable task = {
            try {
                IntStream.range(0, 1000).forEach { ignore -> stack.poll() }
            } finally {
                latch.countDown()
            }
        }

        and: 'executor service to run tasks'
        ExecutorService es = Executors.newCachedThreadPool()

        and: 'prepare stack for draining by adding 5000 numbers to it'
        IntStream.range(0, 5000).forEach { stack.push(it) }

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

        then: 'unfortunately implementation is NOT thread safe'
        stack.size() != 0
    }
}
