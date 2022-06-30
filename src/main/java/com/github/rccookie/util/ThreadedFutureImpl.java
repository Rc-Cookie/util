package com.github.rccookie.util;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * General-purpose implementation of {@link Future}.
 *
 * @param <V> The content type
 */
public class ThreadedFutureImpl<V> extends AbstractFutureImpl<V> {

    // TODO: Use pool that notices shutdown
    public static final Executor DEFAULT_EXECUTOR
            = r -> new Thread(r).start();//new AutoShutdownThreadPoolExecutor(0, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS, new SynchronousQueue<>());

    private final Object lock = new Object();

    public ThreadedFutureImpl() { }

    public ThreadedFutureImpl(Computation<V> computation) {
        this(computation, DEFAULT_EXECUTOR);
    }

    protected ThreadedFutureImpl(Computation<V> computation, Executor executor) {
        executor.execute(() -> computation.tryCompute(this::complete, this::fail));
    }

    /**
     * Sets the result of the future.
     *
     * @param value The result to set
     * @throws IllegalStateException If the result is already set or
     *                               the future has been cancelled
     */
    @Override
    public void complete(V value) throws IllegalStateException {
        synchronized(lock) {
            if(done)
                throw new IllegalStateException("The value cannot be set because the computation is already done");
            this.value = value;
            done = true;
        }
        for(Consumer<? super V> a : onThen)
            a.accept(value);
        onThen.clear();
        onExcept.clear();
        lock.notifyAll();
    }

    @Override
    public boolean fail(Exception cause) throws IllegalStateException {
        synchronized(lock) {
            if(canceled) return false;
            if(done) throw new IllegalStateException("Result already computed");
            canceled = done = true;
        }
        failCause = cause;
        if(cause != null && onExcept.isEmpty()) {
            Console.warn("Uncaught exception in future:");
            Console.warn(cause);
        }
        else for(Consumer<? super Exception> handler : onExcept)
            handler.accept(cause);
        lock.notifyAll();
        return true;
    }

    @Override
    public boolean cancel() {
        boolean out;
        synchronized(lock) {
            out = !canceled && !done;
            canceled = done = true;
        }
        failCause = null;
        if(out) {
            for(Consumer<? super Exception> handler : onExcept)
                handler.accept(null);
            onThen.clear();
            onExcept.clear();
        }
        lock.notifyAll();
        return out;
    }

    @Override
    public boolean isCanceled() {
        synchronized(lock) {
            return super.isCanceled();
        }
    }

    @Override
    public boolean isDone() {
        synchronized(lock) {
            return super.isDone();
        }
    }

    @Override
    public V get() throws IllegalStateException {
        synchronized(lock) {
            return super.get();
        }
    }

    @Override
    public Future<V> then(Consumer<? super V> action) {
        Arguments.checkNull(action, "action");
        synchronized(lock) {
            if(!done) {
                onThen.add(action);
                return this;
            }
            else if(canceled) return this;
        }
        action.accept(value);
        return this;
    }

    @Override
    public Future<V> except(Consumer<? super Exception> handler) {
        Arguments.checkNull(handler, "handler");
        synchronized(this) {
            if(!done) {
                onExcept.add(handler);
                return this;
            }
            else if(!canceled) return this;
        }
        handler.accept(failCause);
        return this;
    }

    @Override
    public V waitFor() throws UnsupportedOperationException {
        synchronized(lock) {
            while(!done) {
                try { lock.wait(); }
                catch(InterruptedException ignored) { }
            }
        }
        return get();
    }
}
