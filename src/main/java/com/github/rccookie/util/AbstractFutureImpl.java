package com.github.rccookie.util;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * Generic implementation of {@link Future} without specifying the
 * works of {@link #waitFor()}.
 *
 * @param <V> Content type
 */
abstract class AbstractFutureImpl<V> implements FutureImpl<V> {

    /**
     * Whether the computation has been cancelled.
     */
    protected boolean canceled = false;
    /**
     * Whether the computation has ended, either successful
     * or unsuccessful.
     */
    protected boolean done = false;
    /**
     * The result value.
     */
    protected V value = null;
    /**
     * The argument {@link #fail(Exception)} was called with.
     */
    protected Exception failCause;
    /**
     * The function to be called when the result is available.
     */
    protected Consumer<? super V> then = null;
    /**
     * The function to be called when the result gets cancelled.
     */
    protected Consumer<Exception> onCancel = null;


    @Override
    public boolean cancel() {
        boolean out = !canceled && !done;
        canceled = done = true;
        failCause = null;
        if(out && onCancel != null) onCancel.accept(null);
        return out;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public V get() throws IllegalStateException {
        if(!done) throw new IllegalStateException("Result is not yet computed");
        if(canceled) throw new IllegalStateException("Execution has been canceled", failCause);
        return value;
    }

    @Override
    public Future<V> then(Consumer<? super V> action) {
        then = action;
        if(action != null && done && !canceled)
            action.accept(value);
        return this;
    }

    @Override
    public Future<V> onCancel(Consumer<Exception> handler) {
        onCancel = handler;
        if(onCancel != null && canceled)
            handler.accept(failCause);
        return this;
    }

    @Override
    public void complete(V value) throws IllegalStateException {
        if(isDone()) throw new IllegalStateException("The value cannot be set because the computation is already done");
        this.value = value;
        done = true;
        if(then != null)
            then.accept(value);
    }

    @Override
    public boolean fail(@NotNull Exception cause) throws IllegalStateException {
        if(canceled) return false;
        if(done) throw new IllegalStateException("Result already computed");
        canceled = done = true;
        failCause = cause;
        if(onCancel != null) onCancel.accept(cause);
        return true;
    }
}
