package com.github.rccookie.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DistinctIterator<T> extends FilteringIterator<T> {

    private final Set<T> known = new HashSet<>();

    public DistinctIterator(Iterator<? extends T> iterator) {
        super(iterator, $ -> true);
    }

    public DistinctIterator(Iterable<? extends T> iterable) {
        this(iterable.iterator());
    }

    @Override
    boolean test(T t) {
        return known.add(t);
    }
}
