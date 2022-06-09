package com.github.rccookie.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public abstract class ForEachListenable<T> implements Iterable<T>, AutoCloseable {

    private final Deque<T> queue = new ArrayDeque<>();
    private boolean hasIterator = false;
    private Thread iteratorThread = null;
    private boolean closed = false;

    protected void fireEvent(T data) {
        synchronized (queue) {
            queue.add(data);
            if(iteratorThread != null)
                iteratorThread.interrupt();
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        synchronized (this) {
            if(hasIterator)
                throw new IllegalStateException("A ForEachListenable can only have a single listener iterator");
            hasIterator = true;
        }

        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                synchronized (queue) {
                    iteratorThread = Thread.currentThread();
                    return !queue.isEmpty() || !closed;
                }
            }

            @Override
            public T next() {
                synchronized (queue) {
                    iteratorThread = Thread.currentThread();
                    if(!queue.isEmpty()) {
                        Thread.interrupted(); // Clear interrupt flag for next cycle
                        return queue.removeFirst();
                    }
                }
                try { Thread.currentThread().join(); }
                catch (InterruptedException ignored) {}

                synchronized (queue) {
                    return queue.removeFirst();
                }
            }
        };
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void close() throws Exception {
        if(closed) return;
        T data = getCloseEvent();
        boolean force = forceCloseEvent();
        synchronized (queue) {
            if(closed) return;
            closed = true;
            if(force || queue.isEmpty()) {
                queue.add(data);
                if(iteratorThread != null)
                    iteratorThread.interrupt();
            }
        }
    }

    protected abstract T getCloseEvent();

    protected abstract boolean forceCloseEvent();

    public boolean isClosed() {
        return closed;
    }
}
