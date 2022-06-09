package com.github.rccookie.util;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract definition of a future implementation that can be set to a value.
 *
 * @param <V> The content type
 */
public interface FutureImpl<V> extends Future<V> {

    /**
     * Sets the result of the future.
     *
     * @param value The result to set
     * @throws IllegalStateException If the result is already set or
     *                               the future has been cancelled
     */
    void complete(V value) throws IllegalStateException;

    /**
     * Cancels this future because of a computation exception.
     *
     * @param cause The reason why the computation was cancelled
     *              unexpectedly
     * @throws IllegalStateException If the result is already set
     * @return {@code false} if the computation was already canceled
     */
    boolean fail(@NotNull Exception cause) throws IllegalStateException;
}
