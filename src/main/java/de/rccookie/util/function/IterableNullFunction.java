package de.rccookie.util.function;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IterableNullFunction<T> extends Iterable<T> {

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return new Iterator<>() {
            T next;
            boolean hasNext;
            boolean nextComputed = false;

            @Override
            public boolean hasNext() {
                computeNext();
                return hasNext;
            }

            @Override
            public T next() {
                computeNext();
                if(!hasNext)
                    throw new NoSuchElementException();
                return next;
            }

            private void computeNext() {
                if(nextComputed) return;
                hasNext = true;
                try { next = getNext(); }
                catch(InternalException e) { e.rethrow(); }
                catch(RuntimeException e) { hasNext = false; }
                nextComputed = true;
            }
        };
    }

    @SuppressWarnings("DuplicateThrows")
    T getNext() throws InternalException, RuntimeException;

    class InternalException extends RuntimeException {

        public InternalException(RuntimeException cause) {
            super(cause);
        }

        @Contract("->fail")
        public void rethrow() {
            throw (RuntimeException) getCause();
        }
    }
}
