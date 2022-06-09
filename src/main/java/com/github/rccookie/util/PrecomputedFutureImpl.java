package com.github.rccookie.util;

import org.jetbrains.annotations.NotNull;

/**
 * A future implementation where the result is already present at computation
 * start time.
 *
 * @param <V> Content type of the future
 */
public class PrecomputedFutureImpl<V> extends AbstractFutureImpl<V> {

    /**
     * Returns a new successful future implementation with the given result
     * value.
     *
     * @param result The result for the future
     */
    public PrecomputedFutureImpl(V result) {
        complete(result);
    }

    /**
     * Creates a new failed future implementation with the given failure cause.
     *
     * @param failure The cause of failure
     */
    private PrecomputedFutureImpl(@NotNull Exception failure) {
        fail(failure);
    }

    @Override
    public V waitFor() throws IllegalStateException {
        if(canceled)
            throw new IllegalStateException("The computation has been cancelled");
        return value;
    }

    /**
     * Creates a new failed future implementation with the given failure cause.
     *
     * @param cause The cause of failure
     */
    public static <V> PrecomputedFutureImpl<V> failed(@NotNull Exception cause) {
        return new PrecomputedFutureImpl<>(cause);
    }
}
