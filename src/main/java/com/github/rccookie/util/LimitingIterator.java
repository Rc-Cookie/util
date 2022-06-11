package com.github.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LimitingIterator<T> implements IterableIterator<T> {

    private final Iterator<T> iterator;
    private final long limit;
    private long count = 0;

    public LimitingIterator(Iterator<T> iterator, long limit) {
        this.iterator = Arguments.checkNull(iterator);
        this.limit = limit;
    }

    public LimitingIterator(Iterable<T> iterable, long limit) {
        this(iterable.iterator(), limit);
    }

    @Override
    public boolean hasNext() {
        return count < limit && iterator.hasNext();
    }

    @Override
    public T next() {
        if(count >= limit) throw new NoSuchElementException();
        count++;
        return iterator.next();
    }
}
