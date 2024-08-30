package de.rccookie.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

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
    @Deprecated(forRemoval = true)
    default Future<V> onCancel(Consumer<Exception> handler) {
        return except(handler);
    }

    /**
     * Sets the action to be executed when the result gets cancelled.
     *
     * @param handler The action to perform
     * @return This future
     */
    @Deprecated(forRemoval = true)
    default Future<V> onCancel(Runnable handler) {
        return except(handler);
    }

    /**
     * Sets the action to be executed when the result gets cancelled.
     * As parameter the cause of cancellation will be passed, or {@code null}
     * if the computation was cancelled using the {@link #cancel()} method.
     *
     * @param handler The action to perform
     * @return This future
     */
    Future<V> except(Consumer<? super Exception> handler);

    /**
     * Sets the action to be executed when the result gets cancelled.
     *
     * @param handler The action to perform
     * @return This future
     */
    default Future<V> except(Runnable handler) {
        return except($ -> handler.run());
    }

    /**
     * Returns a new future that applies returns the output of the given
     * mapping function applied to the output of this future.
     *
     * @param mapper The mapping function to apply
     * @param <T> The type to be mapped to
     * @return The new future
     */
    <T> Future<T> map(Function<? super V, ? extends T> mapper);

    /**
     * Returns a new future that waits for this future to compute and then
     * returns the output of the given generator.
     *
     * @param generator The supplier for the result
     * @param <T> The type to be mapped to
     * @return The new future
     */
    default <T> Future<T> map(Supplier<? extends T> generator) {
        return map($ -> generator.get());
    }

    /**
     * Returns a new future that represents the computation of this future, then
     * the application of the given mapping function, and finally the computation
     * of the future returned by the mapping function.
     *
     * @param mapper The mapping function to generate the second computation part
     * @param <T> The content type of the future returned by the mapping function
     * @return The new future
     */
    <T> Future<T> flatMap(Function<? super V, ? extends Future<T>> mapper);

    /**
     * Returns a new future that represents the computation of this future and
     * afterwards the computation of the future returned by the generator.
     *
     * @param nextGenerator The generator for the future to compute after this
     *                      future is done.
     * @param <T> The content type of the future returned by the mapping function
     * @return The new future
     */
    default <T> Future<T> flatMap(Supplier<? extends Future<T>> nextGenerator) {
        return flatMap($ -> nextGenerator.get());
    }


    /**
     * Returns a future with the given result already computed.
     *
     * @param result The result for the future
     * @param <V> Result type
     * @return A future with the given result
     */
    static <V> Future<V> of(V result) {
        return new PrecomputedFuture<>(result);
    }

    /**
     * Returns a future that has failed because of the given exception.
     *
     * @param cause The failure cause, null will be threaded as cancellation
     * @return A failed future
     */
    static <V> Future<V> failed(@Nullable Exception cause) {
        return new FailedFuture<>(cause);
    }
}
