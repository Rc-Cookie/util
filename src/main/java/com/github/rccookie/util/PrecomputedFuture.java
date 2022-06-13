package com.github.rccookie.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A future implementation where the result is already present at computation
 * start time.
 *
 * @param <V> Content type of the future
 */
class PrecomputedFuture<V> implements Future<V> {

    private final V result;

    /**
     * Returns a new successful future implementation with the given result
     * value.
     *
     * @param result The result for the future
     */
    PrecomputedFuture(V result) {
        this.result = result;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public V get() throws IllegalStateException {
        return result;
    }

    @Override
    public V waitFor() throws IllegalStateException {
        return result;
    }

    @Override
    public Future<V> then(Consumer<? super V> action) {
        action.accept(result);
        return this;
    }

    @Override
    public Future<V> except(Consumer<? super Exception> handler) {
        return this;
    }

    @Override
    public <T> Future<T> map(Function<? super V, ? extends T> mapper) {
        return new PrecomputedFuture<>(mapper.apply(result));
    }

    @Override
    public <T> Future<T> flatMap(Function<? super V, ? extends Future<T>> mapper) {
        return mapper.apply(result);
    }
}
