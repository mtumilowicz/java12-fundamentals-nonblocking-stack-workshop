[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# java12-fundamentals-nonblocking-stack-workshop

* Reference
    * https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
    * [WJUG #136 - java.util.concurrent.atomic - Tomasz Nurkiewicz](https://www.youtube.com/watch?v=5qjFq0Pj5MU)
    * https://howtodoinjava.com/java/multi-threading/compare-and-swap-cas-algorithm/

# preface
* exclusive locking (`synchronized` keyword) is a pessimistic technique — it 
assumes the worst and doesn’t proceed until you can guarantee, that other
threads will not interfere
* there is an alternate approach — the optimistic approach, whereby you proceed 
with an update, hopeful that you can complete it without interference
    * it relies on collision detection to determine if there has been interference 
    from other parties during the update, in which case the operation fails and 
    can be retried (or not)
    * "it is easier to obtain forgiveness than permission”
* nearly every modern processor has some form of atomic read-modify-write instruction, 
    * compare-and-swap
    * load-linked/store-conditional
* CAS has three operands
    * MEM: a memory location on which to operate 
    * OLD: the expected old value, 
    * NEW: the new value 
    * CAS atomically updates MEM to NEW, but only if the value in MEM matches OLD
        * otherwise it does nothing
        * the variant called compare-and-set instead returns whether the operation
          succeeded
* CAS means "I think MEM should have the value OLD; if it does, put NEW
there, otherwise don’t change it but tell me I was wrong." 
* CAS is an optimistic technique — it proceeds with the update in the hope of 
success, and can detect failure if another thread has updated the variable since it 
was last examined

* when multiple threads attempt to update the same variable simultaneously
using CAS, one wins and updates the variable’s value, and the rest lose. 
* but the losers are not punished by suspension, as they could be if they failed to
acquire a lock; 
    * instead, they are told that they didn’t win the race this time but
can try again. 
* Because a thread that loses a CAS is not blocked, it can decide
whether it wants to try again, take some other recovery action, or do nothing.
    * Doing nothing may be a perfectly sensible response to a failed CAS; in some 
    nonblocking algorithms a failed CAS means that someone else already did the 
    work you were planning to do.

* The typical pattern for using CAS is 
    1. first to read the value A from V, 
    1. derive the new value B from A, 
    1. and then use CAS to atomically change V from A to B 
        * so long as no other thread has changed V to another value in the meantime

# summary
* The language syntax for locking may be compact, but the work done by the
JVM and OS to manage locks is not. 
    * Locking entails traversing a relatively complicated code path in the 
    JVM and may entail OS-level locking, thread suspension, and context switches. 
    * In the best case, locking requires at least one CAS, so using locks moves the 
    CAS out of sight but doesn’t save any actual execution cost. 
    * On the other hand, executing a CAS from within the program involves no JVM code,
    system calls, or scheduling activity
* The primary disadvantage of CAS is that it forces the caller to
deal with contention (by retrying, backing off, or giving up), 
    * whereas locks deal with contention automatically by blocking until the lock is available
* In Java 5.0, low-level support was added to expose CAS operations on int , long ,
and object references, and the JVM compiles these into the most efficient means
provided by the underlying hardware. 
    * On platforms supporting CAS, the runtime inlines them into the appropriate machine 
    instruction(s); 
    * in the worst case, if a CAS-like instruction is not available the JVM uses a spin lock.

* Java's traditional synchronization mechanism (`synchronized` keyword) 
impacts hardware utilization and scalability:
    * Multiple threads constantly competing for a lock = 
    frequent context switching (can take many processor cycles). 
    * When a thread holding a lock is delayed (e.g., because of a scheduling 
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