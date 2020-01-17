package workshop;

import java.util.concurrent.atomic.AtomicReference;

class ConcurrentStackWorkshop<E> {
    AtomicReference<Node<E>> top = new AtomicReference<>();

    public void push(E item) {
        // newHead.next = oldHead
        // compareAndSet(old, new)
    }

    public E pop() {
        // if empty - null
        // newHead = oldHead.next
        // compareAndSet
        return null;
    }

    private static class Node<E> {
        public final E item;
        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }
}