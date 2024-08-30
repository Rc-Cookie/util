package de.rccookie.util.function;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

public interface IterableFunction<T> extends Iterable<T> {

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return new Iterator<>() {
            T next;
            boolean nextComputed = false;

            @Override
            public boolean hasNext() {
                computeNext();
                return next != null;
            }

            @Override
            public T next() {
                computeNext();
                if(next == null)
                    throw new NoSuchElementException();
                return next;
            }

            private void computeNext() {
                if(nextComputed) return;
                next = getNext();
                nextComputed = true;
            }
        };
    }

    T getNext();
}
