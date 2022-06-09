package com.github.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

public interface ForIFunction<T> extends Iterable<T> {

    @NotNull
    @Override
    default java.util.Iterator<T> iterator() {
        return new Iterator<>() {
            int i=0;
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
                next = getNext(i++);
                nextComputed = true;
            }
        };
    }

    T getNext(int i);
}
