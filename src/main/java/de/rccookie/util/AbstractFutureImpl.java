package de.rccookie.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

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
     * The functions to be called when the result is available.
     */
    protected final List<Consumer<? super V>> onThen = new ArrayList<>();
    /**
     * The functions to be called when the result gets cancelled.
     */
    protected final List<Consumer<? super Exception>> onExcept = new ArrayList<>();


    @Override
    public boolean cancel() {
        boolean out = !canceled && !done;
        canceled = done = true;
        failCause = null;
        if(out) {
            for(Consumer<? super Exception> handler : onExcept)
                handler.accept(null);
            onThen.clear();
            onExcept.clear();
        }
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
        Arguments.checkNull(action, "action");
        if(!done)
            onThen.add(action);
        else if(!canceled)
            action.accept(value);
        return this;
    }

    @Override
    public Future<V> except(Consumer<? super Exception> handler) {
        Arguments.checkNull(handler, "handler");
        if(!done) onExcept.add(handler);
        else if(canceled)
            handler.accept(failCause);
        return this;
    }

    @Override
    public void complete(V value) throws IllegalStateException {
        if(isDone()) throw new IllegalStateException("The value cannot be set because the computation is already done");
        this.value = value;
        done = true;
        for(Consumer<? super V> a : onThen)
            a.accept(value);
        onThen.clear();
        onExcept.clear();
    }

    @Override
    public boolean fail(@Nullable Exception cause) throws IllegalStateException {
        if(canceled) return false;
        if(done) throw new IllegalStateException("Result already computed");
        canceled = done = true;
        failCause = cause;
        if(cause != null && onExcept.isEmpty()) {
            Console.warn("Uncaught exception in future:");
            Console.warn(cause);
        }
        else for(Consumer<? super Exception> handler : onExcept)
            handler.accept(cause);
        return true;
    }

    @Override
    public <T> Future<T> map(Function<? super V, ? extends T> mapper) {
        return flatMap(r -> Future.of(mapper.apply(r)));
    }

    @Override
    public <T> Future<T> flatMap(Function<? super V, ? extends Future<T>> mapper) {
        return new FlatMappedFutureImpl<>(this, mapper);
    }

    static class FlatMappedFutureImpl<I,O> extends AbstractFutureImpl<O> {

        final Future<I> input;
        Future<O> secondComputation;
        private final Object lock = new Object();

        public FlatMappedFutureImpl(Future<I> input, Function<? super I, ? extends Future<O>> mapper) {
            this.input = input;
            input.then(v -> {
                synchronized(lock) {
                    secondComputation = mapper.apply(v);
                    for (Consumer<? super O> action : onThen)
                        secondComputation.then(action);
                    for (Consumer<? super Exception> handler : onExcept)
                        secondComputation.except(handler);
                    onThen.clear();
                    onExcept.clear();
                }
            });
            input.except(this::fail);
        }

        @Override
        public O waitFor() throws IllegalStateException, UnsupportedOperationException {
            input.waitFor(); // Calls 'then' action
            assert secondComputation != null;
            return secondComputation.waitFor();
        }

        @Override
        public Future<O> then(Consumer<? super O> action) {
            synchronized(lock) {
                if(secondComputation == null)
                    return super.then(action);
            }
            secondComputation.then(action);
            return this;
        }

        @Override
        public Future<O> except(Consumer<? super Exception> handler) {
            synchronized(lock) {
                if(secondComputation == null)
                    return super.except(handler);
            }
            secondComputation.except(handler);
            return this;
        }
    }
}
