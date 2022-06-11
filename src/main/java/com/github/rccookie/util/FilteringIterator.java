package com.github.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

public class FilteringIterator<T> implements IterableIterator<T> {

    private final Iterator<? extends T> iterator;
    private final Predicate<? super T> filter;
    private T next;
    private Boolean hasNext = null;

    public FilteringIterator(Iterator<? extends T> iterator, Predicate<? super T> filter) {
        this.iterator = Arguments.checkNull(iterator);
        this.filter = Arguments.checkNull(filter);
    }

    public FilteringIterator(Iterable<? extends T> iterable, Predicate<? super T> filter) {
        this(iterable.iterator(), filter);
    }

    @Override
    public boolean hasNext() {
        compute();
        return hasNext;
    }

    @Override
    public T next() {
        compute();
        if(!hasNext) throw new NoSuchElementException();
        hasNext = null;
        return next;
    }

    boolean test(T t) {
        return filter.test(t);
    }

    private void compute() {
        if(hasNext != null) return;
        while(iterator.hasNext()) {
            next = iterator.next();
            if(test(next)) {
                hasNext = true;
                return;
            }
        }
        hasNext = false;
    }
}
