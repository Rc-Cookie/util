package com.github.rccookie.util;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Future implementation that only starts the computation if the result is requested
 * directly (using {@link #waitFor()}) or indirectly (using {@link #then} or {@link #onCancel}).
 *
 * @param <V> Content type
 */
public class OnDemandFutureImpl<V> extends ThreadedFutureImpl<V> {

    private final Computation<V> computation;
    private final Executor executor;
    private boolean started = false;

    public OnDemandFutureImpl(Computation<V> computation) {
        this(computation, DEFAULT_EXECUTOR);
    }

    public OnDemandFutureImpl(Computation<V> computation, Executor executor) {
        this.computation = Arguments.checkNull(computation, "computation");
        this.executor = Arguments.checkNull(executor, "executor");
    }

    @Override
    public V waitFor() throws UnsupportedOperationException {
        startComputation();
        return super.waitFor();
    }

    @Override
    public Future<V> then(Consumer<? super V> action) {
        startComputation();
        return super.then(action);
    }

    @Override
    public Future<V> except(Consumer<? super Exception> handler) {
        startComputation();
        return super.except(handler);
    }

    public synchronized Future<V> startComputation() {
        if(started) return this;
        started = true;
        if(isCanceled()) return this;
        executor.execute(() -> {
            try {
                complete(computation.compute());
            } catch(Exception e) {
                fail(e);
            }
        });
        return this;
    }
}
