package com.github.rccookie.util;

import org.jetbrains.annotations.Blocking;

/**
 * A Future that has two ways of being computed; first, in an asynchronous way, and
 * secondly, in a blocking way. If the result is requested using {@link #waitFor()}
 * before the async computation is done, it will be cancelled and instead computed
 * using the other strategy and blocking the requesting thread.
 * <p>This future is not thread-safe for read-access of {@link #waitFor()}.</p>
 *
 * @param <V> The content type
 */
public abstract class DualComputationFutureImpl<V> extends AbstractFutureImpl<V> {

    @Override
    public V waitFor() throws IllegalStateException, UnsupportedOperationException {
        if(done) return get();
        cancelNonBlocking();
        try {
            complete(computeBlocking());
            return value;
        } catch(Exception e) {
            fail(e);
            throw new IllegalStateException("Computation has been cancelled", e);
        }
    }

    /**
     * Cancels the non-blocking computation attempt.
     */
    public abstract void cancelNonBlocking();

    /**
     * Computes the result, blocking until done.
     *
     * @return The result
     * @throws Exception If an exception occurred. This will cause the
     *                   future to fail with that exception as cause
     */
    @Blocking
    public abstract V computeBlocking() throws Exception;
}
