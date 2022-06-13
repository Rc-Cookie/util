package com.github.rccookie.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * General-purpose implementation of {@link Future}.
 *
 * @param <V> The content type
 */
public class ThreadedFutureImpl<V> extends AbstractFutureImpl<V> {

    // TODO: Use pool that notices shutdown
    public static final Executor DEFAULT_EXECUTOR
            = r -> new Thread(r).start();//new AutoShutdownThreadPoolExecutor(0, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS, new SynchronousQueue<>());

    /**
     * Threads that are waiting for the result of the computation;
     */
    private Set<Thread> waiting = new HashSet<>();

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
        super.complete(value);
        onDone();
    }

    @Override
    public boolean fail(Exception cause) throws IllegalStateException {
        if(!super.fail(cause)) return false;
        onDone();
        return true;
    }

    @Override
    public boolean cancel() {
        boolean out = super.cancel();
        onDone();
        return out;
    }

    private void onDone() {
        for(Thread t : waiting)
            t.interrupt();
        waiting = null; // Free up memory
    }

    @Override
    public V waitFor() throws UnsupportedOperationException {
        while(!isDone()) { // use while in case thread gets interrupted from elsewhere
            synchronized (this) { waiting.add(Thread.currentThread()); }
            try { Thread.currentThread().join(); }
            catch(InterruptedException ignored) { }
        }
        return get();
    }
}
