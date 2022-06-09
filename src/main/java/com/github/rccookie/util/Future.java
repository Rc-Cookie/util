package com.github.rccookie.util;

import java.util.function.Consumer;

import org.jetbrains.annotations.Blocking;

/**
 * Describes a value that will be computed at some time in the
 * future.
 *
 * @param <V> The content type
 */
public interface Future<V> {

    /**
     * Attempts to cancel execution of this task. This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when {@code cancel} is called,
     * this task should never run.
     *
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}. Subsequent calls to {@link #isCanceled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    boolean cancel();

    /**
     * Returns {@code true} if this task was cancelled before it completed
     * normally.
     *
     * @return {@code true} if this task was cancelled before it completed
     */
    boolean isCanceled();

    /**
     * Returns {@code true} if this task completed.
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
    boolean isDone();

    /**
     * Returns the result.
     *
     * @return the computed result
     * @throws IllegalStateException If the computation is not done yet or
     *                               has been canceled
     */
    V get() throws IllegalStateException;

    /**
     * Waits until the result is computed and returns it. If the result
     * is already computed it returns the result immediately.
     *
     * @return The computed result.
     * @throws IllegalStateException If the computation has been canceled
     * @throws UnsupportedOperationException If this operation is not supported
     *                                       by this future
     */
    @Blocking
    V waitFor() throws IllegalStateException, UnsupportedOperationException;

    /**
     * Sets the action to be executed when the result is received.
     *
     * @param action The action to perform
     * @return This future itself
     */
    Future<V> then(Consumer<? super V> action);

    /**
     * Sets the action to be executed when the result is received.
     *
     * @param action The action to perform
     * @return This future itself
     */
    default Future<V> then(Runnable action) {
        return then($ -> action.run());
    }

    /**
     * Sets the action to be executed when the result gets cancelled.
     * As parameter the cause of cancellation will be passed, or {@code null}
     * if the computation was cancelled using the {@link #cancel()} method.
     *
     * @param handler The action to perform
     * @return This future
     */
    Future<V> onCancel(Consumer<Exception> handler);

    /**
     * Sets the action to be executed when the result gets cancelled.
     *
     * @param handler The action to perform
     * @return This future
     */
    default Future<V> onCancel(Runnable handler) {
        return onCancel($ -> handler.run());
    }
}
