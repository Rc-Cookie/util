package de.rccookie.util;

import java.util.ListIterator;

import org.jetbrains.annotations.NotNull;

public interface IterableListIterator<T> extends IterableIterator<T>, ListIterator<T> {

    IterableListIterator<?> EMPTY = new IterableListIterator<>() {
        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public Object previous() {
            throw new EmptyIteratorException();
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return -1;
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }

        @Override
        public void set(Object o) {
            throw new IllegalStateException();
        }

        @Override
        public void add(Object o) {
            throw new IllegalStateException();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new EmptyIteratorException();
        }
    };


    /**
     * Returns an empty iterable iterator.
     *
     * @param <T> The type (has no effect)
     * @return {@link #EMPTY}
     */
    @SuppressWarnings("unchecked")
    @NotNull
    static <T> IterableListIterator<T> empty() {
        return (IterableListIterator<T>) EMPTY;
    }
}
