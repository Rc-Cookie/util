package com.github.rccookie.util;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * General-purpose implementation of {@link Future}.
 *
 * @param <V> The content type
 */
public class ThreadedFutureImpl<V> extends AbstractFutureImpl<V> {

    /**
     * Threads that are waiting for the result of the computation;
     */
    private Set<Thread> waiting = new HashSet<>();

    /**
     * Sets the result of the future.
     *
     * @param value The result to set
     * @throws IllegalStateException If the result is already set or
     *                               the future has been cancelled
     */
    @Override
    public void complete(V value) throws IllegalStateException {
        super.complete(value);
        onDone();
    }

    @Override
    public boolean fail(@NotNull Exception cause) throws IllegalStateException {
        if(!super.fail(cause)) return false;
        onDone();
        return true;
    }

    @Override
    public boolean cancel() {
        boolean out = super.cancel();
        onDone();
        return out;
    }

    private void onDone() {
        for(Thread t : waiting)
            t.interrupt();
        waiting = null; // Free up memory
    }

    @Override
    public V waitFor() throws UnsupportedOperationException {
        while(!isDone()) { // use while in case thread gets interrupted from elsewhere
            synchronized (this) { waiting.add(Thread.currentThread()); }
            try { Thread.currentThread().join(); }
            catch(InterruptedException ignored) { }
        }
        return get();
    }
}
