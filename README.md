# java12-fundamentals-nonblocking-stack-workshop

* Reference
    * https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
    * [WJUG #136 - java.util.concurrent.atomic - Tomasz Nurkiewicz](https://www.youtube.com/watch?v=5qjFq0Pj5MU)

# preface
The compare-and-swap (CAS) instruction is an uninterruptible instruction 
that reads a memory location, compares the read value with an expected 
value, and stores a new value in the memory location when the read value 
matches the expected value. Otherwise, nothing is done. The actual 
microprocessor instruction may differ somewhat (e.g., return true if 
CAS succeeded or false otherwise instead of the read value).

1. Read value v from address X.
1. Perform a multistep computation to derive a new value v2.
1. Use CAS to change the value of X from v to v2. CAS succeeds 
when X's value hasn't changed while performing these steps.

**CAS offers better performance (and scalability) over synchronization.**

Java's traditional synchronization mechanism (`synchronized` keyword) 
impacts hardware utilization and scalability:
1. Multiple threads constantly competing for a lock = 
frequent context switching (can take many processor cycles). 
1. When a thread holding a lock is delayed (e.g., because of a scheduling 
delay), no thread that requires that lock makes any progress.

## digression
note that before java 8, to perform some function on atomic value (for example - double it) 
we have to use `compareAndSet` in an do-while loop
```
int prev;

do {
    prev = atom.get();
} while (!atom.compareAndSet(prev, prev * 2));
```
now, we have dedicated methods in atomic classes: `updateAndGet` and `accumulateAndGet`