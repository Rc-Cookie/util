package de.rccookie.util;

import java.util.function.Supplier;

public interface CachedSupplier<T> extends Supplier<T> {

    void invalidate();

    boolean isCached();

    default CachedSupplier<T> threadSafe() {
        return new Synchronized<>(this);
    }



    static <T> CachedSupplier<T> timed(Supplier<? extends T> generator, long lifetime) {
        return new Timed<>(generator, lifetime);
    }



    class Timed<T> implements CachedSupplier<T> {

        private final Supplier<? extends T> generator;
        private final long lifetime;

        private long timeout = 0;
        private T value = null;

        protected Timed(Supplier<? extends T> generator, long lifetime) {
            this.lifetime = lifetime;
            this.generator = Arguments.checkNull(generator, "generator");
        }

        @Override
        public T get() {
            if(!isCached()) {
                value = generator.get();
                long now = System.currentTimeMillis();
                timeout = now + lifetime;
                if(timeout < now)
                    timeout = Long.MAX_VALUE;
            }
            return value;
        }

        @Override
        public boolean isCached() {
            return System.currentTimeMillis() < timeout;
        }

        @Override
        public void invalidate() {
            timeout = 0;
            value = null;
        }
    }

    class Synchronized<T> implements CachedSupplier<T> {

        private final CachedSupplier<? extends T> cache;

        public Synchronized(CachedSupplier<? extends T> cache) {
            this.cache = Arguments.checkNull(cache, "supplier");
        }

        @Override
        public synchronized T get() {
            return cache.get();
        }

        @Override
        public synchronized boolean isCached() {
            return cache.isCached();
        }

        @Override
        public synchronized void invalidate() {
            cache.invalidate();
        }

        @Override
        public CachedSupplier<T> threadSafe() {
            return this;
        }
    }
}
