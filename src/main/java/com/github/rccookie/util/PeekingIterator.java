package com.github.rccookie.util;

import java.util.Iterator;
import java.util.function.Consumer;

public class PeekingIterator<T> implements IterableIterator<T> {

    private final Iterator<? extends T> iterator;
    private final Consumer<? super T> action;

    public PeekingIterator(Iterator<? extends T> iterator, Consumer<? super T> action) {
        this.iterator = Arguments.checkNull(iterator);
        this.action = Arguments.checkNull(action);
    }

    public PeekingIterator(Iterable<? extends T> iterable, Consumer<? super T> action) {
        this(iterable.iterator(), action);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        T next = iterator.next();
        action.accept(next);
        return next;
    }
}
