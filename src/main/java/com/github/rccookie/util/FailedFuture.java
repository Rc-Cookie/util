package com.github.rccookie.util;

import java.util.function.Consumer;
import java.util.function.Function;

class FailedFuture<V> implements Future<V> {

    private final Exception cause;

    FailedFuture(Exception cause) {
        this.cause = cause;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public V get() throws IllegalStateException {
        throw new IllegalStateException("Computation failed");
    }

    @Override
    public V waitFor() throws IllegalStateException, UnsupportedOperationException {
        return get();
    }

    @Override
    public Future<V> then(Consumer<? super V> action) {
        return this;
    }

    @Override
    public Future<V> except(Consumer<? super Exception> handler) {
        handler.accept(cause);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Future<T> map(Function<? super V, ? extends T> mapper) {
        return (Future<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Future<T> flatMap(Function<? super V, ? extends Future<T>> mapper) {
        return (Future<T>) this;
    }
}
