[![Build Status](https://travis-ci.com/mtumilowicz/java12-fundamentals-nonblocking-stack-workshop.svg?branch=master)](https://travis-ci.com/mtumilowicz/java12-fundamentals-nonblocking-stack-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# java12-fundamentals-nonblocking-stack-workshop

* Reference
    * https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
    * [WJUG #136 - java.util.concurrent.atomic - Tomasz Nurkiewicz](https://www.youtube.com/watch?v=5qjFq0Pj5MU)
    * https://howtodoinjava.com/java/multi-threading/compare-and-swap-cas-algorithm/
    * https://en.wikipedia.org/wiki/Treiber_stack
    
# preface
* the main goal of this workshops is to introduce compare-and-swap and show how it could be used in practice 
(by implementing non-blocking and concurrent stack)
* `answers` with correctly implemented `workshop` tasks are in `answers` package

# introduction
* exclusive locking (`synchronized` keyword) is a pessimistic technique
    * it asks you to first guarantee that no other thread will interfere
* the optimistic approach
    * you proceed with an update, being hopeful that you can complete it without interference
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
* The typical pattern for using CAS is 
    1. read the value OLD from MEM, 
    1. derive the new value NEW from OLD, 
    1. and then use CAS to atomically change MEM from OLD to NEW 
        * provided no other thread has changed OLD to another value in the meantime
* CAS is an optimistic technique
    * it proceeds with the update in the hope of success
    * can detect failure if another thread has updated the variable since it was last examined
* when multiple threads attempt to update the same variable simultaneously
using CAS - only one wins
    * but the losers are not punished by suspension (contrary to failure when acquiring a lock)
        * they can simply try again 
        * because a thread that loses a CAS is not blocked, it can decide
        whether it wants to try again, take some other recovery action, or do nothing
            * doing nothing may be a perfectly sensible - a failed CAS means that someone else already did the 
            work you were planning to do

# concurrent stack
* basic principle: for the algorithm is to only add something new to the stack once you know the item you are trying 
to add is the only thing that has been added since you began the operation
* compare-and-swap
* https://en.wikipedia.org/wiki/ABA_problem

# summary
* the language syntax for locking may be compact, but the work done by the
JVM and OS to manage locks is not
    * locking entails traversing a relatively complicated code path in the 
    JVM and may entail OS-level locking, thread suspension, and context switches 
    * in the best case, locking requires at least one CAS (using locks moves the 
    CAS out of sight)
* java's traditional synchronization mechanism (`synchronized` keyword) 
impacts hardware utilization and scalability:
    * multiple threads constantly competing for a lock = 
    frequent context switching (can take many processor cycles)
    * when a thread holding a lock is delayed (e.g., because of a scheduling  delay), 
    no thread that requires that lock makes any progress
* executing a CAS from within the program involves no JVM code,
system calls, or scheduling activity
* the primary disadvantage of CAS is that it forces the caller to deal with contention 
(by retrying, backing off, or giving up) 
    * locks deal with contention automatically by blocking until the lock is available
* in Java 5.0, low-level support was added to expose CAS operations on int, long,
and object references, and the JVM compiles these into the most efficient means
provided by the underlying hardware 
    * the runtime inlines them into the appropriate machine instructions 
    * if a CAS-like instruction is not available the JVM uses a spin lock

# digression
note that before java 8, to perform some function on atomic value (for example - double it) 
we have to use `compareAndSet` in an do-while loop
```
int prev;

do {
    prev = atom.get();
} while (!atom.compareAndSet(prev, prev * 2));
```
now, we have dedicated methods in atomic classes: `updateAndGet` and `accumulateAndGet`